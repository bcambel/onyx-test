(ns bctest.functions.sample-functions
  (:require [clojure.string :refer [trim capitalize] :as s]
            [taoensso.timbre :as log]
            [cheshire.core :refer :all]
            [bctest.helper :refer :all]))



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

(defn parse-ua
  [ua]
  (str->features ua)
  )


(defn get-ip
  [dict]
  (when-let [x-for-ips (get (:headers dict) :x-forwarded-for)]
    (let [ip-candidates (s/split x-for-ips #",")
          ip-first-pick (first ip-candidates)]
    (if-not (empty? ip-first-pick)
      ip-first-pick
      (last (:ip dict))))))



(defn parse-data
  [{:keys [id data] :as segment}]
    (let [record (parse-string data true)]
      (-> record
        (assoc :event-id id
          :event-name (-> record :params :_e)
          :ip (get-ip record)
          :day (get-day (:epoch record))
          :ts (epoch->datetime (:epoch record))
          :cookies (parse-cookies (-> record :headers :cookie))
          )
        (merge (select-keys (:params record) [:_tz :_ul :_sz :_ref :_uid]))
        (merge (parse-ua (-> record :headers :user-agent )))
        (merge (parse-url (-> record :params :url)))
      )
   ))

(defn selector2
  [segment]
  segment
  )

(defn prep-redis-data
  [segment]
  [
    {:op :sadd :args ["events" (:event-id segment) ]}
    {:op :pfadd :args ["events_hll" (:event-id segment)]}
    ]
  )

; (defn upper-case [{:keys [line] :as segment}]
;   (if (seq line)
;     (let [upper-cased (apply str (capitalize (first line)) (rest line))]
;       (assoc-in segment [:line] upper-cased))
;     segment))
