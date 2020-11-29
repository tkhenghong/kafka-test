package com.example.spring.boot.kafka.kafkatest;

import com.example.spring.boot.kafka.kafkatest.services.kafka.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

// Tutorial links:
// https://medium.com/@Ankitthakur/apache-kafka-installation-on-mac-using-homebrew-a367cdefd273
// https://www.baeldung.com/spring-kafka
// https://www.udemy.com/course/apache-kafka-and-spring-boot-consumer-producer
@SpringBootApplication
public class KafkaTestApplication {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaService kafkaService;

    public static void main(String[] args) {
        SpringApplication.run(KafkaTestApplication.class, args);
    }

    @Autowired
    public KafkaTestApplication (KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Bean("customKafkaTemplate")
    public KafkaTemplate generateKafkaTemplate() {
        logger.info("generateKafkaTemplate()");
        Map<String, Object> producerConfigurations = kafkaService.generateProducerConfigurations(null);
        ProducerFactory<String, String>  producerFactory = kafkaService.generateProducerFactory(producerConfigurations);
        return kafkaService.generateKafkaTemplate(producerFactory);
    }
}
