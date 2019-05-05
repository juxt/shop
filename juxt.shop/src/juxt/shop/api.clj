(ns juxt.shop.api
  (:require
   [yada.yada :as yada]
   crux.api
   [integrant.core :as ig]
   [json-schema.core :as jsonschema]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]
   [clojure.edn :as edn]))

(def $schema "http://json-schema.org/draft-07/schema#")

(defn jsonschema [path]
  (-> (format "juxt/shop/schema/%s.edn" path)
      io/resource slurp edn/read-string
      (assoc "$schema" $schema)
      (assoc "$id" (format "https://juxt.shop/schemas/%s" path))))

(defn schema-path [k]
  (let [endpoint-type (second k)]
    (assert (= (namespace endpoint-type)
               "juxt.shop.api"))
    (name endpoint-type)))

(defn get-response [k system ctx]
  (case (yada/content-type ctx)
    "application/json"
    (sequence
     (crux.api/q
      (crux.api/db system)
      '{:find [id title amount]
        :where [[e :crux.db/id id]
                [e :title title]
                [e :amount amount]]}))
    "application/schema+json" (json/encode (jsonschema (schema-path k)))))

(defn put [system form]
  (let [id (java.util.UUID/randomUUID)]
    (-> (crux.api/submit-tx
         system
         [[:crux.tx/put id
           (merge form {:crux.db/id id})]])
        (merge {:crux.db/id id}))))

(defn add-product
  [system ctx]
  (let [data (-> ctx :body :formData)]
    (jsonschema/validate (jsonschema "products") data)
    (put system data)))

;; TODO: This endpoint could become parameterized
(defmethod ig/init-key ::api
  [k {:keys [system]}]
  (yada/resource
   {:methods
    {:get
     {:produces #{"application/json"
                  "application/schema+json;q=0.8"}
      :response #(get-response k system %)}
     :post
     {:consumes "application/json"
      :produces "application/json"
      :response #(add-product system %)}}}))
