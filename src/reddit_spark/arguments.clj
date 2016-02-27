(ns reddit-spark.arguments
  (:require [clojure.tools.cli :as cli]))

(def cli-options
  [["-r" "--redis-host HOST" "Redis host"
    :default "localhost"]
   ["-p" "--redis-port PORT" "Redis port"
    :default 6379
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 65536) "Must be a number between 0 and 65536"]]
   ["-z" "--zookeeper-connect" "Zookeeper connect string, comma separated list of host:port"
    :default "localhost:2181"]])

(defn parse-opts [args]
  (cli/parse-opts args cli-options))
