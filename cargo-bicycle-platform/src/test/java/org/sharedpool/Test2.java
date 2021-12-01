package org.sharedpool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class Test2 extends CamelTestSupport {

    @Test
    public void test() throws Exception {
        DirectEndpoint directEndpoint = context.getEndpoint("mock:middle", DirectEndpoint.class);

        MockEndpoint mockMiddleEndpoint = context.getEndpoint("mock:middle", MockEndpoint.class);
        mockMiddleEndpoint.whenAnyExchangeReceived(e -> {
            throw new RuntimeException("oops");
        });
        MockEndpoint resultEndpoint1 = context.getEndpoint("mock:out", MockEndpoint.class);
        resultEndpoint1.expectedMessageCount(1);
        MockEndpoint exceptionEndpoint = context.getEndpoint("mock:exception", MockEndpoint.class);
        exceptionEndpoint.expectedMessageCount(1);
        template.sendBody("direct:in", "hello");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void test2() throws JsonProcessingException {
        String a = "{\n" +
                "  \"hi-res\": \"images/Logo_72.jpg\",\n" +
                "  \"low-res\": \"images/Logo_300.jpg\"\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        Map map = objectMapper.readValue(a, Map.class);
        System.out.println(map.get("hi-res"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:in")
                        .doTry().to("mock:middle").endDoTry()
                        .doCatch(Exception.class).to("mock:exception").end()
                        .to("mock:out");
            }
        };
    }
}
