package com.example.spring.boot.kafka.kafkatest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
public class KafkaSimpleController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Remember when you run this application, please run the Zookeeper with Kafka too****
     * <p>
     * Commands in Mac:
     * <p>
     * Installation:
     * brew cask install java
     * brew install kafka
     * <p>
     * Encountered and solved Problem:
     * Unable to start kafka with error: Broker may not be configured.
     * Reason: Due to file access denied as stated in Kafka logs
     * Solution: start Kafka server with su level
     * <p>
     * Start up (better with sudo to prevent file access denied errors):
     * Command for MacOS install Kafka with Zookeeper with brew:
     * sudo zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
     * sudo kafka-server-start /usr/local/etc/kafka/server.properties
     */

    // Constructor injection
    private final KafkaTemplate<String, String> kafkaTemplate;

    // Bring in the Bean created in KafkaConfiguration.java file.
    private final Gson jsonConverter;

    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaSimpleController(KafkaTemplate<String, String> kafkaTemplate, Gson jsonConverter, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
        this.objectMapper = objectMapper;
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
    public void postSimpleModelWithGson(@RequestBody SimpleModel simpleModel) {
        logger.info("postSimpleModelWithGson()");
        // Convert SimpleModel object to Gson string
        String simpleModelString = jsonConverter.toJson(simpleModel);
        this.kafkaTemplate.send("myTopic", simpleModelString);
    }

    /**
     * POST
     * http://localhost:8080/api/kafka/objectMapper
     *
     * @param simpleModel: An example object for testing Kafka.
     */
    @PostMapping("/objectMapper")
    public void postSimpleModelWithObjectMapper(@RequestBody SimpleModel simpleModel) {
        logger.info("postSimpleModelWithObjectMapper()");
        String simpleModelString = null;
        try {
            simpleModelString = objectMapper.writeValueAsString(simpleModel);
        } catch (JsonProcessingException e) {
            logger.error("Unable convert SimpleModel object from object to JSON string.");
        }

        logger.info("simpleModelString: {}", simpleModelString);

        this.kafkaTemplate.send("myTopic3", simpleModelString);
    }

    /**
     * Kafka listener for myTopic. Sent from postSimpleModelWithGson().
     * <p>
     * After you sent a value from Postman, to REST API here, processed and sent to Kafka using KafkaTemplate, in your terminal/console, you may type:
     * kafka-console-consumer --bootstrap-server localhost:9092 --topic myTopic
     * to see the values spit out from that topic in Kafka
     * <p>
     * Create Kafka listener in REST API
     * When you run POST request with http://localhost:8080/api/kafka/ again, this API endpoint will run by itself, like a listener.
     *
     * @param simpleModelString: A String got from Kafka, ready to be converted into SimpleModel object.
     */
    @KafkaListener(topics = "myTopic")
    public void getSimpleModelFromKafkaWithGson(String simpleModelString) {
        logger.info("getSimpleModelFromKafkaWithGson()");
        logger.info("simpleModelString: {}", simpleModelString);

        // Convert Gson String back to object.
        SimpleModel simpleModel = jsonConverter.fromJson(simpleModelString, SimpleModel.class);

        logger.info("simpleModel: {}", simpleModel);
    }

    /**
     * Kafka Listener for myTopic3. Sent from postSimpleModelWithObjectMapper().
     * <p>
     * Testing Kafka JSON serialization using Spring Boot default Object Mapper.
     *
     * @param simpleModelString: A String got from Kafka, ready to be converted into SimpleModel object.
     */
//    @KafkaListener(topics = "myTopic3")
    public void getSimpleModelFromKafkaWithObjectMapper(String simpleModelString) {
        logger.info("getSimpleModelFromKafkaWithObjectMapper()");
        SimpleModel simpleModel = null;
        logger.info("simpleModelString: {}", simpleModelString);

        try {
            simpleModel = objectMapper.readValue(simpleModelString, SimpleModel.class);
        } catch (JsonProcessingException e) {
            logger.error("Unable convert SimpleModel object from JSON string to object.");
        }

        logger.info("simpleModel: {}", simpleModel);
    }

    /**
     * Send to Kafka using Gson as JSON serializer.
     * POST
     * http://localhost:8080/api/kafka/v2
     * <p>
     * With using Gson as JSON serializer.
     * <p>
     * Raw JSON body:
     * {
     * "title": "Kafka Consumption",
     * "description": "Creating a Kafka Consumer with Spring Boot"
     * }
     *
     * @param moreSimpleModel: MoreSimpleModel object to be tested in Kafka.
     */
    @PostMapping("/v2")
    public void postMoreSimpleModelWithGson(@RequestBody MoreSimpleModel moreSimpleModel) {
        logger.info("postMoreSimpleModelWithGson()");
        kafkaTemplate.send("myTopic2", jsonConverter.toJson(moreSimpleModel));
    }

    /**
     * POST
     * http://localhost:8080/api/kafka/v2/objectMapper
     * With using Spring Boot default ObjectMapper as JSON serializer.
     *
     * @param moreSimpleModel: MoreSimpleModel object to be tested in Kafka.
     */
    @PostMapping("/v2/objectMapper")
    public void postMoreSimpleModelWithObjectMapper(@RequestBody MoreSimpleModel moreSimpleModel) {
        logger.info("postMoreSimpleModelWithObjectMapper()");
        try {
            kafkaTemplate.send("myTopic4", objectMapper.writeValueAsString(moreSimpleModel));
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert MoreSimpleModel from Object to JSON String.");
        }
    }

    /**
     * Kafka listener for myTopic2, sent from postMoreSimpleModelWithGson().
     *
     * @param moreSimpleModelString: A String got from Kafka, ready to be converted into MoreSimpleModel object.
     */
    @KafkaListener(topics = "myTopic2")
    public void getMoreSimpleModelFromKafkaWithGson(String moreSimpleModelString) {
        logger.info("getMoreSimpleModelFromKafkaWithGson()");
        logger.info("moreSimpleModelString: {}", moreSimpleModelString);

        MoreSimpleModel moreSimpleModel = jsonConverter.fromJson(moreSimpleModelString, MoreSimpleModel.class);

        logger.info("moreSimpleModel: {}", moreSimpleModel);
    }

    /**
     * Kafka listener for myTopic4, sent from postMoreSimpleModelWithGson().
     *
     * @param moreSimpleModelString: A String got from Kafka, ready to be converted into MoreSimpleModel object.
     */
    @KafkaListener(topics = "myTopic4")
    public void getMoreSimpleModelFromKafkaWithObjectMapper(String moreSimpleModelString) {
        logger.info("getMoreSimpleModelFromKafkaWithObjectMapper()");
        logger.info("moreSimpleModelString: {}", moreSimpleModelString);
        MoreSimpleModel moreSimpleModel = null;

        try {
            moreSimpleModel = objectMapper.readValue(moreSimpleModelString, MoreSimpleModel.class);
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert MoreSimpleModel from JSON String to Object.");
        }

        if (!ObjectUtils.isEmpty(moreSimpleModel)) {
            logger.info("moreSimpleModel: {}", moreSimpleModel);
        }
    }
}
