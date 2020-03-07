package com.example.spring.boot.kafka.kafkatest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
public class KafkaSimpleController {

    // Remember when you run this application, please run the Zookeeper with Kafka too****
    // Commands in Mac:
    // Installation:
    // brew cask install java
    // brew install kafka


    // Start up:
    // zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
    // kafka-server-start /usr/local/etc/kafka/server.properties

    // Constructor injection
    private KafkaTemplate<String, SimpleModel> kafkaTemplate;

    @Autowired
    public KafkaSimpleController(KafkaTemplate<String, SimpleModel> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Create a simple API to get message from outside to store into a topic of Kafka
    // http://localhost:8080/api/kafka/
    // raw JSON body:
    // {
    //	"field1":"field1",
    //	"field2":"field2"
    // }
    // Producer
    @PostMapping("/")
    public void post(@RequestBody SimpleModel simpleModel) {
        this.kafkaTemplate.send("myTopic", simpleModel);
    }

    // After you sent a value from Postman, to REST API here, processed and sent to Kafka using KafkaTemplate, in your terminal/console, you may type:
    // kafka-console-consumer --bootstrap-server localhost:9092 --topic myTopic
    // to see the values spit out from that topic in Kafka
}
