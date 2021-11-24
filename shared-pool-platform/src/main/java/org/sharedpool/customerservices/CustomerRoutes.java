package org.sharedpool.customerservices;

import org.apache.camel.builder.RouteBuilder;

public class CustomerRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("rest:get:customers/{id}/name")
                .transform().constant("Zaphod Beeblebrox");
    }
}
