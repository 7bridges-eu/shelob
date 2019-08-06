;; Copyright 2019 7bridges s.r.l.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns shelob.core
  (:require
   [clojure.core.async :as as]
   [clojure.string :as s]
   [shelob.browser :as shb]
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders])
  (:import
   [org.openqa.selenium.chrome ChromeDriver ChromeOptions]
   [org.openqa.selenium.edge EdgeDriver]
   [org.openqa.selenium.ie InternetExplorerDriver]
   [org.openqa.selenium.firefox FirefoxDriver FirefoxDriver$SystemProperty FirefoxOptions]
   [org.openqa.selenium.opera OperaDriver]
   [org.openqa.selenium.safari SafariDriver]))

(def ^:const pool-size 10)

(timbre/merge-config!
 {:appenders {:spit (appenders/spit-appender {:fname "shelob.log"})}})

(defn chrome-driver
  []
  (System/setProperty "webdriver.chrome.silentLogging" "true")
  (System/setProperty "webdriver.chrome.silentOutput" "true")
  (-> (ChromeOptions.)
      (.setHeadless true)
      (ChromeDriver.)))

(defn edge-driver
  []
  (EdgeDriver.))

(defn firefox-driver
  []
  (System/setProperty FirefoxDriver$SystemProperty/BROWSER_LOGFILE "/dev/null")
  (-> (FirefoxOptions.)
      (.setHeadless true)
      (FirefoxDriver.)))

(defn internet-explorer-driver
  []
  (InternetExplorerDriver.))

(defn opera-driver
  []
  (OperaDriver.))

(defn safari-driver
  []
  (SafariDriver.))

(defn web-driver
  [browser]
  (case browser
    :chrome (chrome-driver)
    :edge (edge-driver)
    :firefox (firefox-driver)
    :internet-explorer (internet-explorer-driver)
    :opera (opera-driver)
    :safari (safari-driver)
    (firefox-driver)))

(defn- init-webdriver
  ([browser]
   (web-driver browser))
  ([browser init-fn]
   (init-fn (web-driver browser))))

(defn- navigate-to
  [webdriver url]
  (-> (shb/go webdriver url)
      .getTitle))

(defn- scrape-data
  [source]
  (s/split source #" "))

(defn- webdriver-pool
  [init-fn pool-size]
  (reduce (fn [acc _]
            (conj acc (init-webdriver :firefox init-fn)))
          []
          (range pool-size)))

(defn- close-pool
  [pool result]
  (doseq [driver pool]
    (.close driver))
  result)

(defn navigate-and-scrape-xf
  [navigate-fn scrape-fn]
  (comp (map #(apply navigate-fn %))
        (mapcat scrape-fn)))

(defn navigate-and-scrape
  [init-fn navigate-fn scrape-fn urls]
  (let [pool (webdriver-pool init-fn pool-size)]
    (try
      (let [in-ch (as/chan pool-size (navigate-and-scrape-xf navigate-fn scrape-fn))
            data (map vector (cycle pool) urls)]
        (as/go (as/onto-chan in-ch data))
        (close-pool pool (as/<!! (as/into [] in-ch))))
      (catch Exception e
        (close-pool pool e)
        (throw e)))))
