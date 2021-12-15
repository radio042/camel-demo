# Simple Router Demo

### Requirements
- recent Docker version
- recent Java version

### start the infrastructure and the service
- `docker compose up -d`
- `mvn clean compile quarkus:dev`

### send a valid request
- `docker exec simple-router_kafka_1 kafka-console-consumer --topic bookings --bootstrap-server localhost:29092 --from-beginning`
- `curl -iX POST http://localhost:8080/booking -d @src/test/resources/ui-request.json --header "Content-Type: application/json"`

### send an invalid request
- `docker exec simple-router_kafka_1 kafka-console-consumer --topic error-topic --bootstrap-server localhost:29092 --from-beginning`
- in another console tab: `curl -iX POST http://localhost:8080/booking -d @src/test/resources/ui-broken-request.json --header "Content-Type: application/json"`

### clean up containers
- `docker compose down`