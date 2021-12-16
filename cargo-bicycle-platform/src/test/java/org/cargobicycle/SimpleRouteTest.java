package org.cargobicycle;

import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class SimpleRouteTest extends CamelTestSupport {

    @Test
    public void messagesForMarketingChannelsAreSent() throws Exception {
        // given
        AdviceWith.adviceWith(context, "simple-route", a -> {
            a.replaceFromWith("direct:start");
            a.interceptSendToEndpoint("kafka:twitter-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:topic-1");
            a.interceptSendToEndpoint("kafka:facebook-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:topic-2");
            a.interceptSendToEndpoint("kafka:error-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:error-topic");
        });
        String audience = "marketing channels";
        String body = String.format("{\n" +
                "  \"message\": \"Interested in world domination? Acme Inc. is looking to hire someone like you!\",\n" +
                "  \"audience\": \"%s\"\n" +
                "}", audience);

        MockEndpoint resultEndpoint1 = context.getEndpoint("mock:topic-1", MockEndpoint.class);
        resultEndpoint1.expectedMessageCount(1);
        resultEndpoint1.expectedBodiesReceived(body);

        MockEndpoint resultEndpoint2 = context.getEndpoint("mock:topic-2", MockEndpoint.class);
        resultEndpoint2.expectedMessageCount(1);
        resultEndpoint2.expectedBodiesReceived(body);

        MockEndpoint resultEndpoint3 = context.getEndpoint("mock:error-topic", MockEndpoint.class);
        resultEndpoint3.expectedMessageCount(0);

        // when
        template.sendBody("direct:start", body);

        // then
        assertMockEndpointsSatisfied();
    }

    @Test
    public void messagesForOtherChannelsAreFilteredOut() throws Exception {
        // given
        AdviceWith.adviceWith(context, "simple-route", a -> {
            a.replaceFromWith("direct:start");
            a.interceptSendToEndpoint("kafka:twitter-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:topic-1");
            a.interceptSendToEndpoint("kafka:facebook-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:topic-2");
            a.interceptSendToEndpoint("kafka:error-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:error-topic");
        });

        String audience = "Acme Inc. minions";
        String body = String.format("{\n" +
                "  \"message\": \"Interested in world domination? Acme Inc. is looking to hire someone like you!\",\n" +
                "  \"audience\": \"%s\"\n" +
                "}", audience);

        MockEndpoint resultEndpoint1 = context.getEndpoint("mock:topic-1", MockEndpoint.class);
        resultEndpoint1.expectedMessageCount(0);

        MockEndpoint resultEndpoint2 = context.getEndpoint("mock:topic-2", MockEndpoint.class);
        resultEndpoint2.expectedMessageCount(0);

        MockEndpoint resultEndpoint3 = context.getEndpoint("mock:error-topic", MockEndpoint.class);
        resultEndpoint3.expectedMessageCount(0);

        // when
        template.sendBody("direct:start", body);

        // then
        assertMockEndpointsSatisfied();
    }

    @Test
    public void invalidMessagesAreSentToErrorTopic() throws Exception {
        // given
        AdviceWith.adviceWith(context, "simple-route", a -> {
            a.replaceFromWith("direct:start");
            a.interceptSendToEndpoint("kafka:twitter-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:topic-1");
            a.interceptSendToEndpoint("kafka:facebook-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:topic-2");
            a.interceptSendToEndpoint("kafka:error-topic?brokers=localhost:29092")
                    .skipSendToOriginalEndpoint().to("mock:error-topic");
        });

        MockEndpoint resultEndpoint1 = context.getEndpoint("mock:topic-1", MockEndpoint.class);
        resultEndpoint1.expectedMessageCount(0);

        MockEndpoint resultEndpoint2 = context.getEndpoint("mock:topic-2", MockEndpoint.class);
        resultEndpoint2.expectedMessageCount(0);

        MockEndpoint resultEndpoint3 = context.getEndpoint("mock:error-topic", MockEndpoint.class);
        resultEndpoint3.expectedMessageCount(1);

        String body = "no json";

        // when
        template.sendBody("direct:start", body);

        // then
        assertMockEndpointsSatisfied();
    }

}
