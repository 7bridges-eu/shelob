(ns shelob.temp
  (:require
   [clojure.core.async :as as]
   [shelob.browser :as shb]
   [shelob.scraper :as shs]
   [taoensso.timbre :as timbre])
  (:import
   [org.openqa.selenium.chrome ChromeDriver ChromeOptions]
   [org.openqa.selenium.edge EdgeDriver]
   [org.openqa.selenium.ie InternetExplorerDriver]
   [org.openqa.selenium.firefox FirefoxDriver FirefoxDriver$SystemProperty FirefoxOptions]
   [org.openqa.selenium.opera OperaDriver]
   [org.openqa.selenium.safari SafariDriver]
   [org.openqa.selenium Proxy]))

(def driver-pool (atom []))

(defn close-driver-pool
  [pool]
  (doseq [driver pool]
    (timbre/debug "Closing " (.hashCode driver))
    (.close driver))
  (reset! driver-pool []))

(defn- ->proxy [http ssl]
  (-> (Proxy.)
      (.setHttpProxy http)
      (.setSslProxy ssl)))

(defmulti ->driver-options :browser)
(defmethod ->driver-options :firefox [options]
  (let [default-options (-> (FirefoxOptions.)
                            (.setHeadless true))]
    (if-let [proxy (:proxy options)]
      (->> (->proxy proxy proxy)
           (.setProxy default-options))
      default-options)))

(defmethod ->driver-options :chrome [options]
  (throw (ex-info "Chrome not implemented yet!" {})))

(defmethod ->driver-options :edge [options]
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
  (let [opts (->driver-options options)
        browser (:browser options)
        driver (web-driver browser opts)]
    (swap! driver-pool conj driver)
    driver))

(defn init-driver-pool [{:keys [driver-options pool-size] :or {pool-size 5}}]
  (->> (repeatedly pool-size #(init-driver driver-options))
       (reset! driver-pool)))

(defn- to-vector [x]
  (if (vector? x) x (vector x)))

(defn- process-messages
  ([messages]
   (doseq [driver @driver-pool]
     (process-messages driver messages)))
  ([driver messages]
   (doseq [message messages]
     (->> (assoc message :driver driver)
          shb/validate
          shb/browser-command))))

(defn- driver-listener
  "Create a command executor listener. Messages should always be vectors."
  [driver in-ch out-ch]
  (as/thread
    (loop []
      (when-let [messages (as/<!! in-ch)]
        (process-messages driver messages)
        (->> (.getPageSource driver)
             (as/>!! out-ch))
        (recur)))))

(defn- scraper-listener [ctx in-ch]
  (let [scrape-fn (:scrape-fn ctx)]
    (as/thread
      (when-let [source (as/<!! in-ch)]
        (->> source
             shs/parse
             scrape-fn)))))

(defn init-executors
  "Starts a thread for each instanced driver and returns a merged channel."
  [in-ch out-ch]
  (timbre/debug "Initialize executors...")
  (->> @driver-pool
       (reduce (fn [acc driver] (conj acc (driver-listener driver in-ch out-ch))) [])
       as/merge))

(defn init-scrapers
  "Starts `n` scraper threads with `ctx` and returns a merged channel."
  [ctx n in-ch]
  (timbre/debug "Initialize scrapers...")
  (-> n
      (repeatedly #(scraper-listener ctx in-ch))
      as/merge))

(defn init [ctx]
  (let [init-messages (:init-messages ctx)]
    (init-driver-pool ctx)
    (when init-messages
      (process-messages init-messages))))

(defn- send-batch-messages [ctx messages]
  (timbre/debug "Messages " (count messages))
  (let [msg-ch (as/chan (:pool-size ctx 5))
        scraper-ch (as/chan)
        result-ch (init-scrapers ctx (count messages) scraper-ch)]
    (init-executors msg-ch scraper-ch)
    (as/onto-chan msg-ch messages)
    (let [results (as/<!! (as/reduce into [] result-ch))]
      (as/close! msg-ch)
      (as/close! scraper-ch)
      results)))

(defn send-messages [ctx messages]
  (->> messages
       (partition-all 500)
       (reduce
        (fn [results messages-batch]
          (into results (send-batch-messages ctx messages-batch)))
        [])))

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

(defn example [context]
  (init context)
  (->> [[{:msg :source}]
        [{:msg :source}]
        [{:msg :source}]
        [{:msg :source}]]
       (send-messages context)))
