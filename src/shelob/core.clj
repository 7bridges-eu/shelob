(ns shelob.core
  (:import [org.openqa.selenium WebDriver By]
           [org.openqa.selenium.firefox FirefoxDriver]))

(defn web-driver
  []
  (FirefoxDriver. ))

(defn go
  [driver url]
  (.get driver url)
  driver)

(defn by
  [{:keys [what how]}]
  (case what
    :id (By/id how)
    :link-text (By/linkText how)
    :name (By/name how)
    :class-name (By/className how)
    :xpath (By/xpath how)))

(defn find-element
  [starting-point by]
  (.findElement starting-point by))

(defn find-elements
  [starting-point by]
  (.findElements starting-point by))

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
