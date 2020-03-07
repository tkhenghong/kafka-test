package com.example.spring.boot.kafka.kafkatest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Tutorial links:
// https://medium.com/@Ankitthakur/apache-kafka-installation-on-mac-using-homebrew-a367cdefd273
// https://www.baeldung.com/spring-kafka
// https://www.udemy.com/course/apache-kafka-and-spring-boot-consumer-producer
@SpringBootApplication
public class KafkaTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTestApplication.class, args);
    }

}
