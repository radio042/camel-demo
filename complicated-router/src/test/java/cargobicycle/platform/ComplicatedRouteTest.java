package cargobicycle.platform;

import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

class ComplicatedRouteTest extends CamelTestSupport {
    private static final String RESOURCES = "src/test/resources";

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new ComplicatedRoute();
    }

    @Test
    public void validRequestsAreForwardedCorrectly() throws Exception {
        // given
        overwriteEndpoints();
        String validRequestBody = Files.readString(Paths.get(RESOURCES, "ui-request.json"));

        getMockEndpoint("mock:customer-events").expectedMessageCount(1);
        getMockEndpoint("mock:provider-events").expectedMessageCount(1);
        getMockEndpoint("mock:analytics-events").expectedMessageCount(1);
        getMockEndpoint("mock:error").expectedMessageCount(0);

        // when
        template.sendBody("direct:start", validRequestBody);

        // then
        assertMockEndpointsSatisfied();
    }

    private void overwriteEndpoints() throws Exception {
        AdviceWith.adviceWith(context, "complicated-route", a -> {
            a.replaceFromWith("direct:start");
            a.weaveById("enrich1")
                    .replace().process(exchange -> exchange.getMessage().setHeader("provider-name", "Horst Merlin"));
            a.weaveById("enrich2")
                    .replace().process(exchange -> exchange.getMessage().setHeader("bicycle-description", "Cargo Bike 3000"));
            a.weaveById("enrich3")
                    .replace().process(exchange -> exchange.getMessage().setHeader("customer-name", "Zaphod Beeblebrox"));
            a.weaveByToUri("kafka:error-topic?brokers=localhost:29092")
                    .replace().to("mock:error");
        });
        AdviceWith.adviceWith(context, "customer-route", a -> {
            a.weaveByToUri("kafka:customer-events?brokers=localhost:29092")
                    .replace().to("mock:customer-events");
        });
        AdviceWith.adviceWith(context, "provider-route", a -> {
            a.weaveByToUri("kafka:provider-events?brokers=localhost:29092")
                    .replace().to("mock:provider-events");
        });
        AdviceWith.adviceWith(context, "analytics-route", a -> {
            a.weaveByToUri("kafka:analytics-events?brokers=localhost:29092")
                    .replace().to("mock:analytics-events");

        });
    }

}