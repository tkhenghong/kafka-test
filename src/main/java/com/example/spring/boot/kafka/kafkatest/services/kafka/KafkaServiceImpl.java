package com.example.spring.boot.kafka.kafkatest.services.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Duration;
import java.time.LocalDateTime;
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

    private final String producerBootstrapServers;

    private final String defaultProducerClientId;

    private final String consumerBootstrapServers;

    private final String defaultConsumerClientId;

    private final String defaultConsumerGroupId;

    @Autowired
    KafkaServiceImpl(@Value("${kafka.producer.client-id}") String defaultProducerClientId,
                     @Value("${kafka.consumer.client-id}") String defaultConsumerClientId,
                     @Value("${kafka.consumer.group-id}") String defaultConsumerGroupId,
                     @Value("${kafka.producer.bootstrap-servers}") String producerBootstrapServers,
                     @Value("${kafka.consumer.bootstrap-servers}") String consumerBootstrapServers) {
        this.defaultProducerClientId = defaultProducerClientId;
        this.defaultConsumerClientId = defaultConsumerClientId;
        this.producerBootstrapServers = producerBootstrapServers;
        this.consumerBootstrapServers = consumerBootstrapServers;
        this.defaultConsumerGroupId = defaultConsumerGroupId;
    }

    /**
     * Add message into Kafka.
     *
     * @param userId:     User ID to be identified in Kafka, allow better troubleshooting.
     * @param kafkaTopic: An identifier for the Kafka topic.
     * @param message:    A String converted from object(typically WebSocketMessage object) to be store into Kafka.
     */
    @Override
    public void addMessage(String userId, String kafkaTopic, String message) {
        logger.info("Adding message into kafkaTopic: {}", kafkaTopic);

        Map<String, Object> producerConfigurations = generateProducerConfigurations(null);
        if (StringUtils.hasText(userId)) {
            producerConfigurations.put(ProducerConfig.CLIENT_ID_CONFIG, userId);
        }

        ProducerFactory<String, String> producerFactory = generateProducerFactory(producerConfigurations);

        KafkaTemplate kafkaTemplate = generateKafkaTemplate(producerFactory);

        kafkaTemplate.send(kafkaTopic, message).addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                logger.info("Message has failed to sent to {}.", kafkaTopic);
                // TODO: Saved failed Kafka messages to DB table called PendingKafkaMessages.
            }

            @Override
            public void onSuccess(Object result) {
                logger.info("Message has been sent to {} successfully.", kafkaTopic);
            }
        });
    }

    /**
     * Add message into Kafka.
     * Some reference: http://cloudurable.com/blog/kafka-tutorial-kafka-producer/index.html
     *
     * @param kafkaTopic: An identifier for the Kafka topic.
     * @param message:    A String converted from object(typically WebSocketMessage object) to be store into Kafka.
     */
    @Override
    public void addMessage(String kafkaTopic, String message) {
        logger.info("Adding message into kafkaTopic: {}", kafkaTopic);

        Map<String, Object> producerConfigurations = generateProducerConfigurations(null);

        ProducerFactory<String, String> producerFactory = generateProducerFactory(producerConfigurations);

        KafkaTemplate kafkaTemplate = generateKafkaTemplate(producerFactory);

        kafkaTemplate.send(kafkaTopic, message).addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                logger.info("Message has failed to sent to {}.", kafkaTopic);
                // TODO: Saved failed Kafka messages to DB table called PendingKafkaMessages.
            }

            @Override
            public void onSuccess(Object result) {
                logger.info("Message has been sent to {} successfully.", kafkaTopic);
                kafkaTemplate.flush();
            }
        });
    }

    /**
     * Add message into Kafka topic based on @param id.
     *
     * @param kafkaTopic: A list of identifiers used to find the relative topic in Kafka.
     * @return A list of messages from Kafka topic, typically can be converted into WebSocketMessage.
     */
    @Override
    public List<String> getMessages(String userId, String kafkaTopic) {
        logger.info("getMessages()");
        logger.info("kafkaTopic: {}", kafkaTopic);

        List<String> messages = new ArrayList<>();

        // Create Kafka Consumer Factory.
        Map<String, Object> consumerConfigurations = generateConsumerConfigurations(null);

        ConsumerFactory<String, String> consumerFactory = generateConsumerFactory(consumerConfigurations);

        ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory =
                kafkaListenerContainerFactory(consumerFactory);

        // Create KafkaConsumer.
        ConsumerFactory consumerFactory1 = concurrentKafkaListenerContainerFactory.getConsumerFactory();
        Consumer consumer = consumerFactory1.createConsumer();

        List<String> kafkaTopics = new ArrayList<>();
        kafkaTopics.add(kafkaTopic);
        consumer.subscribe(kafkaTopics);

        // Polling Consumer records.
        LocalDateTime now = LocalDateTime.now();
        // A polling for Consumer is a mechanism for the consumer app to wait for Kafka to return the result of a topic's messages.
        // https://stackoverflow.com/questions/41030854/kafka-consumer-polling-timeout
        ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.between(now, now.plusSeconds(1)));
        logger.info("consumerRecords.count(): {}", consumerRecords.count());

        logger.info("kafkaTopic: {}", kafkaTopic);
        // Getting messages from one topic from ConsumerRecords object.
        Iterable<ConsumerRecord<String, String>> consumerRecordIterable = consumerRecords.records(kafkaTopic);
        consumerRecordIterable.forEach(consumerRecord -> {
            String message = consumerRecord.value();
            logger.info("Extracted message: {}.", message);
            messages.add(message);

            consumer.commitSync(); // Acknowledge this message has been received.
        });

        consumer.close();

        return messages;
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
        Map<String, Object> configurations = standardProducerConfigurations();
        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }
        return configurations;
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
        Map<String, Object> configurations = standardProducerConfigurations();
        configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }
        return configurations;
    }

    /**
     * For more standard configuration of Producer, see: https://kafka.apache.org/documentation/#producerconfigs
     *
     * @return Final configurations for Kafka Producer.
     */
    private Map<String, Object> standardProducerConfigurations() {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
        configurations.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configurations.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configurations.put(ProducerConfig.CLIENT_ID_CONFIG, defaultProducerClientId);
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
        Map<String, Object> configurations = generateStandardConsumerConfigurations();

        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }

        return configurations;
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
        Map<String, Object> configurations = generateStandardConsumerConfigurations();

        if (StringUtils.hasText(kafkaBroker)) {
            configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        }

        if (StringUtils.hasText(groupId)) {
            configurations.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        }

        if (!ObjectUtils.isEmpty(additionalConfigurations)) {
            configurations.putAll(additionalConfigurations);
        }

        return configurations;
    }

    /**
     * Add standard Kafka Consumer configurations here.
     *
     * @return Final configurations for Kafka Consumer.
     */
    private Map<String, Object> generateStandardConsumerConfigurations() {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
        configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configurations.put(ConsumerConfig.CLIENT_ID_CONFIG, defaultConsumerClientId);
        configurations.put(ConsumerConfig.GROUP_ID_CONFIG, defaultConsumerGroupId);
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
