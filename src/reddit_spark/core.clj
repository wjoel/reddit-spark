(ns reddit-spark.core
  (:require [clojure.string :as string]
            [flambo.conf :as fconf]
            [flambo.api :as f]
            [flambo.tuple :as ft]
            [flambo.function :as ffn]
            [flambo.streaming :as s]
            [taoensso.carmine :as redis :refer [wcar]])
  (:gen-class))

(set! *warn-on-reflection* true)

(def server1-conn {:pool {}
                   :spec {:host "10.0.99.1"
                          :port 6379}})
(defmacro redis* [& body] `(redis/wcar server1-conn ~@body))

(defn read-from-kafka [streaming-context]
  (s/kafka-stream streaming-context "zookeeper.container:2181"
                  "reddit-title-consumer" {"reddit-stream" 1}))

(defn do-stuff []
  (let [spark-config (-> (fconf/spark-conf)
                         (fconf/master "local[4]")
                         (fconf/app-name "reddit-spark"))]
    (redis*
     (with-open [spark-context (f/spark-context spark-config)]
       (let [ssc (s/streaming-context spark-context 1000)]
         (-> (read-from-kafka ssc)
             (s/map (memfn _2))
             (s/flat-map (f/fn [title]
                           (string/split title #" ")))
             (s/map-to-pair (f/fn [w] (ft/tuple w 1)))
             ;; (s/window 1000 2000)
             (s/reduce-by-key (f/fn [x y]
                                (+ x y)))
             (s/foreach-rdd
              (f/fn [rdd time]
                (f/foreach-partition
                 rdd
                 (f/fn [records]
                   (redis*
                    (doseq [word-count (iterator-seq records)]
                      (redis/zincrby "reddit-words"
                                     (._2 word-count)
                                     (._1 word-count)))))))))
         (.start ssc)
         (.awaitTermination ssc)
         (.close ssc))))))

(defn -main [& args]
  (do-stuff))
