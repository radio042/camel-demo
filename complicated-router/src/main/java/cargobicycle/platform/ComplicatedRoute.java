package cargobicycle.platform;

import cargobicycle.platform.entities.Booking;
import cargobicycle.platform.entities.BookingForAnalytics;
import cargobicycle.platform.entities.BookingForCustomers;
import cargobicycle.platform.entities.BookingForProviders;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jsonvalidator.JsonValidationException;


public class ComplicatedRoute extends RouteBuilder {
    @Override
    public void configure() {
        JacksonDataFormat bookingDataFormat = new JacksonDataFormat();
        bookingDataFormat.setUnmarshalType(Booking.class);
        bookingDataFormat.addModule(new JavaTimeModule());

        onException(JsonValidationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .to("seda:errors")
                .setBody().constant("Invalid json data");

        from("rest:post:booking")
                .id("complicated-route")
                .to("json-validator:ui-schema.json")
                .unmarshal(bookingDataFormat)
                .enrich()
                    .simple("rest:get:${header.providerId}/name?host=localhost:8080/providers")
                    .aggregationStrategy(((oldExchange, newExchange) -> appendAsHeader(oldExchange, newExchange, "provider-name")))
                    .id("enrich-provider-name")
                .enrich()
                    .simple("rest:get:${header.providerId}/offer/${header.bicycleId}/description?host=localhost:8080/providers")
                    .aggregationStrategy(((oldExchange, newExchange) -> appendAsHeader(oldExchange, newExchange, "bicycle-description")))
                    .id("enrich-bicycle-description")
                .enrich()
                    .simple("rest:get:${header.customerId}/name?host=localhost:8080/customers")
                    .aggregationStrategy(((oldExchange, newExchange) -> appendAsHeader(oldExchange, newExchange, "customer-name")))
                    .id("enrich-customer-name")
                .multicast()
                    .to("direct:customer-services", "direct:provider-services", "direct:analytics-services");

        from("direct:customer-services")
                .id("customer-route")
                .process(this::toBookingForCustomers)
                .marshal().json(BookingForCustomers.class)
                .to("kafka:customer-events?brokers=localhost:9092");

        from("direct:provider-services")
                .id("provider-route")
                .process(this::toBookingForProviders)
                .marshal().json(BookingForProviders.class)
                .to("kafka:provider-events?brokers=localhost:9092");

        from("direct:analytics-services")
                .id("analytics-route")
                .process(this::toBookingForAnalytics)
                .marshal().json(BookingForAnalytics.class)
                .to("kafka:analytics-events?brokers=localhost:9092");

        from("seda:errors")
                .id("error-route")
                .to("kafka:error-topic?brokers=localhost:9092");
    }

    private void toBookingForAnalytics(Exchange exchange) {
        final Booking booking = exchange.getMessage().getBody(Booking.class);
        final BookingForAnalytics bookingForAnalytics = BookingForAnalytics.builder()
                .customerName(exchange.getMessage().getHeader("customer-name", String.class))
                .bicycleDescription(exchange.getMessage().getHeader("bicycle-description", String.class))
                .providerName(exchange.getMessage().getHeader("provider-name", String.class))
                .fromDate(booking.getFromDate())
                .toDate(booking.getToDate()).build();
        exchange.getMessage().setBody(bookingForAnalytics);
    }

    private void toBookingForProviders(Exchange exchange) {
        final Booking booking = exchange.getMessage().getBody(Booking.class);
        final BookingForProviders bookingForProviders = BookingForProviders.builder()
                .customerName(exchange.getMessage().getHeader("customer-name", String.class))
                .providerId(booking.getProviderId())
                .bicycleId(booking.getBicycleId())
                .fromDate(booking.getFromDate())
                .toDate(booking.getToDate()).build();
        exchange.getMessage().setBody(bookingForProviders);
    }

    private void toBookingForCustomers(Exchange exchange) {
        final Booking booking = exchange.getMessage().getBody(Booking.class);
        final BookingForCustomers bookingForCustomers = BookingForCustomers.builder()
                .customerId(booking.getCustomerId())
                .bicycleDescription(exchange.getMessage().getHeader("bicycle-description", String.class))
                .providerName(exchange.getMessage().getHeader("provider-name", String.class))
                .fromDate(booking.getFromDate())
                .toDate(booking.getToDate()).build();
        exchange.getMessage().setBody(bookingForCustomers);
    }

    private Exchange appendAsHeader(Exchange originalExchange, Exchange enrichmentExchange, String headerKey) {
        originalExchange.getMessage().setHeader(headerKey, enrichmentExchange.getMessage().getBody(String.class));
        return originalExchange;
    }
}
