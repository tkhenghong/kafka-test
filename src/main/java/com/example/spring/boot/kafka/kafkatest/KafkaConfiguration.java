package com.example.spring.boot.kafka.kafkatest;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
// Add @EnableKafka to let this application to listen for Kafka topic
@EnableKafka
public class KafkaConfiguration {

    // As you know Spring Boot dependency injection, this is the starting part that you configure here.
    // When you run the application, @Bean will be read by Spring and run this method, get the object out from this method,
    // and put it into the Spring context.

    // ProducerFactory<String, SimpleModel>: String is the id's variable type, SimpleModel
    // All SimpleModel are replaced by String
    @Bean
    ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // The IP address of the Kafka server that you have set up.
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");

        // Needed for serializing the key and value of the JSON objects that are passing through Kafka
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    // We need another bean for KafkaTemplate(Like WebSocketTemplate).
    // Need to setup the topic (in this project is static but it can be made into dynamic) and send it to Kafka
    // This application becomes the PRODUCER (Observable)
    // The application will become a CONSUMER (Subscriber) when a WebSocket user connected to WebSocket and listens to the topic.
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory()); // The KafkaTemplate object loads with the Producer object in it.
    }

    // Remember when you run this application, please run the Zookeeper with Kafka too****


    // Kafka Consumer
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Basically just copy over the config.put(...) line from producerFactory() method, change from ProducerConfig to ConsumerConfig, change from SERIALIZER to DESERIALIZER
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // Replace to use StringDeserializer.class
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // You can put any name for this listener/subscriber, just to differentiate this listener from other listeners in case if you have a lot of Kafka listeners.
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "myGroupId");

        // For the Consumer, not only you have to give the configurations that we have set up, but also give the instance of the object for Deserialization. the JsonDeserializer object you need to give the object explicitly.
        // return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(SimpleModel.class));
        // Replace to use StringDeserializer.class
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new StringDeserializer());
    }

    // For Consumer, you also need to have a listener by default
    // You must name this method as kafkaListenerContainerFactory, as this will replace the real kafkaListenerContainerFactory in the Spring Boot context
    @Bean
    public ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();

        // ConcurrentKafkaListenerContainerFactory doesn't take ConsumerFactory as constructor argument, use setter
        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

        return concurrentKafkaListenerContainerFactory;
    }

    // When you run the application, you will see a lot of related consumer client logs appear, indicating successfully registered.
    // myGroupId: partitions assigned: [myTopic-0]


    // Additional knowledge: Use Google GSON 3rd party JSON Serializer and Deserializer library to
    // This is used to create custom serialization or deserialization
    @Bean
    public Gson jsonConverter() {
        return new Gson();
    }

    // The instructor intents to converts whatever String comes from the HTML request into Gson object and convert it to any POJO object.
}
