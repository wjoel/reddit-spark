(ns reddit-spark.core
  (:require [clojure.string :as string]
            [reddit-spark.stemmer :as stemmer]
            [flambo.conf :as fconf]
            [flambo.api :as f]
            [flambo.streaming :as s]
            [taoensso.carmine :as redis :refer [wcar]])
  (:gen-class))

(set! *warn-on-reflection* true)

(def server1-conn {:pool {}
                   :spec {:host "10.0.99.1"
                          :port 6379}})
(defmacro redis* [& body] `(redis/wcar server1-conn ~@body))

(defonce spark-config (-> (fconf/spark-conf)
                          (fconf/master "local[4]")
                          (fconf/app-name "reddit-spark")))

(defn read-from-kafka [streaming-context]
  (s/kafka-stream streaming-context "zookeeper.container:2181"
                  "reddit-title-consumer" {"reddit-stream" 1}))

(defn store-word-counts-in-redis! [rdd]
  (redis*
   (doseq [word-count (iterator-seq rdd)]
     (redis/zincrby "reddit-words"
                    (._2 word-count)
                    (._1 word-count)))))

(defn do-stuff []
  (with-open [spark-context (f/spark-context spark-config)]
    (let [ssc (s/streaming-context spark-context 100)]
      (-> (read-from-kafka ssc)
          (s/map (memfn _2))
          (s/flat-map stemmer/stem-string)
          (.countByValue)
          (s/foreach-rdd
           (fn [rdd time]
             (f/foreach-partition rdd store-word-counts-in-redis!))))
      (.start ssc)
      (.awaitTermination ssc 60000)
      (.close ssc))))

(defn -main [& args]
  (do-stuff))
