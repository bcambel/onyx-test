(ns bctest.functions.sample-functions
  (:require [clojure.string :refer [trim capitalize]]
            [taoensso.timbre :as log]
            [cheshire.core :refer :all]))

;;; Defines functions to be used by the peers. These are located
;;; with fully qualified namespaced keywords, such as
;;; bctest.functions.sample-functions/format-line

(defn format-line [segment]
  (log/info segment)
  (update-in segment [:line] trim))

(defn separate-number-data
  [line]
  {:id (subs line 0 56) :data (subs line 57)})

(defn parse-line
  [{:keys [line]}]
  (let [{:keys [id data] :as segment} (separate-number-data line)]
      segment
    )

  )

(defn parse-data
  [{:keys [id data] :as segment}]
    (let [record (parse-string data true)]
      (assoc record :event-id id )
      )
   )

(defn selector2
  [segment]
  segment
  )

; (defn upper-case [{:keys [line] :as segment}]
;   (if (seq line)
;     (let [upper-cased (apply str (capitalize (first line)) (rest line))]
;       (assoc-in segment [:line] upper-cased))
;     segment))
