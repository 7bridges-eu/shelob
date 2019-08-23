(ns shelob.temp
  (:require
   [clojure.core.async :as as]
   [shelob.browser.commands :as shc])
  (:import
   [org.openqa.selenium.chrome ChromeDriver ChromeOptions]
   [org.openqa.selenium.edge EdgeDriver]
   [org.openqa.selenium.ie InternetExplorerDriver]
   [org.openqa.selenium.firefox FirefoxDriver FirefoxDriver$SystemProperty FirefoxOptions]
   [org.openqa.selenium.opera OperaDriver]
   [org.openqa.selenium.safari SafariDriver]
   [org.openqa.selenium Proxy]))

(def driver-pool (atom {}))

(defn- close-driver-pool
  [pool]
  (doseq [[k driver] pool]
    (println "Closing " k)
    (.quit (:instance driver)))
  (reset! driver-pool {}))

(defn- available-driver
  [pool]
  (->> pool
       (filter (fn [[_ v]] (:available v)))
       first))

(defn take-driver [pool]
  (let [[driver-id driver] (available-driver @pool)]
    (swap! pool update-in [driver-id :available] not)
    [driver-id (:instance driver)]))

(defn release-driver [pool driver-id]
  (swap! pool update-in [driver-id :available] not))

(defn- init-channels [ctx]
  (assoc ctx :channels {:messages (as/chan)
                        :scraper (as/chan)
                        :results (as/chan)}))

(defn- close-channels [ctx]
  (let [channels (:channels ctx)]
    (doseq [channel (vals channels)]
      (as/close! channel)))
  (dissoc ctx :channels))

(defn- ->proxy [http ssl]
  (-> (Proxy.)
      (.setHttpProxy http)
      (.setSslProxy ssl)))

(defmulti driver-options :browser)
(defmethod driver-options :firefox [options]
  (let [default-options (-> (FirefoxOptions.)
                            (.setHeadless true))]
    (if-let [proxy (:proxy options)]
      (->> (->proxy proxy proxy)
           (.setProxy default-options))
      default-options)))

(defmethod driver-options :chrome [options]
  (throw (ex-info "Chrome not implemented yet!" {})))

(defmethod driver-options :edge [options]
  (throw (ex-info "Edge not implemented yet!" {})))

(defn chrome-driver
  [options]
  (System/setProperty "webdriver.chrome.silentLogging" "true")
  (System/setProperty "webdriver.chrome.silentOutput" "true")
  (-> options
      (.setHeadless true)
      (ChromeDriver.)))

(defn edge-driver
  [options]
  (EdgeDriver. options))

(defn firefox-driver
  [options]
  (System/setProperty FirefoxDriver$SystemProperty/BROWSER_LOGFILE "/dev/null")
  (-> options
      (.setHeadless true)
      (FirefoxDriver.)))

(defn internet-explorer-driver
  [options]
  (InternetExplorerDriver. options))

(defn opera-driver
  [options]
  (OperaDriver. options))

(defn safari-driver
  [options]
  (SafariDriver. options))

(defn web-driver
  [browser options]
  (case browser
    :chrome (chrome-driver options)
    :edge (edge-driver options)
    :firefox (firefox-driver options)
    :internet-explorer (internet-explorer-driver options)
    :opera (opera-driver options)
    :safari (safari-driver options)
    (firefox-driver options)))

(defn init-driver [options]
  (let [opts (driver-options options)
        browser (:browser options)]
    (web-driver browser opts)))

(defn init-driver-pool [ctx]
  (let [pool-size (:pool-size ctx 5)]
    (dotimes [_ pool-size]
      (let [options (:driver-options ctx)
            driver (init-driver options)]
        (swap! driver-pool assoc (.hashCode driver) {:instance driver
                                                     :available true})))
    ctx))

(defn- to-vector [x]
  (if (vector? x) x (vector x)))

(defn- exec-listener
  "Create a command executor listener. Messages should always be vectors."
  [ctx]
  (let [in-ch (get-in ctx [:channels :messages])
        scrapers-ch (get-in ctx [:channels :scraper])]
    (as/go-loop []
      (when-let [messages (as/<! in-ch)]
        (let [[driver-id driver] (take-driver driver-pool)]
          (doseq [message messages]
            (println "Sending" message)
            (->> (assoc message :driver driver)
                 shc/browser-command
                 (as/>! scrapers-ch)))
          (release-driver driver-pool driver-id)
          (recur))))))

(defn init-executors [ctx]
  (exec-listener ctx)
  ctx)

(defn init [ctx]
  (-> ctx
      init-driver-pool
      init-channels
      init-executors))

(defn example []
  (let [context (init {:driver-options {:browser :firefox}
                       :pool-size 2})
        in-chan (get-in context [:channels :messages])
        out-chan (get-in context [:channels :scraper])]
    (as/onto-chan in-chan [[{:msg :go :url "https://7bridges.eu"}
                            {:msg :source}]])
    (as/go-loop []
      (when-let [v (as/<! out-chan)]
        (println v)
        (recur)))))
