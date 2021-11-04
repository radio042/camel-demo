package org.acme.marketing;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.acme.marketing.Helper.toJson;
import static org.acme.marketing.Helper.toMap;

public class Test3 extends CamelTestSupport {

    @Test
    public void test() throws Exception {
        AdviceWith.adviceWith(context, "complicated-route-2", a -> {
            a.replaceFromWith("direct:start");
            a.interceptSendToEndpoint("file:classes?noop=true&idempotent=false&fileName=snacks.txt")
                    .skipSendToOriginalEndpoint().to("mock:enricher");
            a.interceptSendToEndpoint("kafka:out?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:topic-1");
            a.interceptSendToEndpoint("kafka:errors?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:error-topic");
        });

        MockEndpoint pollEnrichEndpoint = context.getEndpoint("mock:enricher", MockEndpoint.class);
        pollEnrichEndpoint.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody("chips,popcorn"));

        MockEndpoint resultEndpoint1 = context.getEndpoint("mock:topic-1", MockEndpoint.class);
        resultEndpoint1.expectedMessageCount(0);

        String body = "{\n" +
                "  \"message\": \"Party at my place this Saturday\",\n" +
                "  \"bringFriends\": true,\n" +
                "  \"bringSnacks\": true\n" +
                "}";

        // when
        template.sendBody("direct:start", body);

