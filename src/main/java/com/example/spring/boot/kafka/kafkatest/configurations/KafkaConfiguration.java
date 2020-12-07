package com.example.spring.boot.kafka.kafkatest.configurations;

import com.example.spring.boot.kafka.kafkatest.services.kafka.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 * Setup default producer and consumer Kafka configuration for the application.
 * <p>
 * NOTE: It's not recommended to use the default producer/consumer KafkaTemplate / ConcurrentKafkaListenerContainerFactory objects sending/listening
 * because it will be difficult to troubleshoot the problems in Kafka log in the future.
 */
@Configuration
// Add @EnableKafka to let this application to listen for Kafka topic
@EnableKafka
public class KafkaConfiguration {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaService kafkaService;

    private final String defaultProducerClientId;

    private final String defaultConsumerClientId;

    private final String defaultConsumerGroupId;

    private final String producerBootstrapServers;

    private final String consumerBootstrapServers;

    @Autowired
    public KafkaConfiguration(KafkaService kafkaService,
                              @Value("${kafka.producer.client-id}") String defaultProducerClientId,
                              @Value("${kafka.consumer.client-id}") String defaultConsumerClientId,
                              @Value("${kafka.consumer.group-id}") String defaultConsumerGroupId,
                              @Value("${kafka.producer.bootstrap-servers}") String producerBootstrapServers,
                              @Value("${kafka.consumer.bootstrap-servers}") String consumerBootstrapServers) {
        this.kafkaService = kafkaService;
        this.defaultProducerClientId = defaultProducerClientId;
        this.defaultConsumerClientId = defaultConsumerClientId;
        this.defaultConsumerGroupId = defaultConsumerGroupId;
        this.producerBootstrapServers = producerBootstrapServers;
        this.consumerBootstrapServers = consumerBootstrapServers;
    }

    /**
     * Default Producer factory for Kafka.
     *
     * @return ProducerFactory<String, String> object.
     */
    @Bean
    ProducerFactory<String, String> producerFactory() {
        Map<String, Object> producerConfigurations = kafkaService.generateProducerConfigurations(null);

        return kafkaService.generateProducerFactory(producerConfigurations);
    }

    /**
     * Default KafkaTemplate object for Kafka.
     *
     * @return KafkaTemplate<String, String> object.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory()); // The KafkaTemplate object loads with the Producer object in it.
    }

    /**
     * Default consumer factory for Kafka Consumer.
     *
     * @return ConsumerFactory<String, String> object.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> consumerConfigurations = kafkaService.generateConsumerConfigurations(null);

        return kafkaService.generateConsumerFactory(consumerConfigurations);
    }

    /**
     * Default Kafka listeners' configuration in the project.
     *
     * @return ConcurrentKafkaListenerContainerFactory<String, String> object.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();

        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

        return concurrentKafkaListenerContainerFactory;
    }

    private void readValueValues(String methodName) {
        logger.info("{} defaultProducerClientId: {}", methodName, defaultProducerClientId);
        logger.info("{} defaultConsumerClientId: {}", methodName, defaultConsumerClientId);
        logger.info("{} defaultConsumerGroupId: {}", methodName, defaultConsumerGroupId);
        logger.info("{} producerBootstrapServers: {}", methodName, producerBootstrapServers);
        logger.info("{} consumerBootstrapServers: {}", methodName, consumerBootstrapServers);
    }
}
