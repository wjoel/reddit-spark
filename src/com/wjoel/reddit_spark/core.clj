(ns com.wjoel.reddit-spark.core
  (:require [clojure.string :as string]
            [flambo.conf :as fconf]
            [flambo.api :as f]
            [flambo.streaming :as s]
            [taoensso.carmine :as redis :refer [wcar]]
            [com.wjoel.reddit-spark.arguments :as args]
            [com.wjoel.reddit-spark.stemmer :as stemmer])
  (:gen-class))

(set! *warn-on-reflection* true)

(defonce spark-config (-> (fconf/spark-conf)
                          (fconf/master "local[4]")
                          (fconf/app-name "reddit-spark")))

(defn read-from-kafka [streaming-context zookeeper-connect]
  (s/kafka-stream streaming-context zookeeper-connect
                  "reddit-title-consumer" {"reddit-stream" 1}))

(defn store-word-counts-in-redis! [redis-conn rdd]
  (redis/wcar redis-conn
              (doseq [word-count (iterator-seq rdd)]
                (redis/zincrby "reddit-words"
                               (._2 word-count)
                               (._1 word-count)))))

(defn process-stream [zookeeper-connect redis-conn]
  (with-open [spark-context (f/spark-context spark-config)]
    (let [ssc (s/streaming-context spark-context 100)]
      (-> (read-from-kafka ssc zookeeper-connect)
          (s/map (memfn _2))
          (s/map :title)
          (s/flat-map stemmer/stem-string)
          (.countByValue)
          (s/foreach-rdd
           (fn [rdd time]
             (f/foreach-partition rdd #(store-word-counts-in-redis! redis-conn %)))))
      (.start ssc)
      (.awaitTermination ssc)
      (.close ssc))))

(defn -main [& args]
  (let [{:keys [options
                errors]} (args/parse-opts args)
        redis-conn {:pool {}
                    :spec {:host (:redis-host options)
                           :port (:redis-port options)}}]

    (when-not (empty? errors)
      (println "Errors:" (string/join \newline errors))
      (System/exit 1))

    (process-stream (:zookeeper-connect options) redis-conn)))
