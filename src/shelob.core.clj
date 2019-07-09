(ns shelob.core
  (:import [com.gargoylesoftware.htmlunit WebClient BrowserVersion Page]
           [com.gargoylesoftware.htmlunit.html HtmlElement HtmlInput]))

(def firefox BrowserVersion/FIREFOX_60)
(def explorer BrowserVersion/INTERNET_EXPLORER)
(def chrome BrowserVersion/CHROME)

(defn web-client
  "Return a new WebClient instance."
  ([]
   (web-client (BrowserVersion/getDefault)))
  ([browser-version]
   (WebClient. browser-version)))

(defn page
  [client url]
  (.getPage client url))

(defn element-by-name
  [document element-name]
  (.getElementByName document element-name))

(defn elements-by-xpath
  [document xpath]
  (.getByXPath document xpath))

(defn ^Page type-value
  [^HtmlInput input value]
  (.type input value))

(defn click-on
  [^HtmlElement el]
  (.click el))
