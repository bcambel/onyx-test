(ns bctest.catalogs.sample-catalog)

;;; Catalogs describe each task in a workflow. We use
;;; them for describing input and output sources, injecting parameters,
;;; and adjusting performance settings.

(defn build-catalog
  ([] (build-catalog 5 50))
  ([batch-size batch-timeout]
     [
     ; {:onyx/name :read-lines
     ;   :onyx/plugin :bctest.plugins.http-reader/reader
     ;   :onyx/type :input
     ;   :onyx/medium :http
     ;   :http/uri "http://textfiles.com/stories/abbey.txt"
     ;   :onyx/batch-size batch-size
     ;   :onyx/batch-timeout batch-timeout
     ;   :onyx/max-peers 1
     ;   :onyx/doc "Reads lines from an HTTP url text file"}

      {:onyx/name :in
       :onyx/plugin :onyx.plugin.seq/input
       :onyx/type :input
       :onyx/medium :seq
       :seq/checkpoint? true
       :onyx/batch-size batch-size
       :onyx/max-peers 1
       :onyx/doc "Reads segments from seq"}

      {:onyx/name :parse-line
       :onyx/fn :bctest.functions.sample-functions/parse-line
       :onyx/type :function
       :onyx/batch-size batch-size
       :onyx/batch-timeout batch-timeout
       :onyx/doc "Strips the line of any leading or trailing whitespace"}

      {:onyx/name :parse-data
       :onyx/fn :bctest.functions.sample-functions/parse-data
       :onyx/type :function
       :onyx/batch-size batch-size
       :onyx/batch-timeout batch-timeout
       :onyx/doc ""}

      {:onyx/name :selector2
       :onyx/fn :bctest.functions.sample-functions/selector2
       :onyx/type :function
       :onyx/batch-size batch-size
       :onyx/batch-timeout batch-timeout
       :onyx/doc ""}

      {:onyx/name :write-lines
       :onyx/plugin :onyx.plugin.core-async/output
       :onyx/type :output
       :onyx/medium :core.async
       :onyx/batch-size batch-size
       :onyx/batch-timeout batch-timeout
       :onyx/max-peers 1
       :onyx/doc "Writes segments to a core.async channel"}]))
