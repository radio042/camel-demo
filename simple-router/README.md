todo: replication instructions


docker exec -it camel-demo_kafka_1 kafka-console-producer --topic in --bootstrap-server localhost:29092
docker exec camel-demo_kafka_1 kafka-console-consumer --topic in --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic out --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic errors --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic charlie --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic dave --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic frank --bootstrap-server localhost:29092 --from-beginning

{"message": "Party at my place this Saturday", "bringFriends": true, "bringSnacks": true}

simple:
invite friends -> "dear friend, you are invited to a gaming party"
complicated, template as resource:
invite friends -> ["dear Bob, you are invited...", "dear Charlie, you are invited..."]

more complicated, bring stuff?:
invite friends, buy stuff -> ["dear Bob, you are invited... Bring pizza", "dear Charlie, you are invited... Bring salad"]