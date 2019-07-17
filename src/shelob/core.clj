(ns shelob.core
  (:import [org.openqa.selenium WebDriver By]
           [org.openqa.selenium.firefox FirefoxDriver]
           [org.openqa.selenium.support.ui WebDriverWait ExpectedConditions]))

(defn web-driver
  []
  (FirefoxDriver. ))

(defn go
  [driver url]
  (.get driver url)
  driver)

(defn expected-condition
  [condition locator]
  (case condition
    :visibility (ExpectedConditions/visibilityOfElementLocated locator)))

(defn wait-for
  ([driver condition locator]
   (wait-for driver condition locator 2))
  ([driver condition locator timeout]
   (let [wdw (WebDriverWait. driver timeout)
         ec (expected-condition condition locator)]
     (.until wdw ec))))

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
  [starting-point by]
  (.findElement starting-point by))

(defn find-elements
  [starting-point by]
  (.findElements starting-point by))

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
  [starting-point by text]
  (-> (find-element starting-point by)
      (fill text))
  starting-point)

(defn click
  [element]
  (.click element)
  element)

(defn click-by
  [starting-point by]
  (-> (find-element starting-point by)
      click)
  starting-point)
