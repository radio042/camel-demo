package cargobicycle.platform;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static cargobicycle.platform.Helper.toJson;
import static cargobicycle.platform.Helper.toMap;


public class ComplicatedRoute extends RouteBuilder {
    @Override
    public void configure() {
        JacksonDataFormat bookingDataFormat = new JacksonDataFormat();
        bookingDataFormat.setUnmarshalType(Booking.class);
        bookingDataFormat.addModule(new JavaTimeModule());

        onException(ValidationException.class)
                .to("kafka:error-topic?brokers=localhost:9092");

        from("rest:post:booking")
                .id("complicated-route")
                .to("json-validator:ui-schema.json")
                .process(exchange -> exchange.getMessage().setHeaders(toMap(exchange.getMessage().getBody(String.class))))
                .process(exchange -> exchange.getMessage().removeHeaders("Camel*"))
                .unmarshal(bookingDataFormat)
                .log(LoggingLevel.INFO, "### ${body.customerId}")
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
                .id("customer-route")
                .process(this::messageToCustomerServices)
                .to("kafka:customer-events?brokers=localhost:9092");

        from("direct:provider-services")
                .id("provider-route")
                .process(this::messageToProviderServices)
                .to("kafka:provider-events?brokers=localhost:9092");

        from("direct:analytics-services")
                .id("analytics-route")
                .process(this::messageToAnalyticsServices)
                .to("kafka:analytics-events?brokers=localhost:9092");
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
