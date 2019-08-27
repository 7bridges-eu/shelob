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
  (:require
   [clojure.spec.alpha :as sp]
   [expound.alpha :as e])
  (:import
   (org.openqa.selenium WebDriver By)
   (org.openqa.selenium.remote RemoteWebDriver)
   (org.openqa.selenium.support.ui ExpectedConditions)
   (org.openqa.selenium.support.ui WebDriverWait)))

;; Locators

(defn by-class-name [class-name]
  (By/className class-name))

(defn by-css-selector [selector]
  (By/cssSelector selector))

(defn by-id [id]
  (By/id id))

(defn by-link-text [text]
  (By/linkText text))

(defn by-name [element-name]
  (By/name element-name))

(defn by-partial-link-text [text]
  (By/partialLinkText text))

(defn by-tag-name [tag-name]
  (By/tagName tag-name))

(defn by-xpath [xpath]
  (By/xpath xpath))

;; Conditions

(defn attribute-contains
  [^By locator ^String attribute ^String value]
  (ExpectedConditions/attributeContains locator attribute value))

(defn attribute-to-be
  [^By locator ^String attribute ^String value]
  (ExpectedConditions/attributeToBe locator attribute value))

(defn element-selection-state-to-be
  [^By locator ^Boolean selected]
  (ExpectedConditions/elementSelectionStateToBe locator selected))

(defn element-to-be-clickable
  [^By locator]
  (ExpectedConditions/elementToBeClickable locator))

(defn element-to-be-selected
  [^By locator]
  (ExpectedConditions/elementToBeSelected locator))

(defn frame-to-be-available-and-switch-to-it
  [^By locator]
  (ExpectedConditions/frameToBeAvailableAndSwitchToIt locator))

(defn invisibility-of-element-located
  [^By locator]
  (ExpectedConditions/invisibilityOfElementLocated locator))

(defn invisibility-of-element-with-text
  [^By locator ^String text]
  (ExpectedConditions/invisibilityOfElementWithText locator text))

(defn number-of-elements-to-be
  [^By locator ^Integer number]
  (ExpectedConditions/numberOfElementsToBe locator number))

(defn number-of-elements-to-be-less-than
  [^By locator ^Integer number]
  (ExpectedConditions/numberOfElementsToBeLessThan locator number))

(defn number-of-elements-to-be-more-than
  [^By locator ^Integer number]
  (ExpectedConditions/numberOfElementsToBeMoreThan locator number))

(defn presence-of-all-elements-located-by
  [^By locator]
  (ExpectedConditions/presenceOfAllElementsLocatedBy locator))

(defn presence-of-element-located
  [^By locator]
  (ExpectedConditions/presenceOfElementLocated locator))

(defn presence-of-nested-element-located-by
  [^By locator ^By child-locator]
  (ExpectedConditions/presenceOfNestedElementLocatedBy locator child-locator))

(defn presence-of-nested-elements-located-by
  [^By locator ^By child-locator]
  (ExpectedConditions/presenceOfNestedElementsLocatedBy locator child-locator))

(defn text-matches
  [^By locator ^java.util.regex.Pattern pattern]
  (ExpectedConditions/textMatches locator pattern))

(defn text-to-be
  [^By locator ^String value]
  (ExpectedConditions/textToBe locator value))

(defn text-to-be-present-in-element-located
  [^By locator ^String text]
  (ExpectedConditions/textToBePresentInElementLocated locator text))

(defn text-to-be-present-in-element-value
  [^By locator ^String text]
  (ExpectedConditions/textToBePresentInElementValue locator text))

(defn visibility-of-all-elements-located-by
  [^By locator]
  (ExpectedConditions/visibilityOfAllElementsLocatedBy locator))

(defn visibility-of-element-located
  [^By locator]
  (ExpectedConditions/visibilityOfElementLocated locator))

(defn visibility-of-nested-elements-located-by
  [^By locator ^By child-locator]
  (ExpectedConditions/visibilityOfNestedElementsLocatedBy locator child-locator))

;; Messages

(defmulti browser-command :msg)

(defmethod browser-command :clean-cookies [{:keys [driver]}]
  (.. driver manage deleteAllCookies))

(defmethod browser-command :go [{:keys [driver url]}]
  (.get driver url))

(defmethod browser-command :wait-for [{:keys [driver condition timeout-seconds]
                                       :or {timeout-seconds 2}}]
  (let [wdw (WebDriverWait. driver timeout-seconds)]
    (.until wdw condition)))

(defmethod browser-command :find-element [{:keys [driver locator]}]
  (.findElement driver locator))

(defmethod browser-command :find-elements [{:keys [driver locator]}]
  (.findElements driver locator))

(defmethod browser-command :children [{:keys [driver locator]}]
  (.findElements driver locator))

(defmethod browser-command :fill [{:keys [driver locator text]}]
  (let [element (.findElement driver locator)]
    (->> [text]
         into-array
         (.sendKeys element))))

(defmethod browser-command :click [{:keys [driver locator]}]
  (-> (.findElement driver locator)
      (.click)))

(defmethod browser-command :attribute [{:keys [driver locator attribute-name]}]
  (-> (.findElement driver locator)
      (.getAttribute attribute-name)))

(defmethod browser-command :text [{:keys [driver locator]}]
  (-> (.findElement driver locator)
      (.getText)))

(defmethod browser-command :title [{:keys [driver]}]
  (.getTitle driver))

(defmethod browser-command :source [{:keys [driver]}]
  (.getPageSource driver))

;; Specs

(defn- web-driver?
  [x]
  (isa? (type x) org.openqa.selenium.remote.RemoteWebDriver))

(defn- condition?
  [x]
  (instance? java.lang.Object x))

(defn- locator?
  [x]
  (instance? org.openqa.selenium.By x))

(sp/def ::driver web-driver?)
(sp/def ::url string?)
(sp/def ::condition condition?)
(sp/def ::timeout-seconds number?)
(sp/def ::locator locator?)
(sp/def ::text string?)
(sp/def ::attribute-name string?)

(defmulti msg-type :msg)

(defmethod msg-type :clean-cookies [_]
  (sp/keys :req-un [::driver]))

(defmethod msg-type :go [_]
  (sp/keys :req-un [::driver ::url]))

(defmethod msg-type :wait-for [_]
  (sp/keys :req-un [::driver ::condition ::timeout-seconds]))

(defmethod msg-type :find-element [_]
  (sp/keys :req-un [::driver ::locator]))

(defmethod msg-type :find-elements [_]
  (sp/keys :req-un [::driver ::locator]))

(defmethod msg-type :children [_]
  (sp/keys :req-un [::driver ::locator]))

(defmethod msg-type :fill [_]
  (sp/keys :req-un [::driver ::locator ::text]))

(defmethod msg-type :click [_]
  (sp/keys :req-un [::driver ::locator]))

(defmethod msg-type :attribute [_]
  (sp/keys :req-un [::driver ::locator ::attribute-name]))

(defmethod msg-type :text [_]
  (sp/keys :req-un [::driver ::locator]))

(defmethod msg-type :title [_]
  (sp/keys :req-un [::driver]))

(defmethod msg-type :source [_]
  (sp/keys :req-un [::driver]))

(sp/def ::msg (sp/multi-spec msg-type :msg))

(defn validate
  "Check if `input` conforms to `::msg`.
  If it conforms, return it. Otherwise, throw an exception."
  [input]
  (let [i (sp/conform ::msg input)]
    (if (= i ::sp/invalid)
      (throw (ex-info "Invalid input" (e/expound ::msg input)))
      i)))
