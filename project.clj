(defproject com.joelwilsson/reddit-spark "0.1.0-SNAPSHOT"
  :description "Consume reddit titles from Kafka with Spark"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [yieldbot/flambo "0.7.1"]
                 [clj-kafka "0.3.3"]
                 [org.apache.spark/spark-streaming_2.10 "1.5.2"]
                 [org.apache.spark/spark-streaming-flume_2.10 "1.5.2"]
                 [org.apache.spark/spark-streaming-kafka_2.10 "1.5.2"]
                 [com.taoensso/carmine "2.12.0"]
                 [org.apache.lucene/lucene-core "5.3.1"]
                 [org.apache.lucene/lucene-analyzers-common "5.3.1"]]
  :main reddit-spark.core
  :profiles {:provided {:dependencies [[org.apache.spark/spark-core_2.10 "1.5.2"]
                                       [org.apache.hadoop/hadoop-common "2.6.0"]]}
             :dev {:aot [reddit-spark.core]}
             :uberjar {:aot :all}})
