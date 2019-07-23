(ns shelob.conditions
  (:import
   [org.openqa.selenium By]
   [org.openqa.selenium.support.ui ExpectedConditions]))

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
