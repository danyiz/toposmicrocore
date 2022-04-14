package account.management.kafka;

import account.management.model.AnalyticalTransactionDTO;
import account.management.service.AnalyticalPosting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnalyticalTransactionListener {

    @Value("${spring.kafka.topic.name.consumer}")
    private String topicName;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AnalyticalPosting analyticalPosting;


    @KafkaListener(topics = "${spring.kafka.topic.name.consumer}", containerFactory = "analyticalTransactionConcurrentKafkaListenerContainerFactory")
    public void consume(ConsumerRecord kafkaMessage, AnalyticalTransactionDTO analyticalTransactionDTO) throws JsonProcessingException {
        log.debug("Topic: {}", kafkaMessage.topic());
        log.debug("Key: {}", kafkaMessage.key());
        log.debug("Headers: {}", kafkaMessage.headers());
        log.debug("Partion: {}", kafkaMessage.partition());
        log.debug("Offset: {}", kafkaMessage.offset());
        log.debug("Order: {}", kafkaMessage.value());
        log.debug("Transaction {}",objectMapper.writeValueAsString(analyticalTransactionDTO));
        analyticalPosting.createAnalyticalTransaction(analyticalTransactionDTO);

    }

}
