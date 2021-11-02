todo: replication instructions


docker exec -it camel-demo_kafka_1 kafka-console-producer --topic in --bootstrap-server localhost:29092
docker exec camel-demo_kafka_1 kafka-console-consumer --topic in --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic out --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic errors --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic charlie --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic dave --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic frank --bootstrap-server localhost:29092 --from-beginning

{"message": "Party at my place this Saturday", "bringFriends": true, "bringSnacks": true}

- body to header, split, aggregate to 3 messages, header to body -> aggregation complicated
- split in method, build 3 exchanges - java intensive
- process and send sequentially