(ns bctest.workflows.sample-workflow)

;;; The workflow of an Onyx job describes the graph of all possible
;;; tasks that data can flow between.

(def workflow
  [[:in :parse-line]

   [:parse-line :parse-data]
   ; [:parse-data :write-lines]
   [:parse-data :selector2]
   [:selector2 :write-lines]

   ])
