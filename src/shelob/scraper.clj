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

(ns shelob.scraper
  "This namespace contains function to parse the source of an HTML page: getting
  elements, getting attributes, selecting elements, selecting children of
  elements, selecting the text of an element."
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Node Element]))

(defn parse
  "Parses `html` string into a Document."
  [^String html]
  (Jsoup/parse html))

(defn all-elements
  "Returns all elements under this element (including self, and children of
  children)."
  [^Element starting-point]
  (.getAllElements starting-point))

(defn element-by-id
  "Returns an element by id."
  [^Element starting-point ^String id]
  (.getElementById starting-point id))

(defn elements-by-attribute
  "Returns all elements that have a named attribute set."
  [^Element starting-point ^String attribute]
  (.getElementsByAttribute starting-point attribute))

(defn elements-by-attribute-value
  "Returns all elements that have an attribute with the specific value."
  [^Element starting-point ^String attribute ^String value]
  (.getElementsByAttributeValue starting-point attribute value))

(defn elements-by-class
  "Returns all elements with matching class."
  [^Element starting-point ^String class-name]
  (.getElementsByClass starting-point class-name))

(defn element-by-tag
  "Returns all elements with the specified tag."
  [^Element starting-point ^String tag-name]
  (.getElementsByTag starting-point tag-name))

(defn select
  "Returns elements that match the CSS query."
  [^Element starting-point ^String css-query]
  (.select starting-point css-query))

(defn select-first
  "Returns the first element that matches the CSS query."
  [^Element starting-point ^String css-query]
  (.selectFirst starting-point css-query))

(defn parent
  "Returns the `element`'s parent or nil if none."
  [^Element element]
  (.parent element))

(defn children
  "Returns the children of an element or an empty list if none."
  [^Element element]
  (.children element))

(defn text
  "Returns the text of an element."
  ([^Element element]
   (text element ""))
  ([^Element element default-return]
   (try
     (.text element)
     (catch Exception _
       default-return))))

(defn attribute
  "Returns the attribute value of an element."
  ([^Node element ^String attribute-name]
   (attribute element attribute-name ""))
  ([^Node element ^String attribute-name default-return]
   (try
     (.attr element attribute-name)
     (catch Exception _
       default-return))))
