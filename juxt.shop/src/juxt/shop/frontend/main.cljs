(ns ^:figwheel-hooks juxt.shop.frontend.main
  )

(js/console.log "JUXT Shop - frontend loaded")

(def render js/ReactDOM.render)
(def Component js/React.Component)
(def React js/React)
(def Form (.-default js/JSONSchemaForm))
(def e js/React.createElement)

(defn log
  [type]
  (js/console.log.bind js/console type))

(defn submit
  [stuff]
  (js/console.log "Submit")
  (js/fetch
   "/api/products"
   #js {:method "POST"
        :body (js/JSON.stringify stuff)
        :headers
        #js {"Content-Type" "application/json"}}))

(defn product-form
  [schema]
  (clj->js
   {:schema schema
    :onChange (log "changed")
    :onSubmit submit
    :onError (log "errors")}))

(defn mount
  []
  (let [app (js/document.getElementById "app")]
    (-> (js/fetch "/api/products"
                  #js {:method "GET"
                       :headers #js {"Accept" "application/schema+json"}})
        (.then #(.json %))
        (.then #(do
                  (js/console.log "rendering" %)
                  (render
                   (e Form (product-form %) nil)
                   app))))
    true))

;; This is called once
(defonce init
  (mount))

;; This is called every time you make a code change
(defn ^:after-load reload []
  (mount))
