package cargobicycle.customerservices;

import org.apache.camel.builder.RouteBuilder;

public class CustomerRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("rest:get:customers/{customerId}/name")
                .transform().constant("Zaphod Beeblebrox");
    }
}
