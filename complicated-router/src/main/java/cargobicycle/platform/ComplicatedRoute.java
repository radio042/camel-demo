package cargobicycle.platform;

import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;

import java.util.Map;

import static cargobicycle.platform.Helper.toJson;
import static cargobicycle.platform.Helper.toMap;


public class ComplicatedRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(ValidationException.class)
                .to("kafka:error-topic?brokers=localhost:29092");

        from("rest:post:booking")
                .to("json-validator:ui-schema.json")
                .process(exchange -> exchange.getMessage().setHeaders(toMap(exchange.getMessage().getBody(String.class))))
                .process(exchange -> exchange.getMessage().removeHeaders("Camel*"))
                .enrich()
                    .simple("rest:get:${header.providerId}/name?host=localhost:8080/providers")
                    .aggregationStrategy(((oldExchange, newExchange) -> appendAsHeader(oldExchange, newExchange, "provider-name")))
                .enrich()
                    .simple("rest:get:${header.providerId}/offer/${header.bicycleId}/description?host=localhost:8080/providers")
                    .aggregationStrategy(((oldExchange, newExchange) -> appendAsHeader(oldExchange, newExchange, "bicycle-description")))
                .enrich()
                    .simple("rest:get:${header.customerId}/name?host=localhost:8080/customers")
                    .aggregationStrategy(((oldExchange, newExchange) -> appendAsHeader(oldExchange, newExchange, "customer-name")))
                .multicast()
                    .to("direct:customer-services", "direct:provider-services", "direct:analytics-services");

        from("direct:customer-services")
                .process(this::messageToCustomerServices)
                .to("kafka:customer-events?brokers=localhost:29092");

        from("direct:provider-services")
                .process(this::messageToProviderServices)
                .to("kafka:provider-events?brokers=localhost:29092");

        from("direct:analytics-services")
                .process(this::messageToAnalyticsServices)
                .to("kafka:analytics-events?brokers=localhost:29092");
    }

    private void messageToAnalyticsServices(Exchange exchange) {
        Map<String, Object> booking = toMap(exchange.getMessage().getBody(String.class));
        booking.remove("providerId");
        booking.put("providerName", exchange.getMessage().getHeader("provider-name"));
        booking.remove("bicycleId");
        booking.put("bicycleDescription", exchange.getMessage().getHeader("bicycle-description"));
        booking.remove("customerId");
        booking.put("customerName", exchange.getMessage().getHeader("customer-name"));
        exchange.getMessage().setBody(toJson(booking));
    }

    private void messageToProviderServices(Exchange exchange) {
        Map<String, Object> booking = toMap(exchange.getMessage().getBody(String.class));
        booking.remove("customerId");
        booking.put("customerName", exchange.getMessage().getHeader("customer-name"));
        exchange.getMessage().setBody(toJson(booking));
    }

    private void messageToCustomerServices(Exchange exchange) {
        Map<String, Object> booking = toMap(exchange.getMessage().getBody(String.class));
        booking.remove("providerId");
        booking.put("providerName", exchange.getMessage().getHeader("provider-name"));
        booking.remove("bicycleId");
        booking.put("bicycleDescription", exchange.getMessage().getHeader("bicycle-description"));
        exchange.getMessage().setBody(toJson(booking));
    }

    private Exchange appendAsHeader(Exchange originalExchange, Exchange enrichmentExchange, String headerKey) {
        originalExchange.getMessage().setHeader(headerKey, enrichmentExchange.getMessage().getBody(String.class));
        return originalExchange;
    }

}
