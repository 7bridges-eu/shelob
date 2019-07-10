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
  "Return the required page as a Page object."
  [client url]
  (.getPage client url))

(defn element-by-id
  "Return the element with the required id attribute."
  [document element-id]
  (.getElementById document element-id))

(defn element-by-name
  "Return the element with the required name attribute."
  [document element-name]
  (.getElementByName document element-name))

(defn elements-by-attribute
  "Return a vector of elements that match the required `attribute` `value`.
  The query starts from `starting-element`. If `starting-element` is a whole
  page, the starting point is the root element."
  [starting-element tag-name attribute value]
  (if (instance? HtmlElement starting-element)
    (.getElementsByAttribute starting-element
                             tag-name
                             attribute
                             value)
    (.getElementsByAttribute (.getDocumentElement starting-element)
                             tag-name
                             attribute
                             value)))

(defn elements-by-xpath
  "Return a vector of elements that satisfies a XPath query."
  [document xpath]
  (.getByXPath document xpath))

(defn ^Page type-value
  "Type a `value` in the `input` element."
  [^HtmlInput input value]
  (.type input value))

(defn click-on
  "Simulate a click on `element`."
  [^HtmlElement element]
  (.click element))
