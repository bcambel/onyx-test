(ns bctest.workflows.sample-workflow)

;;; The workflow of an Onyx job describes the graph of all possible
;;; tasks that data can flow between.

(def workflow
  [[:in :parse-line]

   [:parse-line :parse-data]
   [:parse-data :selector2]
   [:selector2 :write-lines]
   [:selector2 :prep-redis-data]
   [:prep-redis-data :out-to-redis]

   ])
