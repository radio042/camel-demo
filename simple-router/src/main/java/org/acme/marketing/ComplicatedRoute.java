package org.acme.marketing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class ComplicatedRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("kafka:marketing-topic?brokers=localhost:29092")
                .routeId("complicated-route")
                .errorHandler(deadLetterChannel("kafka:error-topic?brokers=localhost:29092"))
                .filter(jsonpath("$.[?(@.audience == 'marketing channels')]"))
                .pollEnrich()
                .simple("file:classes?noop=true&idempotent=false&fileName=image-uri")
                .aggregationStrategy(this::appendHeaders)
                .to("kafka:twitter-service?brokers=localhost:29092")
                .to("kafka:facebook-service?brokers=localhost:29092")
                .to("kafka:instagram-service?brokers=localhost:29092");
    }

    private AggregationStrategy appendHeaders() {
        return (originalExchange, enrichmentExchange) -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                var queryResult = objectMapper.readValue(enrichmentExchange.getMessage().getBody(String.class), Map.class);
                originalExchange.getMessage().setHeaders(Map.of(
                        "hi-res", queryResult.get("hi-res"),
                        "low-res", queryResult.get("low-res")));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return enrichmentExchange;
        };
    }
}
