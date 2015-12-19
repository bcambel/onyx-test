(ns bctest.jobs.sample-submit-job
  (:require [clojure.java.io :refer [resource]]
            [com.stuartsierra.component :as component]
            [bctest.workflows.sample-workflow :refer [workflow]]
            [bctest.catalogs.sample-catalog :refer [build-catalog] :as sc]
            [bctest.lifecycles.sample-lifecycle :refer [build-lifecycles] :as sl]
            [bctest.flow-conditions.sample-flow-conditions :as sf]
            [bctest.functions.sample-functions]
            [bctest.dev-inputs.sample-input :as dev-inputs]
            [bctest.utils :as u]
            [onyx.api]))

(defn submit-job [dev-env f]
  (let [log-config {:onyx.log/config {:level :trace}}

        dev-cfg (-> "dev-peer-config.edn" resource slurp read-string)
        peer-config (assoc dev-cfg :onyx/id (:onyx-id dev-env))
        peer-config (merge peer-config log-config)
        ;; Turn :read-lines and :write-lines into core.async I/O channels
        stubs [:write-lines]
        ;; Stubs the catalog entries for core.async I/O
        dev-catalog (u/in-memory-catalog (build-catalog 20 50) stubs)
        ;; Stubs the lifecycles for core.async I/O
        dev-lifecycles (u/in-memory-lifecycles (build-lifecycles f) dev-catalog stubs)]
    ;; Automatically pipes the data structure into the channel, attaching :done at the end
    ; (u/bind-inputs! dev-lifecycles {:read-lines dev-inputs/lines})
    (let [job {:workflow workflow
               :catalog dev-catalog
               :lifecycles dev-lifecycles
               :flow-conditions sf/flow-conditions
               :task-scheduler :onyx.task-scheduler/balanced}]
      (onyx.api/submit-job peer-config job)
      ;; Automatically grab output from the stubbed core.async channels,
      ;; returning a vector of the results with data structures representing
      ;; the output.
      (u/collect-outputs! dev-lifecycles [:write-lines]))))
