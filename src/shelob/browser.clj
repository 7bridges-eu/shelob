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

(defn go
  [driver url]
  (.get driver url)
  driver)

(defn wait-for
  ([driver condition]
   (wait-for driver condition 2))
  ([driver condition timeout]
   (let [wdw (WebDriverWait. driver timeout)]
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

(defn webdriver-thread
  [init-fn chan-in chan-out]
  (as/thread
    (loop [webdriver (init-webdriver init-fn)]
      (let [f (as/<!! chan-in)]
        (->> (f webdriver)
             (as/>!! chan-out)))
      (recur webdriver))))

(defn webdriver-pool
  [init-fn chan-in chan-out pool-size]
  (doall
   (repeatedly pool-size #(webdriver-thread init-fn
                                            chan-in
                                            chan-out))))
