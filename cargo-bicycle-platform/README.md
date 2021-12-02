todo: replication instructions

docker exec camel-demo_kafka_1 kafka-console-consumer --topic bookings --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic error-topic --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic customer-events --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic provider-events --bootstrap-server localhost:29092 --from-beginning
docker exec camel-demo_kafka_1 kafka-console-consumer --topic analytics-events --bootstrap-server localhost:29092 --from-beginning



curl -iX POST http://localhost:8080/booking -d @cargo-bicycle-platform/src/test/resources/ui-request.json --header "Content-Type: application/json"

curl -iX GET http://localhost:8080/customers/42/name
curl -iX GET http://localhost:8080/providers/28/name
curl -iX GET http://localhost:8080/providers/28/offer/496

curl -iX POST http://localhost:8080/booking-v2 -d @cargo-bicycle-platform/src/test/resources/ui-request.json --header "Content-Type: application/json"