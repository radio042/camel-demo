package org.sharedpool.platform;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class TestRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        rest("/say")
                .get("/hello").to("direct:hello")
                .get("/bye").consumes("application/json").to("direct:bye")
                .post("/bye").to("mock:update");

        from("direct:hello")
                .process(exchange -> {
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                })
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
