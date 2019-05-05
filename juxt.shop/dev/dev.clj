(ns dev
  (:require
    [dev-extras :refer :all]))

;; Add your helpers here
(defn crux [] (:juxt.crux.ig.system/standalone system))
