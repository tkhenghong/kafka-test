package com.example.spring.boot.kafka.kafkatest.services.kafka;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.List;
import java.util.Map;

public interface KafkaService {
    void addMessage(String id, String message);

    List<String> getMessages(String id);

    Map<String, Object> generateProducerConfigurations(Map<String, Object> additionalConfigurations);

    Map<String, Object> generateProducerConfigurations(String kafkaBroker, Map<String, Object> additionalConfigurations);

    ProducerFactory<String, String> generateProducerFactory(Map<String, Object> producerConfigurations);

    KafkaTemplate generateKafkaTemplate(ProducerFactory<String, String> producerFactory);

    Map<String, Object> generateConsumerConfigurations(Map<String, Object> additionalConfigurations);

    Map<String, Object> generateConsumerConfigurations(String kafkaBroker, String groupId, Map<String, Object> additionalConfigurations);

    ConsumerFactory<String, String> generateConsumerFactory(Map<String, Object> consumerConfigurations);

    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory);
}
