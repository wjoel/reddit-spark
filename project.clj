(defproject com.joelwilsson/reddit-spark "0.1.0-SNAPSHOT"
  :description "Consume reddit titles from Kafka with Spark"
  :url "https://github.com/wjoel/reddit-spark"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [yieldbot/flambo "0.7.1"]
                 [clj-kafka "0.3.3"]
                 [org.apache.spark/spark-streaming_2.11 "1.6.0"]
                 [org.apache.spark/spark-streaming-flume_2.11 "1.6.0"]
                 [org.apache.spark/spark-streaming-kafka_2.11 "1.6.0"]
                 [com.taoensso/carmine "2.12.0"]
                 [org.apache.lucene/lucene-core "5.3.1"]
                 [org.apache.lucene/lucene-analyzers-common "5.3.1"]]
  :main ^:skip-aot com.wjoel.reddit-spark.core
  :uberjar-merge-with {"reference.conf" [slurp str spit]}
  :profiles {:provided {:dependencies [[org.apache.spark/spark-core_2.11 "1.6.0"]
                                       [org.apache.hadoop/hadoop-common "2.6.0"]]}
             :dev {:aot [reddit-spark.core]}
             :uberjar {:aot :all}})
