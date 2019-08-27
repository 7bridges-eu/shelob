(ns shelob.temp
  (:require
   [clojure.core.async :as as]
   [shelob.browser :as shb]
   [shelob.scraper :as shs])
  (:import
   [org.openqa.selenium.chrome ChromeDriver ChromeOptions]
   [org.openqa.selenium.edge EdgeDriver]
   [org.openqa.selenium.ie InternetExplorerDriver]
   [org.openqa.selenium.firefox FirefoxDriver FirefoxDriver$SystemProperty FirefoxOptions]
   [org.openqa.selenium.opera OperaDriver]
   [org.openqa.selenium.safari SafariDriver]
   [org.openqa.selenium Proxy]))

(def driver-pool (atom []))

(defn- close-driver-pool
  [pool]
  (doseq [driver pool]
    (println "Closing " (.hashCode driver))
    (.close driver))
  (reset! driver-pool []))

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
        browser (:browser options)
        driver (web-driver browser opts)]
    (swap! driver-pool conj driver)
    driver))

(defn- to-vector [x]
  (if (vector? x) x (vector x)))

(defn- process-messages
  ([messages]
   (println "Drivers " (count @driver-pool))
   (doseq [driver @driver-pool]
     (process-messages driver messages)))
  ([driver messages]
   (doseq [message messages]
     (println "Sending"
              message
              "from"
              (.getId (Thread/currentThread))
              "to"
              (.hashCode driver))
     (->> (assoc message :driver driver)
          shb/validate
          shb/browser-command))))

(defn- driver-listener
  "Create a command executor listener. Messages should always be vectors."
  [ctx]
  (let [driver-options (:driver-options ctx)
        in-ch (get-in ctx [:channels :messages])
        out-ch (get-in ctx [:channels :scraper])
        driver (init-driver driver-options)]
    (as/go
      (loop [driver driver]
        (when-let [messages (as/<! in-ch)]
          (process-messages driver messages)
          (->> (.getPageSource driver)
               (as/>! out-ch))
          (recur driver))))))

(defn init-executors [ctx]
  (let [pool-size (:pool-size ctx 5)
        init-messages (:init-messages ctx)]
    (dotimes [_ pool-size]
      (driver-listener ctx))
    (when init-messages
      (println "Init drivers" init-messages)
      (process-messages init-messages)))
  ctx)

(defn- scraper-listener [ctx]
  (let [in-ch (get-in ctx [:channels :scraper])
        out-ch (get-in ctx [:channels :results])
        scrape-fn (:scrape-fn ctx)]
    (as/go
      (loop []
        (when-let [source (as/<! in-ch)]
          (->> source
               shs/parse
               scrape-fn
               (as/>! out-ch))
          (recur))))))

(defn init-scrapers [ctx]
  (let [pool-size (:pool-size ctx 5)]
    (dotimes [_ pool-size]
      (scraper-listener ctx)))
  ctx)

(defn init [ctx]
  (-> ctx
      init-channels
      init-executors
      init-scrapers))

(defn scrape-fn [document]
  (map shs/text (shs/select document ".result__url__domain")))

(def ddg-scrape
  {:driver-options {:browser :firefox}
   :pool-size 2
   :init-messages [{:msg :go :url "https://duckduckgo.com/"}
                   {:msg :fill
                    :locator (shb/by-css-selector "#search_form_input_homepage")
                    :text "Clojure"}
                   {:msg :click :locator (shb/by-css-selector "#search_button_homepage")}]
   :scrape-fn scrape-fn})

(defn example []
  (let [context (init ddg-scrape)
        in-chan (get-in context [:channels :messages])
        out-chan (get-in context [:channels :results])]
    (as/go (as/>! in-chan [{:msg :source}]))
    (println (as/<!! (as/into [] out-chan)))
    context))
