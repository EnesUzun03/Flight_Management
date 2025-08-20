package com.enesuzun.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Flight verilerine ait Kafka mesajlarını dinleyen ve işleyen Apache Camel route'u
 */
@ApplicationScoped
public class FlightCamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        // Flight events topic'ini dinleyen route
        from("kafka:flight-events?brokers={{kafka.bootstrap.servers}}")
            .routeId("flight-kafka-consumer")
            .log("Received message from flight-events topic: ${body}")
            .unmarshal().json(JsonLibrary.Jackson)
            .log("Unmarshaled flight message: ${body}")
            .to("bean:flightMessageProcessor?method=processFlightMessage")
            .log("Flight message processed successfully")
            // İşlem sonrası başka topic'e gönder
            .marshal().json(JsonLibrary.Jackson)
            .to("kafka:{{app.kafka.flight.processed.topic}}?brokers={{kafka.bootstrap.servers}}")
            .log("Flight processed message sent to {{app.kafka.flight.processed.topic}} topic");
    }
}