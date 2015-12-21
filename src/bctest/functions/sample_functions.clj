(ns bctest.functions.sample-functions
  (:require [clojure.string :refer [trim capitalize] :as s]
            [taoensso.timbre :as log]
            [cheshire.core :refer :all]
            [bctest.helper :refer :all]))



;;; Defines functions to be used by the peers. These are located
;;; with fully qualified namespaced keywords, such as
;;; bctest.functions.sample-functions/format-line

(defn format-line [segment]
  ; (log/info segment)
  (update-in segment [:line] trim))

(defn separate-number-data
  [^String line]
  {:id (subs line 0 56) :data (subs line 57)})

(defn parse-line
  [{:keys [line]}]
  (let [{:keys [id data] :as segment} (separate-number-data line)]
      segment
    )

  )

(defn parse-ua
  [^String ua]
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
    (let [record (parse-string data true)
          ip (get-ip record)]
      (-> record
        (assoc :event-id id
          :event-name (-> record :params :_e)
          :dt (.toDate (epoch->date (:epoch record)))
          :ip ip
          :day (get-day (:epoch record))
          :ts (epoch->datetime (:epoch record))
          :cookies (parse-cookies (-> record :headers :cookie))
          )
        (merge (get-country ip))
        (merge (select-keys (:params record) [:_tz :_ul :_sz :_ref :_uid]))
        (merge (parse-ua (-> record :headers :user-agent )))
        (merge (parse-url (-> record :params :url)))
      )
   ))

(defn selector2
  [segment]
  (log/sometimes 0.001 (log/info segment))
  segment
  )

(defn prep-redis-data
  [segment]
  [
    {:op :sadd :args ["events" (:event-id segment) ]}
    {:op :pfadd :args [(s/join ":" ["visits" "domain" (:domain segment)]) (:event-id segment)]}
    {:op :pfadd :args [(s/join ":" ["visits" "day" "domain" (:day segment) (:domain segment)]) (:event-id segment)]}
    ; {:op :pfadd :args [(s/join ":" ["visits" "day" "path" (:day segment) (:path segment)]) (:event-id segment)]}
    {:op :pfadd :args [(s/join ":" ["visits" "day" "browser" (:day segment) (:browser segment)]) (:event-id segment)]}
    {:op :pfadd :args [(s/join ":" ["visits" "day" "os" (:day segment) (:os segment)]) (:event-id segment)]}
    {:op :pfadd :args [(s/join ":" ["visits" "day" "country" (:day segment) (:country segment)]) (:event-id segment)]}
    ]
  )

; (defn upper-case [{:keys [line] :as segment}]
;   (if (seq line)
;     (let [upper-cased (apply str (capitalize (first line)) (rest line))]
;       (assoc-in segment [:line] upper-cased))
;     segment))
