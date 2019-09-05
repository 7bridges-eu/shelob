(ns shelob.driver
  "This namespace contains facilities to interact with web drivers.
  The entry point is `init-driver-pool`"
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
(def driver-pool-size 5)

(defn close-driver-pool
  "Closes every driver available in `pool`."
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
  "Creates a web driver for `browser` using `options`.
  Defaults to a Firefox driver is there is no matching keyword."
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
  "Initialises a new web driver with `options` and stores it in the driver pool."
  [options]
  (let [opts (->driver-options options)
        browser (:browser options)
        driver (web-driver browser opts)]
    (swap! driver-pool conj driver)
    driver))

(defn init-driver-pool
  "Initialises the driver pool of `pool-size` and using `driver-options`.
  `pool-size` defaults to 5 is not present."
  [{:keys [driver-options pool-size] :or {pool-size driver-pool-size}}]
  (->> (repeatedly pool-size #(init-driver driver-options))
       (reset! driver-pool)))
