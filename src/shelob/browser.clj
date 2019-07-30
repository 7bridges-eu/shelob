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
  (:import
   [org.openqa.selenium WebDriver By]
   [org.openqa.selenium.support.ui WebDriverWait]))

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
