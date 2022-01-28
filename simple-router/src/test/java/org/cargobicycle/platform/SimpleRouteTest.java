package org.cargobicycle.platform;

import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleRouteTest extends CamelTestSupport {
    private static final String RESOURCES = "src/test/resources";

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new SimpleRoute();
    }

    @Test
    public void validRequestsAreForwardedCorrectly() throws Exception {
        // given
        overwriteEndpoints();
        String validRequestBody = Files.readString(Paths.get(RESOURCES, "ui-request.json"));
        MockEndpoint outEndpoint = context.getEndpoint("mock:out", MockEndpoint.class);
        outEndpoint.expectedMessageCount(1);
        outEndpoint.expectedBodiesReceived(validRequestBody);

        MockEndpoint errorEndpoint = context.getEndpoint("mock:error", MockEndpoint.class);
        errorEndpoint.expectedMessageCount(0);

        // when
        template.sendBody("direct:start", validRequestBody);

        // then
        assertMockEndpointsSatisfied();
    }

    @Test
    public void invalidRequestsAreForwardedCorrectly() throws Exception {
        // given
        overwriteEndpoints();
        String invalidRequestBody = Files.readString(Paths.get(RESOURCES, "ui-broken-request.json"));

        MockEndpoint outEndpoint = context.getEndpoint("mock:out", MockEndpoint.class);
        outEndpoint.expectedMessageCount(0);

        MockEndpoint errorEndpoint = context.getEndpoint("mock:error", MockEndpoint.class);
        errorEndpoint.expectedMessageCount(1);
        errorEndpoint.expectedBodiesReceived(invalidRequestBody);

        // when
        template.sendBody("direct:start", invalidRequestBody);

        // then
        assertMockEndpointsSatisfied();
    }

    private void overwriteEndpoints() throws Exception {
        AdviceWith.adviceWith(context, "simple-route", a -> {
            a.replaceFromWith("direct:start");
            a.weaveByToUri("kafka:bookings?brokers=localhost:29092")
                    .replace().to("mock:out");
            a.weaveByToUri("kafka:error-topic?brokers=localhost:29092")
                    .replace().to("mock:error");
        });
    }

}
