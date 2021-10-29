package org.acme.marketing;

import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

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

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new ComplicatedRoute2();
    }

}
