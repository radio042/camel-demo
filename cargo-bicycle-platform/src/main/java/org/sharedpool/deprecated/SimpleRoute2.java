package org.sharedpool.deprecated;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.sharedpool.platform.Helper;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class SimpleRoute2 extends RouteBuilder {

    @Override
    public void configure() {
//        from("kafka:in?brokers=localhost:29092")
//                .routeId("simple-route-2")
//                .errorHandler(deadLetterChannel("kafka:errors?brokers=localhost:29092"))
//                .filter(jsonpath("$.[?(@.bringFriends == true)]"))
//                .process(this::messageToFriends)
//                .to("kafka:out?brokers=localhost:29092");
    }

    private void messageToFriends(Exchange exchange) {
        final var body = exchange.getMessage().getBody(Map.class);
        final var inputMessage = body.get("message");
        final var outputMessage = Helper.toJson(Map.of("message", inputMessage));
        exchange.getMessage().setBody(outputMessage);
    }
}
