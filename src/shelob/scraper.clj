(ns shelob.scraper
  (:require [clojure.core.async :as as])
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Node Element]))

(defn parse
  "Parse `html` string into a Document."
  [^String html]
  (Jsoup/parse html))

(defn all-elements
  "Return all elements under this element (including self, and children of
  children)."
  [^Element starting-point]
  (.getAllElements starting-point))

(defn element-by-id
  "Return an element by id."
  [^Element starting-point ^String id]
  (.getElementById starting-point id))

(defn elements-by-attribute
  "Return all elements that have a named attribute set."
  [^Element starting-point ^String attribute]
  (.getElementsByAttribute starting-point attribute))

(defn elements-by-attribute-value
  "Return all elements that have an attribute with the specific value."
  [^Element starting-point ^String attribute ^String value]
  (.getElementsByAttributeValue starting-point attribute value))

(defn elements-by-class
  "Return all elements with matching class."
  [^Element starting-point ^String class-name]
  (.getElementsByClass starting-point class-name))

(defn element-by-tag
  "Return all elements with the specified tag."
  [^Element starting-point ^String tag-name]
  (.getElementsByTag starting-point tag-name))

(defn select
  "Return elements that match the CSS query."
  [^Element starting-point ^String css-query]
  (.select starting-point css-query))

(defn select-first
  "Return the first element that matches the CSS query."
  [^Element starting-point ^String css-query]
  (.selectFirst starting-point css-query))

(defn parent
  "Return the `element`'s parent or nil if none."
  [^Element element]
  (.parent element))

(defn children
  "Return the children of an element or an empty list if none."
  [^Element element]
  (.children element))

(defn text
  "Return the text of an element."
  ([^Element element]
   (text element ""))
  ([^Element element default-return]
   (try
     (.text element)
     (catch Exception e
       default-return))))

(defn attribute
  "Return the attribute value of an element."
  ([^Node element ^String attribute-name]
   (attribute element attribute-name ""))
  ([^Node element ^String attribute-name default-return]
   (try
     (.attr element attribute-name)
     (catch Exception e
       default-return))))

(defn scraper-thread
  "Initialize a scraper thread."
  [scraper-fn chan-in chan-out]
  (as/thread
    (loop []
      (when-let [data (as/<!! chan-in)]
        (->> data
             scraper-fn
             (as/>!! chan-out))
        (recur)))))

(defn scraper-pool
  "Initialize a pool of scraper-thread"
  [scraper-fn chan-in chan-out pool-size]
  (dotimes [thread-nr pool-size]
    (println "Start scraper thread " thread-nr)
    (scraper-thread scraper-fn chan-in chan-out)))
