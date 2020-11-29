package com.example.spring.boot.kafka.kafkatest.services.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service used to connect to Kafka and generate/consume topics dynamically.
 * Main Reference: https://developer.okta.com/blog/2019/11/19/java-kafka
 */
@Service
public class KafkaServiceImpl implements KafkaService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String defaultKafkaBroker;

    private final String defaultGroupId;

    @Autowired
    KafkaServiceImpl(@Value("${kafka.consumer.bootstrap-servers}") String defaultKafkaBroker,
                     @Value("${kafka.consumer.group-id}") String defaultGroupId) {
        this.defaultKafkaBroker = defaultKafkaBroker;
        this.defaultGroupId = defaultGroupId;
    }

    /**
     * Add message into Kafka.
     *
     * @param id:      An identifier for the Kafka topic.
     * @param message: A String converted from object(typically WebSocketMessage object) to be store into Kafka.
     */
    @Override
    public void addMessage(String id, String message) {
        logger.info("Adding message into Kafka with ID: {}", id);
        // Find the user in Kafka topics.

        // Send messages into topics.
    }

    /**
     * Add message into Kafka topic based on @param id.
     *
     * @param id: An identifier used to find the relative topic in Kafka.
     * @return A list of messages from Kafka topic, typically can be converted into WebSocketMessage.
     */
    @Override
    public List<String> getMessages(String id) {
        logger.info("Get messages from Kafka using ID: {}", id);
        // Find the user in Kafka topics.

        // Get current not yet send messages.

        return new ArrayList<>();
    }

    /******************************Kafka Producer Methods**********************************/
    /**
     * Create configurations for Kafka Producer. Adds standard configuration for Kafka Producer.
     *
     * @param additionalConfigurations: Additional configurations for Kafka Producer.
     * @return Map<String, Object> for Kafka Producer.
     */
    @Override
    public Map<String, Object> generateProducerConfigurations(Map<String, Object> additionalConfigurations) {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultKafkaBroker);
        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }
        return standardProducerConfigurations(configurations);
    }

    /**
     * Create configurations for Kafka Producer. Adds standard configuration for Kafka Producer.
     *
     * @param kafkaBroker:              Optional parameter for override address for the Kafka broker.
     * @param additionalConfigurations: Additional configurations for Kafka Producer.
     * @return Map<String, Object> for Kafka Producer.
     */
    @Override
    public Map<String, Object> generateProducerConfigurations(String kafkaBroker, Map<String, Object> additionalConfigurations) {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }
        return standardProducerConfigurations(configurations);
    }

    /**
     * @param configurations: Configurations for other Kafka Producer methods.
     * @return Final configurations for Kafka Producer.
     */
    private Map<String, Object> standardProducerConfigurations(Map<String, Object> configurations) {
        configurations.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configurations.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return configurations;
    }

    /**
     * Create a Kafka instance using producer factory class of Kafka.
     *
     * @param producerConfigurations: Producer Configurations from generateProducerConfigurations(...).
     * @return ProducerFactory object which has information of Kafka Producer.
     */
    public ProducerFactory<String, String> generateProducerFactory(Map<String, Object> producerConfigurations) {
        return new DefaultKafkaProducerFactory<>(producerConfigurations);
    }

    /**
     * Create a Kafka Template, ready to send messages.
     *
     * @param producerFactory: ProducerFactory object, generated from generateProducerFactory(...)
     * @return KafkaTemplate object.
     */
    public KafkaTemplate generateKafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /******************************Kafka Producer Methods END**********************************/

    /******************************Kafka Consumer Methods**********************************/
    /**
     * Create configurations for Kafka Consumer. Adds standard configuration for Kafka Consumer.
     *
     * @param additionalConfigurations: Additional configurations for Kafka Consumer.
     * @return Map<String, Object> for Kafka Consumer.
     */
    @Override
    public Map<String, Object> generateConsumerConfigurations(Map<String, Object> additionalConfigurations) {
        Map<String, Object> configurations = new HashMap<>();

        configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultKafkaBroker);
        configurations.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId);

        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }

        return generateStandardConsumerConfigurations(configurations);
    }

    /**
     * Create configurations for Kafka Consumer. Adds standard configuration for Kafka Consumer.
     *
     * @param kafkaBroker:              Optional parameter for Kafka broker.
     * @param groupId:                  Optional Group ID for Kafka Consumer.
     * @param additionalConfigurations: Additional configurations for Kafka Consumer.
     * @return Map<String, Object> for Kafka Consumer.
     */
    @Override
    public Map<String, Object> generateConsumerConfigurations(String kafkaBroker, String groupId, Map<String, Object> additionalConfigurations) {
        Map<String, Object> configurations = new HashMap<>();

        if (StringUtils.hasText(kafkaBroker)) {
            configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        } else {
            configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultKafkaBroker);
        }

        if (StringUtils.hasText(groupId)) {
            configurations.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        } else {
            configurations.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId);
        }

        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }

        return generateStandardConsumerConfigurations(configurations);
    }

    /**
     * Add standard Kafka Consumer configurations here.
     *
     * @param configurations: Configurations for other Kafka consumer methods.
     * @return Final configurations for Kafka Consumer.
     */
    private Map<String, Object> generateStandardConsumerConfigurations(Map<String, Object> configurations) {
        configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return configurations;
    }

    /**
     * Create a Kafka instance using Consumer factory class of Kafka.
     *
     * @param consumerConfigurations: Consumer Configurations from generateConsumerConfigurations(...).
     * @return ConsumerFactory object which has information of Kafka Consumer.
     */
    @Override
    public ConsumerFactory<String, String> generateConsumerFactory(Map<String, Object> consumerConfigurations) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigurations);
    }

    /**
     * Create a Listener to ready get messages from a Kafka topic.
     *
     * @param consumerFactory: ConsumerFactory object, generated from generateConsumerFactory(...)
     * @return ConcurrentKafkaListenerContainerFactory object.
     */
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    // How to use Kafka Consumer:
    // https://www.tutorialspoint.com/apache_kafka/apache_kafka_consumer_group_example.htm

    /******************************Kafka Consumer Methods END**********************************/
}
