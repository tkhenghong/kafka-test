package com.example.spring.boot.kafka.kafkatest;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
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

    // Encountered and solved Problem:
    // Unable to start kafka with error: Broker may not be configured.
    // Reason: Due to file access denied as stated in Kafka logs
    // Solution: start Kafka server with su level


    // Start up (better with sudo to prevent file access denied errors):
    // Command for MacOS install Kafka with Zookeeper with brew:
    // sudo zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
    // sudo kafka-server-start /usr/local/etc/kafka/server.properties

    // Constructor injection
    private KafkaTemplate<String, String> kafkaTemplate;

    // Bring in the Bean created in KafkaConfiguration.java file
    private Gson jsonConverter;

    @Autowired
    public KafkaSimpleController(KafkaTemplate<String, String> kafkaTemplate, Gson jsonConverter) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
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
        // Convert SimpleModel object to Gson string
        String simpleModelString = jsonConverter.toJson(simpleModel);
        this.kafkaTemplate.send("myTopic", simpleModelString);
    }

    // After you sent a value from Postman, to REST API here, processed and sent to Kafka using KafkaTemplate, in your terminal/console, you may type:
    // kafka-console-consumer --bootstrap-server localhost:9092 --topic myTopic
    // to see the values spit out from that topic in Kafka

    // Create Kafka listener in REST API
    // When you run POST request with http://localhost:8080/api/kafka/ again, this API endpoint will run by itself, like a listener
    @KafkaListener(topics = "myTopic")
    public void getFromKafka(String simpleModelString) {

        System.out.println("simpleModelString: " + simpleModelString);

        // Convert Gson String back to Model
        SimpleModel simpleModel = jsonConverter.fromJson(simpleModelString, SimpleModel.class);

        System.out.println("simpleModel.toString(): " + simpleModel.toString());
    }

    // http://localhost:8080/api/kafka/v2
    // raw JSON body:
    // {
    //	"title": "Kafka Consumption",
    //	"description": "Creating a Kafka Consumer with Spring Boot"
    //}
    @PostMapping("/v2")
    public void post(@RequestBody MoreSimpleModel moreSimpleModel) {
        kafkaTemplate.send("myTopic2", jsonConverter.toJson(moreSimpleModel));
    }

    @KafkaListener(topics = "myTopic2")
    public void getFromKafka2(String moreSimpleModelString) {
        System.out.println("moreSimpleModelString: " + moreSimpleModelString);

        MoreSimpleModel moreSimpleModel = jsonConverter.fromJson(moreSimpleModelString, MoreSimpleModel.class);

        System.out.println("moreSimpleModel.toString(): " + moreSimpleModel.toString());
    }
}
