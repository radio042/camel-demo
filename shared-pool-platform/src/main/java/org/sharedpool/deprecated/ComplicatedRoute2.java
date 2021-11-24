package org.sharedpool.deprecated;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.sharedpool.platform.Helper;

import javax.enterprise.context.ApplicationScoped;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class ComplicatedRoute2 extends RouteBuilder {

    @Override
    public void configure() {
//        from("kafka:in?brokers=localhost:29092")
//                .routeId("complicated-route-2")
//                .filter().jsonpath("$.[?(@.bringFriends == true)]")
//                .pollEnrich()
//                .simple("file:classes?noop=true&idempotent=false&fileName=snacks.txt")
//                .aggregationStrategy(this::contentAs3OrdersHeader)
//                .multicast().to("direct:bob", "direct:charlie", "direct:dave");
//        from("direct:bob")
//                .process(exchange -> messageToFriendsIncludingOrder(exchange, 0))
//                .log("final message a: ${body}")
//                .to("kafka:bob?brokers=localhost:29092");
//        from("direct:charlie")
//                .process(exchange -> messageToFriendsIncludingOrder(exchange, 1))
//                .log("final message b: ${body}")
//                .to("kafka:charlie?brokers=localhost:29092");
//        from("direct:dave")
//                .process(exchange -> messageToFriendsIncludingOrder(exchange, 2))
//                .log("final message c: ${body}")
//                .to("kafka:dave?brokers=localhost:29092");
    }

    private Exchange contentAs3OrdersHeader(Exchange originalExchange, Exchange enrichmentExchange) {
        String[] snacks = enrichmentExchange.getMessage().getBody(String.class).split(",");
        originalExchange.getMessage().setHeader("orders", distributeToNOrders(snacks, 3));
        return originalExchange;
    }

    private Map<Integer, String> distributeToNOrders(String[] items, int n) {
        return IntStream.range(0, items.length)
                .mapToObj(i -> new AbstractMap.SimpleEntry<>(i, items[i]))
                .collect(Collectors.toMap(
                        entry -> entry.getKey() % n,
                        AbstractMap.SimpleEntry::getValue,
                        (item1, item2) -> String.format("%s, %s", item1, item2)
                ));
    }

    private void messageToFriendsIncludingOrder(Exchange exchange, int orderNumber) {
        String body = exchange.getMessage().getBody(String.class);
        Map<?, ?> orders = exchange.getMessage().getHeader("orders", Map.class);
        String messageToBob = String.format(
                "%s.%s",
                Helper.toMap(body).get("message"),
                orders.get(orderNumber) != null ? String.format(" Bring %s.", orders.get(orderNumber)) : ""
        );
        exchange.getMessage().setBody(messageToBob);
    }
}
