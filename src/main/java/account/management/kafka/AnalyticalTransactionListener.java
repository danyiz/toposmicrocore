package account.management.kafka;

import account.management.repository.AnalyticalTransactions;
import account.management.model.AnalyticalTransactionDTO;
import account.management.model.PostingResult;
import account.management.service.AnalyticalPosting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
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

    @KafkaListener(topics = "${spring.kafka.topic.name.consumer}"/*, containerFactory = "analyticalTransactionConcurrentKafkaListenerContainerFactory"*/)
    @SendTo
    public Message<?> transactionListener( ConsumerRecord<String, Object> kafkaMessage) throws JsonProcessingException, InterruptedException {

        log.info("Topic: {}", kafkaMessage.topic());
        log.info("Key: {}", kafkaMessage.key());
        log.info("Headers: {}", kafkaMessage.headers());
        log.info("Partion: {}", kafkaMessage.partition());
        log.info("Offset: {}", kafkaMessage.offset());

        var analyticalTransactionDTO = objectMapper.readValue(String.valueOf(kafkaMessage.value()),AnalyticalTransactionDTO.class);

        log.debug("Payload: {}",objectMapper.writeValueAsString(analyticalTransactionDTO));

        analyticalPosting.createAnalyticalTransaction(analyticalTransactionDTO);

        var postingResults = PostingResult.builder()
                .processID(analyticalTransactionDTO.getProcessID())
                .postingStatus("ACCEPTED")
                .transactionID(analyticalTransactionDTO.getTransactionID())
                .batchID(analyticalTransactionDTO.getProcessID())
                .build();

        Headers nativeHeaders = kafkaMessage.headers();
        byte[] replyTo = nativeHeaders.lastHeader(KafkaHeaders.REPLY_TOPIC).value();
        byte[] correlation = nativeHeaders.lastHeader(KafkaHeaders.CORRELATION_ID).value();
        return MessageBuilder.withPayload(objectMapper.writeValueAsString(postingResults))
                .setHeader(KafkaHeaders.KEY,kafkaMessage.key())
                .setHeader(KafkaHeaders.CORRELATION_ID, correlation)
                .setHeader(KafkaHeaders.TOPIC, replyTo)
                .build();
    }
}
