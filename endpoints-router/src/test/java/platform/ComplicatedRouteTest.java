package platform;

import cargobicycle.platform.ComplicatedRoute;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
@Disabled("todo: use properties")
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

        MockEndpoint customerEventsMockEndpoint = getMockEndpoint("mock:customer-events");
        customerEventsMockEndpoint.expectedMessageCount(1);
        customerEventsMockEndpoint.expectedBodiesReceived(Files.readString(Paths.get(RESOURCES, "message-to-customer-services.json")));

        MockEndpoint providerEventsMockEndpoint = getMockEndpoint("mock:provider-events");
        providerEventsMockEndpoint.expectedMessageCount(1);
        providerEventsMockEndpoint.expectedBodiesReceived(Files.readString(Paths.get(RESOURCES, "message-to-provider-services.json")));

        MockEndpoint analyticsEventsMockEndpoint = getMockEndpoint("mock:analytics-events");
        analyticsEventsMockEndpoint.expectedMessageCount(1);
        analyticsEventsMockEndpoint.expectedBodiesReceived(Files.readString(Paths.get(RESOURCES, "message-to-analytics-services.json")));

        getMockEndpoint("mock:error").expectedMessageCount(0);

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

        getMockEndpoint("mock:customer-events").expectedMessageCount(0);
        getMockEndpoint("mock:provider-events").expectedMessageCount(0);
        getMockEndpoint("mock:analytics-events").expectedMessageCount(0);

        MockEndpoint errorMockEndpoint = getMockEndpoint("mock:error");
        errorMockEndpoint.expectedMessageCount(1);
        errorMockEndpoint.expectedBodiesReceived(Files.readString(Paths.get(RESOURCES, "ui-broken-request.json")));

        // when
        template.sendBody("direct:start", invalidRequestBody);

        // then
        assertMockEndpointsSatisfied();
    }

    private void overwriteEndpoints() throws Exception {
        AdviceWith.adviceWith(context, "error-route", a -> {
            a.weaveByToUri("kafka:error-topic?brokers=localhost:9092")
                    .replace().to("mock:error");
        });
        AdviceWith.adviceWith(context, "complicated-route", a -> {
            a.replaceFromWith("direct:start");
            a.weaveById("enrich-provider-name")
                    .replace().process(exchange -> exchange.getMessage().setHeader("provider-name", "Horst Merlin"));
            a.weaveById("enrich-bicycle-description")
                    .replace().process(exchange -> exchange.getMessage().setHeader("bicycle-description", "Cargo Bike 3000"));
            a.weaveById("enrich-customer-name")
                    .replace().process(exchange -> exchange.getMessage().setHeader("customer-name", "Zaphod Beeblebrox"));
        });
        AdviceWith.adviceWith(context, "customer-route", a -> {
            a.weaveByToUri("kafka:customer-events?brokers=localhost:9092")
                    .replace().to("mock:customer-events");
        });
        AdviceWith.adviceWith(context, "provider-route", a -> {
            a.weaveByToUri("kafka:provider-events?brokers=localhost:9092")
                    .replace().to("mock:provider-events");
        });
        AdviceWith.adviceWith(context, "analytics-route", a -> {
            a.weaveByToUri("kafka:analytics-events?brokers=localhost:9092")
                    .replace().to("mock:analytics-events");

        });
    }

}