        // then
        assertMockEndpointsSatisfied();
    }

    @Test
    public void test2() {
        template.sendBody("direct:in", "{\"message\": \"Party at my place this Saturday\", \"bringFriends\": true, \"bringSnacks\": true}");
    }

    @Test
    public void test3() {
        System.out.println(craftToMap(new String[] {}));
        System.out.println(craftToMap(new String[] {"a"}));
        System.out.println(craftToMap(new String[] {"a", "b"}));
        System.out.println(craftToMap(new String[] {"a", "b", "c"}));
        System.out.println(craftToMap(new String[] {"a", "b", "c", "d"}));
        System.out.println(craftToMap(new String[] {"a", "b", "c", "d", "e"}));
        System.out.println(craftToMap(new String[] {"a", "b", "c", "d", "e", "f"}));
    }

    private Map<Integer, String> craftToMap(String[] snacks) {
        return IntStream.range(0, snacks.length)
                .mapToObj(i -> new AbstractMap.SimpleEntry<>(i, snacks[i]))
                .collect(Collectors.toMap(
                        entry -> entry.getKey() % 3,
                        AbstractMap.SimpleEntry::getValue,
                        (snack1, snack2) -> String.format("%s, %s", snack1, snack2)
                ));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {

            @Override
            public void configure() {
                getContext().setTracing(true);

                // body to header, split, aggregate to 3 messages, header to body, open: aggregator
//                splitSolution();

                // loadbalancer, problems: completion, message when no snacks
//                balanceLoaderSolution();

                // split in method, build 3 exchanges - java intensive, todo
                // process and send sequentially todo
                simpleSolution();

            }

            private void splitSolution() {
                from("direct:in")
                        .routeId("complicated-route-4")
                        .filter().jsonpath("$.[?(@.bringFriends == true)]")
                        .pollEnrich()
                        .simple("file:deleteme?noop=true&idempotent=false&fileName=snacks.txt")
                        .aggregationStrategy(this::contentAsBody)
                        .process(exchange -> System.out.println("body: " + exchange.getMessage().getBody(String.class)))
                        .split().tokenize(",")
                        .process(exchange -> System.out.println("split body: " + exchange.getMessage().getBody(String.class)))
                        .to("mock:out");
            }

            private void balanceLoaderSolution() {
                from("direct:in")
                        .routeId("complicated-route-4")
                        .filter().jsonpath("$.[?(@.bringFriends == true)]")
                        .pollEnrich()
                        .simple("file:deleteme?noop=true&idempotent=false&fileName=snacks.txt")
                        .aggregationStrategy(this::contentAsBody)
                        .split().tokenize(",")
                        .process(exchange -> System.out.println("split body: " + exchange.getMessage().getBody(String.class)))
                        .loadBalance().roundRobin()
                        .to("direct:a")
                        .to("direct:b")
                        .to("direct:c")
                        .end()
                        .to("mock:out");
                from("direct:a")
                        .aggregate(header("original-exchange-body"), this::mergeBodies)
                        .completionSize(1)
//                        .completionTimeout(500)
//                        .completionSize(10).completionInterval(5000)
//                        .completionSize(2).completionTimeout(500)
                        .log("a body: ${body}")
                        .to("mock:a");
                from("direct:b")
                        .aggregate(header("original-exchange-body"), this::mergeBodies)
                        .completionSize(1)
//                        .completionTimeout(500)
//                        .completionSize(10).completionInterval(5000)
//                        .completionSize(2).completionTimeout(500)
                        .log("b body: ${body}")
                        .to("mock:b");
                from("direct:c")
                        .aggregate(header("original-exchange-body"), this::mergeBodies)
                        .completionSize(1)
//                        .completionTimeout(500)
//                        .completionSize(10).completionInterval(5000)
//                        .completionSize(1).completionTimeout(500)
                        .log("c body: ${body}")
                        .to("mock:c");
            }

            private void simpleSolution() {
                from("direct:in")
                        .routeId("complicated-route-4")
                        .filter().jsonpath("$.[?(@.bringFriends == true)]")
                        .pollEnrich()
                        .simple("file:deleteme?noop=true&idempotent=false&fileName=snacks.txt")
                        .aggregationStrategy(this::contentAsHeader2)
                        .multicast().to("direct:a", "direct:b", "direct:c");
                from("direct:a")
                        .process(exchange -> {
                            String body = exchange.getMessage().getBody(String.class);
                            Map<?, ?> snacks = exchange.getMessage().getHeader("snacks", Map.class);
                            String messageToBob = String.format(
                                    "%s. Please bring %s.",
                                    toMap(body).get("message"),
                                    snacks.get(0)
                            );
                            exchange.getMessage().setBody(messageToBob);
                        })
                        .log("final message a: ${body}")
                        .to("mock:end");
                from("direct:b")
                        .process(exchange -> {
                            String body = exchange.getMessage().getBody(String.class);
                            Map<?, ?> snacks = exchange.getMessage().getHeader("snacks", Map.class);
                            String messageToBob = String.format(
                                    "%s. Please bring %s.",
                                    toMap(body).get("message"),
                                    snacks.get(1)
                            );
                            exchange.getMessage().setBody(messageToBob);
                        })
                        .log("final message b: ${body}")
                        .to("mock:end");
                from("direct:c")
                        .process(exchange -> {
                            String body = exchange.getMessage().getBody(String.class);
                            Map<?, ?> snacks = exchange.getMessage().getHeader("snacks", Map.class);
                            String messageToBob = String.format(
                                    "%s. Please bring %s.",
                                    toMap(body).get("message"),
                                    snacks.get(2)
                            );
                            exchange.getMessage().setBody(messageToBob);
                        })
                        .log("final message c: ${body}")
                        .to("mock:end");
            }

            private Exchange mergeBodies(Exchange firstExchange, Exchange secondExchange) {
                if (firstExchange == null) {
                    return secondExchange;
                }
                String firstBody = firstExchange.getMessage().getBody(String.class);
                String secondBody = secondExchange.getMessage().getBody(String.class);
                String newBody = String.format("%s, %s", firstBody, secondBody);
                secondExchange.getMessage().setBody(newBody);
                return secondExchange;
            }

            private Exchange contentAsBody(Exchange originalExchange, Exchange enrichmentExchange) {
                enrichmentExchange.getMessage().setHeader("original-exchange-body", originalExchange.getMessage().getBody());
                return enrichmentExchange;
            }

            private Exchange contentAsHeader(Exchange originalExchange, Exchange enrichmentExchange) {
                String snacks = enrichmentExchange.getMessage().getBody(String.class);
                originalExchange.getMessage().setHeader("snacks", snacks);
                return originalExchange;
            }

            private Exchange contentAsHeader2(Exchange originalExchange, Exchange enrichmentExchange) {
                String[] snacks = enrichmentExchange.getMessage().getBody(String.class).split(",");
                originalExchange.getMessage().setHeader("snacks", craftToMap(snacks));
                return originalExchange;
            }

            private void messageToFriends(Exchange exchange) {
                final var body = exchange.getMessage().getBody(Map.class);
                final var inputMessage = body.get("message");
                final var outputMessage = toJson(Map.of("message", inputMessage));
                exchange.getMessage().setBody(outputMessage);
            }
        };
    }

}
