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

(defn set-web-client-options
  "Set some options on `web-client`.
  It's possible to configure: `javascript-enabled`, `css-enabled`, and
  `use-insecure-ssl`."
  [web-client {:keys [javascript-enabled css-enabled use-insecure-ssl]
               :or {javascript-enabled true css-enabled true use-insecure-ssl false}}]
  (.. web-client (getOptions) (setJavaScriptEnabled javascript-enabled))
  (.. web-client (getOptions) (setCssEnabled css-enabled))
  (.. web-client (getOptions) (setUseInsecureSSL javascript-enabled))
  web-client)

(defn page
  "Return the required page as a Page object."
  [client url]
  (.getPage client url))

(defn anchor-by-href
  "Return the `HtmlAnchor` on `document` with `href`."
  [document href]
  (.getAnchorByHref document href))

(defn anchor-by-name
  "Return the `HtmlAnchor` on `document` with `anchor-name`."
  [document anchor-name]
  (.getAnchorByName document anchor-name))

(defn anchor-by-text
  "Return the `HtmlAnchor` on `document` with `text`."
  [document text]
  (.getAnchorByText document text))

(defn anchors
  "Return a list of `HtmlAnchor` from `document`."
  [document]
  (.getAnchors document))

(defn base-url
  "Get the base URL for `document`."
  [document]
  (.getBaseURL document))

(defn body
  "Get the `HtmlElement` representing the body of `document`."
  [document]
  (.getBody document))

(defn charset
  "Get the `Charset` of `document`."
  [document]
  (.getCharset document))

(defn content-type
  "Get the content-type of `document`."
  [document]
  (.getContentType document))

(defn document-element
  "Get the `HtmlElement` representing the document element of `document`."
  [document]
  (.getDocumentElement document))

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

(defn elements-by-id
  "Return a list of `DomElement` in `document` with `id`."
  [document id]
  (.getElementsById document id))

(defn elements-by-id-and-or-name
  "Return a list of `DomElement` in `document` with id or name equals to `id`."
  [document id]
  (.getElementsByIdAndOrName document id))

(defn elements-by-name
  "Return a list of `DomElement` in `document` with `el-name`."
  [document el-name]
  (.getElementsByName document el-name))

(defn elements-by-xpath
  "Return a vector of elements that satisfies a XPath query."
  [document xpath]
  (.getByXPath document xpath))

(defn focused-element
  "Return the `DomElement` representing the element with focus.
  Return nil otherwise."
  [document]
  (.getFocusedElement document))

(defn form-by-name
  "Return the `HtmlForm` representing the form in `document` with `form-name`."
  [document form-name]
  (.getFormByName document form-name))

(defn forms
  "Return a list of `HtmlForms` elements from `document`."
  [document]
  (.getForms document))

(defn frame-by-name
  "Return the `FrameWindow` in document identified by `frame-name`."
  [document frame-name]
  (.getFrameByName document frame-name))

(defn frames
  "Return a list of `FrameWindow` elements from `document`."
  [document]
  (.getFrames document))

(defn fully-qualified-url
  "Return a fully qualified url for `relative-url` based on the URL used to load
  `document`."
  [document relative-url]
  (.getFullyQualifiedUrl document relative-url))

(defn head
  "Return the head element of `document`."
  [document]
  (.getHead document))

(defn html-element-by-access-key
  "Return the `HtmlElement` in `document` assigned to `access-key`."
  [document access-key]
  (.getHtmlElementByAccessKey access-key))

(defn html-element-by-id
  "Return the `HtmlElement` in `document` with the specified `id`."
  [document id]
  (.getHtmlElementById document id))

(defn html-elements-by-access-key
  "Return a list of `HtmlElement` in `document` assigned to `access-key`."
  [document access-key]
  (.getHtmlElementsByAccessKey access-key))

(defn meta-tags
  "Return a list of `HtmlMeta` tags for a http-equiv `value`."
  [document value]
  (.getMetaTags document value))

(defn namespaces
  "Return all the namespaces defined at the root of `document`."
  [document]
  (.getNamespaces document))

(defn title-text
  "Return the title of `document` or an empty string if not specified."
  [document]
  (.getTitleText document))

(defn ^Page type-value
  "Type a `value` in the `input` element."
  [^HtmlInput input value]
  (.type input value))

(defn click-on
  "Simulate a click on `element`."
  [^HtmlElement element]
  (.click element))
