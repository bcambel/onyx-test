(ns bctest.lifecycles.sample-lifecycle
  (:require [clojure.core.async :refer [chan sliding-buffer >!!]]
            [onyx.plugin.core-async :refer [take-segments!]]
            [onyx.plugin.seq]
            [taoensso.timbre :refer [info]]
            [onyx.static.planning :refer [find-task]]
            [bctest.utils :as u])
  (:import [java.io BufferedReader FileReader]))

(defn log-batch [event lifecycle]
  (let [task-name (:onyx/name (:onyx.core/task-map event))]
    (doseq [m (map :message (mapcat :leaves (:tree (:onyx.core/results event))))]
      (info task-name " logging segment: " m)))
  {})

(def log-calls
  {:lifecycle/after-batch log-batch})

(def input-channel-capacity 10000)

(def output-channel-capacity (inc input-channel-capacity))

(defn channel-id-for [lifecycles task-name]
  (:core.async/id
    (->> lifecycles
         (filter #(= task-name (:lifecycle/task %)))
         (first))))

(def get-output-channel
  (memoize
    (fn [id]
      (chan (sliding-buffer output-channel-capacity)))))

(defn collect-outputs! [lifecycles output-tasks]
  (->> output-tasks
       (map #(get-output-channel (channel-id-for lifecycles %)))
       (map take-segments!)
       (zipmap output-tasks)))

(defn inject-in-reader [event lifecycle]
  (let [rdr (FileReader. (:buffered-reader/filename lifecycle))]
    {:seq/rdr rdr
     :seq/seq (map (partial hash-map :line) (line-seq (BufferedReader. rdr)))}))

(defn close-reader [event lifecycle]
  (.close (:seq/rdr event)))

(def in-calls
  {:lifecycle/before-task-start inject-in-reader
   :lifecycle/after-task-stop close-reader})

(defn build-lifecycles [f]
  [
    {:lifecycle/task :in
    :buffered-reader/filename f
    :lifecycle/calls ::in-calls}

    {:lifecycle/task :in
    :lifecycle/calls :onyx.plugin.seq/reader-calls}

    ; {:lifecycle/task :read-lines
    ; :lifecycle/calls :bctest.plugins.http-reader/reader-calls
    ; :lifecycle/replaceable? true
    ; :lifecycle/doc "Lifecycle for reading from a core.async chan"}

   {:lifecycle/task :write-lines
    :lifecycle/calls :bctest.utils/out-calls
    :core.async/id (java.util.UUID/randomUUID)
    :lifecycle/replaceable? true
    :lifecycle/doc "Lifecycle for your output task. When using in-memory-lifecycles, this will be replaced"}

   {:lifecycle/task :write-lines
    :lifecycle/calls :onyx.plugin.core-async/writer-calls
    :lifecycle/doc "Lifecycle for injecting a core.async writer chan"}

   {:lifecycle/task :write-lines
    :lifecycle/calls ::log-calls
    :lifecycle/doc "Lifecycle for printing the output of a task's batch"}])
