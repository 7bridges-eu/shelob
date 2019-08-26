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

(ns shelob.browser.commands
  (:require
   [shelob.conditions :as conditions])
  (:import
   (org.openqa.selenium WebDriver By)
   (org.openqa.selenium.support.ui WebDriverWait)))

(defmulti browser-command :msg)

(defmethod browser-command :clean-cookies [{:keys [driver]}]
  (.. driver manage deleteAllCookies))

(defmethod browser-command :go [{:keys [driver url]}]
  (.get driver url))

(defn- :by [context query]
  (case context
    :class-name (By/className query)
    :css-selector (By/cssSelector query)
    :id (By/id query)
    :link-text (By/linkText query)
    :name (By/name query)
    :partial-link-text (By/partialLinkText query)
    :tag-name (By/tagName query)
    :xpath (By/xpath query)))

(defmethod browser-command :find-element [{:keys [starting-point locator]}]
  (.findElement starting-point locator))

(defmethod browser-command :find-elements [{:keys [starting-point locator]}]
  (.findElements starting-point locator))

(defmethod browser-command :children [{:keys [starting-point]}]
  (.findElements starting-point (By/xpath ".//*")))

(defmethod browser-command :children-by [{:keys [starting-point locator]}]
  (.findElements starting-point locator))

(defmethod browser-command :fill [{:keys [element text]}]
  (->> [text]
       into-array
       (.sendKeys element)))

(defmethod browser-command :fill-by [{:keys [starting-point locator text]}]
  (let [element (.findElement starting-point locator)]
    (->> [text]
         into-array
         (.sendKeys element))))

(defmethod browser-command :click [{:keys [element]}]
  (.click element))

(defmethod browser-command :click-by [{:keys [driver locator]}]
  (-> (.findElement driver locator)
      (.click)))

(defmethod browser-command :attribute [{:keys [driver attribute-name]}]
  (.getAttribute driver attribute-name))

(defmethod browser-command :attribute-by [{:keys [driver locator attribute-name]}]
  (-> (.findElement driver locator)
      (.getAttribute attribute-name)))

(defmethod browser-command :text [{:keys [driver]}]
  (.getText driver))

(defmethod browser-command :text-by [{:keys [driver locator]}]
  (-> (.findElement driver locator)
      (.getText)))

(defmethod browser-command :title [{:keys [driver]}]
  (.getTitle driver))

(defmethod browser-command :source [{:keys [driver]}]
  (.getPageSource driver))
