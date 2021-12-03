package org.sharedpool.platform;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class TestRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
//        restConfiguration().host("localhost:8080/say").producerComponent("http");
//        restConfiguration().component("http");

        rest("/say")
                .get("/hello").to("direct:hello")
                .get("/bye").consumes("application/json").to("direct:bye")
                .post("/bye").to("mock:update");

        rest("/blupp32")
                .get("/hello").to("direct:blupp2");

        from("direct:blupp2")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("blupp2");
                        exchange.getMessage().removeHeaders("Camel*");
                    }
                })

//                .to("rest:get:496/name?host=localhost:8080/providers")
                .enrich("rest:get:496/name?host=localhost:8080/providers")

        .process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                System.out.println("rest:get:496/name?host=localhost:8080/providers");
            }
        });

//                .to("rest:get:hello?host=http://localhost:8080/say&bridgeEndpoint=true");
//                .transform().constant("Blupp Response");

        from("direct:hello")
//                .process(exchange -> {
//                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
//                })
                .transform().constant("Hello World");

        from("direct:bye")
                .transform().constant("Bye World");

//        from("rest:get:say/hello").transform().constant("Hello World").process(new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                System.out.println(exchange);
//            }
//        });
    }
}
