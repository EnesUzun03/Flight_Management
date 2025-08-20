package com.enesuzun.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * FlightCrew verilerine ait Kafka mesajlarını dinleyen ve işleyen Apache Camel route'u
 */
@ApplicationScoped
public class FlightCrewCamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        // Crew events topic'ini dinleyen route
        from("kafka:crew-events?brokers={{kafka.bootstrap.servers}}")
            .routeId("crew-kafka-consumer")
            .log("Received message from crew-events topic: ${body}")
            .unmarshal().json(JsonLibrary.Jackson)
            .log("Unmarshaled crew message: ${body}")
            .to("bean:crewMessageProcessor?method=processCrewMessage")
            .log("Crew message processed successfully")
            // İşlem sonrası başka topic'e gönder
            .marshal().json(JsonLibrary.Jackson)//marshal serilestirmek için kullanılır.dosyayı json formatına çevirir.
            .to("kafka:{{app.kafka.crew.processed.topic}}?brokers={{kafka.bootstrap.servers}}")
            .log("Crew processed message sent to {{app.kafka.crew.processed.topic}} topic");
    }
}