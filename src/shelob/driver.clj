(ns shelob.driver
  (:require
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
    (timbre/debug "Closing driver" (.hashCode driver))
    (.close driver))
  (reset! driver-pool []))

(defn- ->proxy
  [http ssl]
  (-> (Proxy.)
      (.setHttpProxy http)
      (.setSslProxy ssl)))

(defmulti ->driver-options :browser)
(defmethod ->driver-options :firefox [options]
  (System/setProperty FirefoxDriver$SystemProperty/BROWSER_LOGFILE "/dev/null")
  (let [default-options (-> (FirefoxOptions.)
                            (.setHeadless true))]
    (if-let [proxy (:proxy options)]
      (->> (->proxy proxy proxy)
           (.setProxy default-options))
      default-options)))

(defmethod ->driver-options :chrome [options]
  (System/setProperty "webdriver.chrome.silentLogging" "true")
  (System/setProperty "webdriver.chrome.silentOutput" "true")
  (let [default-options (-> (ChromeOptions.)
                            (.setHeadless true))]
    (if-let [proxy (:proxy options)]
      (->> (->proxy proxy proxy)
           (.setProxy default-options))
      default-options)))

(defmethod ->driver-options :edge [_]
  (throw (ex-info "Edge not implemented yet!" {})))

(defn chrome-driver
  [options]
  (ChromeDriver. options))

(defn edge-driver
  [options]
  (EdgeDriver. options))

(defn firefox-driver
  [options]
  (FirefoxDriver. options))

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

(defn init-driver
  [options]
  (let [opts (->driver-options options)
        browser (:browser options)
        driver (web-driver browser opts)]
    (swap! driver-pool conj driver)
    driver))

(defn init-driver-pool
  [{:keys [driver-options pool-size] :or {pool-size 5}}]
  (->> (repeatedly pool-size #(init-driver driver-options))
       (reset! driver-pool)))
