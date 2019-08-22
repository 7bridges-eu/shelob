(ns shelob.temp
  (:require
   [clojure.core.async :as as]
   [shelob.browser :as shb]
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
    (.quit (:driver driver)))
  (reset! driver-pool {}))

(defn available-driver
  [pool]
  (->> pool
       (filter (fn [[_ v]] (:available v)))
       first))

(defn- ->proxy [http ssl]
  (-> (Proxy.)
      (.setHttpProxy http)
      (.setSslProxy ssl)))

(defmulti driver-options :driver)
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

(defn init-driver [options]
  (let [opts (driver-options options)
        driver-type (:driver options)
        driver (case driver-type
                 :firefox (FirefoxDriver. opts)
                 :chrome (ChromeDriver. opts)
                 :edge (EdgeDriver. opts)
                 (throw (ex-info (str "Invalid browser type: " driver-type) {})))]
    driver))

(defn init-driver-pool [ctx]
  (let [pool-size (:pool-size ctx 5)]
    (dotimes [_ pool-size]
      (let [options (:driver-options ctx)
            driver (init-driver options)]
        (swap! driver-pool assoc (.hashCode driver) {:driver driver
                                                     :available true})))
    ctx))

(defn init [ctx]
  (-> ctx
      init-driver-pool))

(defn example []
  (let [context (init {:driver-options {:driver :firefox}})
        in-chan (get-in context [:channels :browser-in])
        out-chan (get-in context [:channels :browser-out])]
    (as/onto-chan in-chan [{:msg :go :url "https://7bridges.eu"}
                           {:msg :get-title}])
    (as/go-loop []
      (println (as/<! out-chan))
      (recur))))
