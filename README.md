# reddit-spark

Parses reddit post messages from a Kafka topic and puts the stemmed
words of the title into Redis.

## Options

`--redis-host HOST` Redis host name

`--redis-port PORT` Redis port

`--zookeeper-connect` ZooKeeper connect string

## Examples

    $ java -jar target/reddit-spark-0.1.0-SNAPSHOT-standalone.jar
           --redis-host localhost
           --redis-port 6379
           --zookeeper-connect localhost:2181
