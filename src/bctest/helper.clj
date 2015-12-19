(ns bctest.helper
  (:require [clojure.string :as s]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :refer [from-long]]
            [clojurewerkz.urly.core :refer [url-like host-of path-of query-of as-map]]
            [taoensso.timbre :as log]
            )
  (:import  [java.net URLDecoder URLEncoder]
              [eu.bitwalker.useragentutils UserAgent DeviceType Browser OperatingSystem]))

(def custom-formatter (f/formatter "yyyy-MM-dd"))
(def date-format (f/formatter "yyyy-MM-dd"))
(def datetime-formatter (f/formatter "yyyy-MM-dd'T'HH:mm:ss"))

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

(defn select-values
  "clojure.core contains select-keys
  but not select-values."
  [m ks]
  (reduce
    #(if-let [v (m %2)]
        (conj %1 v) %1)
    [] ks))

(defn str->features
  [string]
  (try
    (let [user-agent (UserAgent. (or string ""))]
      {:browser (-> user-agent .getBrowser .getGroup .getName)
       :os (-> user-agent .getOperatingSystem .getGroup .getName)
       :device (-> user-agent .getOperatingSystem .getDeviceType .getName)})
    (catch Exception e
      (log/error (str "Could not derive the user-agent from " string) e)
      (str->features nil)
      )))



(defn get-query-parameters*
  [url]
  (let [params (:query (as-map (or url "")))]
    (if (nil? params)
      {}
      (apply merge (map (fn[[k v]]
                          (hash-map (keyword k) v))
                    (map #(-> % (.split "=") vec )
        (vec (.split params "&")))))
      )))

(defn get-query-parameters
  [url]
  (try
    (get-query-parameters* url)
    (catch Throwable t
      (do
        (log/sometimes 0.001
          (log/warn "Query params parsing failed" url))
        {}) )))

(defn epoch->date
  [epoch]
  (try
    (-> (Long/parseLong epoch)
        (from-long))
    (catch Throwable t
      (do
        (log/error "[" epoch "] failed to parse")
        (t/now)
      ))))

(defn get-day
  [epoch]
  (->> (epoch->date epoch)
       (f/unparse custom-formatter)))

(defn epoch->datetime
  [epoch]
  (->> (epoch->date epoch)
       (f/unparse datetime-formatter)))

(defn date->str
  [date]
  (f/unparse date-format date))

(defn str->date
  [date]
  (f/parse date-format date))


(defn get-hour
  "Expects a timestamp in milliseconds
  Returns the starting second(!) of the hour.
  Ex: Passing 1437609280911 (Wed, 22 Jul 2015 23:54:40 GMT)
  1437606000 (GMT: Wed, 22 Jul 2015 23:00:00 GMT)
  "
  [epoch]
  (let [epoch (Long/parseLong epoch)
        date (from-long epoch)]
    ; remove extra minute and seconds
  (- (int (Math/floor (/ epoch 1000)))
    (+ (* (.getMinuteOfHour date) 60)
       (.getSecondOfMinute date)))))

(defn check-path
  "If given path is has a special format URL;
  removes the unnecessary parts(repeating) from the url"
  [p]
  (if (.startsWith (or p "") "/booking/")
    (str "/" (s/join "/" (take 3 (rest (.split p "/")))))
    p))


(defn get-path
  [url]
  (let [p (if (nil? url) "" (path-of url))]
    (if (empty? p)
      p
      (check-path p))))

(defn parse-url
  [url]
  (let [full-url (if (nil? url) "" (if (.startsWith url "http://") url (str "http://" url)))
        url-refer (url-like (or full-url ""))
        domain (if (nil? url-refer) "" (host-of url-refer))
        path (get-path url-refer)
        query-parameters (-> (get-query-parameters url)
                              ((fn[x]
                                (assoc x :q (s/lower-case (or (get x :q) ""))))))]
    {:url url-refer
     :domain domain
     :path path
     :query query-parameters
     }
    )
  )

(defn parse-cookies
  "Returns a map of cookies when given the Set-Cookie string sent
  by a server."
  [#^String cookie-string]
  (when cookie-string
    (into {}
      (for [#^String cookie (.split cookie-string ";")]
        (let [keyval (map (fn [#^String x] (.trim x)) (.split cookie "=" 2))]
          [(first keyval) (second keyval)])))))
