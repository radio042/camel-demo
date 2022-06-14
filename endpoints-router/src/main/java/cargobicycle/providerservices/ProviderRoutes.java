package cargobicycle.providerservices;

import org.apache.camel.builder.RouteBuilder;

public class ProviderRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("rest:get:providers/{providerId}/name")
                .transform().constant("Horst Merlin");

        from("rest:get:providers/{providerId}/offer/{offerId}/description")
                .transform().constant("Cargo Bike 3000");
    }
}
