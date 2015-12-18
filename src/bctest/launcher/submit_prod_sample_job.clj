(ns bctest.launcher.submit-prod-sample-job
  (:require [clojure.java.io :refer [resource]]
            [bctest.workflows.sample-workflow :refer [workflow]]
            [bctest.catalogs.sample-catalog :refer [build-catalog]]
            [bctest.lifecycles.sample-lifecycle :as sample-lifecycle]
            [bctest.functions.sample-functions]
            [onyx.plugin.core-async :refer [take-segments!]]
            [onyx.api]))

(defn -main [onyx-id & args]
  (let [cfg (-> "prod-peer-config.edn" resource slurp read-string)
        peer-config (assoc cfg :onyx/id onyx-id)
        lifecycles (sample-lifecycle/build-lifecycles)]
    (let [job {:workflow workflow
               :catalog (build-catalog 20 50)
               :lifecycles lifecycles
               :task-scheduler :onyx.task-scheduler/balanced}]
      (onyx.api/submit-job peer-config job)
      (sample-lifecycle/collect-outputs! lifecycles  [:write-lines])
      (shutdown-agents))))
