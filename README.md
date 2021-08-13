# camel-demo

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## Kafka integration, copying messages from foo to bar

1. `docker-compose up -d`
1. `./mvnw compile quarkus:dev`
1. `docker exec camel-demo_kafka_1 kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic foo`
1. `docker exec camel-demo_kafka_1 kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic bar`  
1. `docker exec -it camel-demo_kafka_1 kafka-console-producer --topic foo --bootstrap-server localhost:29092`
   `> example message`
1. `docker exec camel-demo_kafka_1 kafka-console-consumer --topic bar --bootstrap-server localhost:29092 --from-beginning`
1. `docker-compose down`

### also useful
- `docker exec camel-demo_kafka_1 kafka-topics --help`
- `docker exec camel-demo_kafka_1 kafka-topics --list --bootstrap-server localhost:29092`
- `docker exec camel-demo_kafka_1 kafka-topics --describe --topic foo --bootstrap-server localhost:29092`
