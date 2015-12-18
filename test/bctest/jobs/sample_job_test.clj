(ns bctest.jobs.sample-job-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.core.async :refer [>!!]]
            [clojure.java.io :refer [resource]]
            [com.stuartsierra.component :as component]
            [bctest.launcher.dev-system :refer [onyx-dev-env]]
            [bctest.workflows.sample-workflow :refer [workflow]]
            [bctest.catalogs.sample-catalog :refer [build-catalog] :as sc]
            [bctest.lifecycles.sample-lifecycle :refer [build-lifecycles] :as sl]
            [bctest.plugins.http-reader]
            [bctest.functions.sample-functions]
            [bctest.dev-inputs.sample-input :as dev-inputs]
            [bctest.utils :as u]
            [onyx.api]))

(deftest test-sample-dev-job
  (try
    (let [stubs [:read-lines :write-lines]
          catalog (u/in-memory-catalog (build-catalog) stubs)
          lifecycles (u/in-memory-lifecycles (build-lifecycles) catalog stubs)]
      (user/go (u/n-peers catalog workflow))
      (u/bind-inputs! lifecycles {:read-lines dev-inputs/lines})
      (let [peer-config (u/load-peer-config (:onyx-id user/system))
            job {:workflow workflow
                 :catalog catalog
                 :lifecycles lifecycles
                 :task-scheduler :onyx.task-scheduler/balanced}]
        (onyx.api/submit-job peer-config job)
        (let [[results] (u/collect-outputs! lifecycles [:write-lines])]
          (is (seq results)))))
    (catch InterruptedException e
      (Thread/interrupted))
    (finally
     (user/stop))))

(deftest test-sample-prod-job
  (try
    (let [catalog (build-catalog 20 500)
          lifecycles (build-lifecycles)]
      (user/go (u/n-peers catalog workflow))
      (u/bind-inputs! lifecycles {:read-lines dev-inputs/lines})
      (let [peer-config (u/load-peer-config (:onyx-id user/system))
            job {:workflow workflow
                 :catalog catalog
                 :lifecycles lifecycles
                 :task-scheduler :onyx.task-scheduler/balanced}]
        (onyx.api/submit-job peer-config job)
        (let [[results] (u/collect-outputs! lifecycles [:write-lines])]
          (is (seq results)))))
    (catch InterruptedException e
      (Thread/interrupted))
    (finally
     (user/stop))))
