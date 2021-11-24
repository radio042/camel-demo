package org.sharedpool.providerservices;

import org.apache.camel.builder.RouteBuilder;

public class ProviderRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("rest:get:providers/{id}/name")
                .transform().constant("Horst Merlin");
    }
}
