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

(ns shelob.browser
  (:require [clojure.core.async :as as])
  (:import
   [org.openqa.selenium WebDriver By]
   [org.openqa.selenium.chrome ChromeDriver ChromeOptions]
   [org.openqa.selenium.edge EdgeDriver]
   [org.openqa.selenium.ie InternetExplorerDriver]
   [org.openqa.selenium.firefox FirefoxDriver FirefoxDriver$SystemProperty FirefoxOptions]
   [org.openqa.selenium.opera OperaDriver]
   [org.openqa.selenium.safari SafariDriver]
   [org.openqa.selenium.support.ui WebDriverWait]))

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

(defn clean-cookies
  [driver]
  (.. driver manage deleteAllCookies)
  driver)

(defn go
  [driver url]
  (.get driver url)
  driver)

(defn wait-for
  ([driver condition]
   (wait-for driver condition 2))
  ([driver condition timeout-seconds]
   (let [wdw (WebDriverWait. driver timeout-seconds)]
     (.until wdw condition))))

(defn by
  [context query]
  (case context
    :class-name (By/className query)
    :css-selector (By/cssSelector query)
    :id (By/id query)
    :link-text (By/linkText query)
    :name (By/name query)
    :partial-link-text (By/partialLinkText query)
    :tag-name (By/tagName query)
    :xpath (By/xpath query)))

(defn find-element
  [starting-point locator]
  (.findElement starting-point locator))

(defn find-elements
  [starting-point locator]
  (.findElements starting-point locator))

(defn children
  [starting-point]
  (find-elements starting-point (by :xpath ".//*")))

(defn children-by
  [starting-point locator]
  (find-elements starting-point locator))

(defn fill
  [element text]
  (->> [text]
       into-array
       (.sendKeys element))
  element)

(defn fill-by
  [starting-point locator text]
  (-> (find-element starting-point locator)
      (fill text))
  starting-point)

(defn click
  [element]
  (.click element)
  element)

(defn click-by
  [starting-point locator]
  (-> (find-element starting-point locator)
      click)
  starting-point)

(defn attribute
  [element attribute-name]
  (.getAttribute element attribute-name))

(defn attribute-by
  [starting-point locator attribute-name]
  (-> (find-element starting-point locator)
      (attribute attribute-name)))

(defn text
  [element]
  (.getText element))

(defn text-by
  [starting-point locator]
  (-> (find-element starting-point locator)
      text))

(defn- init-webdriver
  [init-fn]
  (init-fn (firefox-driver)))

(defn- webdriver-exec
  [f webdriver chan-out chan-err]
  (try
    (let [result (f webdriver)]
      (as/>!! chan-out result))
    (catch Exception e
      (as/>!! chan-err e))))

(defn webdriver-thread
  [init-fn chan-in chan-out chan-err]
  (as/thread
    (loop [webdriver (init-webdriver init-fn)]
      (if-let [f (as/<!! chan-in)]
        (do
          (webdriver-exec f webdriver chan-out chan-err)
          (recur webdriver))
        (.close webdriver)))))

(defn webdriver-pool
  [init-fn chan-in chan-out chan-err pool-size]
  (dotimes [thread-nr pool-size]
    (webdriver-thread init-fn chan-in chan-out chan-err)))
