package account.management.kafka;

import account.management.entity.AnalyticalTransaction;
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

    @Autowired
    KafkaTemplate<?, ?>  kafkaTemplate;


    @KafkaListener(topics = "${spring.kafka.topic.name.consumer}", containerFactory = "analyticalTransactionConcurrentKafkaListenerContainerFactory")
    @SendTo
    public Message consume(ConsumerRecord kafkaMessage, AnalyticalTransactionDTO analyticalTransactionDTO) throws JsonProcessingException {
        log.info("Topic: {}", kafkaMessage.topic());
        log.info("Key: {}", kafkaMessage.key());
        log.info("Headers: {}", kafkaMessage.headers());
        log.info("Partion: {}", kafkaMessage.partition());
        log.info("Offset: {}", kafkaMessage.offset());
        log.info("Order: {}", kafkaMessage.value());
        log.info("Transaction {}",objectMapper.writeValueAsString(analyticalTransactionDTO));
        AnalyticalTransaction transaction = analyticalPosting.createAnalyticalTransaction(analyticalTransactionDTO);


     var postingResults = PostingResult.builder()
                .processID(transaction.getProcessID())
                .postingStatus("SUCCEED")
                .transactionID(transaction.getTransactionID())
                .batchID(transaction.getBatchID())
                .build();


        Headers nativeHeaders = kafkaMessage.headers();
       byte[] replyTo = nativeHeaders.lastHeader(KafkaHeaders.REPLY_TOPIC).value();
        byte[] correlation = nativeHeaders.lastHeader(KafkaHeaders.CORRELATION_ID).value();
        return MessageBuilder.withPayload(postingResults)
                .setHeader(KafkaHeaders.MESSAGE_KEY,kafkaMessage.key())
                .setHeader(KafkaHeaders.CORRELATION_ID, correlation)
                .setHeader(KafkaHeaders.TOPIC, replyTo)
                .build();
    }

}

/*
@KafkaListener(id = "so55622224", topics = "so55622224")
@SendTo("dummy.we.use.the.header.instead")
public Message<?> listen(Message<String> in) {
    System.out.println(in);
    Headers nativeHeaders = in.getHeaders().get(KafkaHeaders.NATIVE_HEADERS, Headers.class);
    byte[] replyTo = nativeHeaders.lastHeader(KafkaHeaders.REPLY_TOPIC).value();
    byte[] correlation = nativeHeaders.lastHeader(KafkaHeaders.CORRELATION_ID).value();
    return MessageBuilder.withPayload(in.getPayload().toUpperCase())
            .setHeader("myHeader", nativeHeaders.lastHeader("myHeader").value())
            .setHeader(KafkaHeaders.CORRELATION_ID, correlation)
            .setHeader(KafkaHeaders.TOPIC, replyTo)
            .build();
}

// This is used to send the reply - needs a header mapper
@Bean
public KafkaTemplate<?, ?> kafkaTemplate(ProducerFactory<Object, Object> kafkaProducerFactory) {
    KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);
    MessagingMessageConverter messageConverter = new MessagingMessageConverter();
    messageConverter.setHeaderMapper(new SimpleKafkaHeaderMapper("*")); // map all byte[] headers
    kafkaTemplate.setMessageConverter(messageConverter);
    return kafkaTemplate;
}

@Bean
public ApplicationRunner runner(ReplyingKafkaTemplate<String, String, String> template) {
    return args -> {
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("myHeader", "myHeaderValue".getBytes()));
        headers.add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "so55622224.replies".getBytes())); // automatic in 2.2
        ProducerRecord<String, String> record = new ProducerRecord<>("so55622224", null, null, "foo", headers);
        RequestReplyFuture<String, String, String> future = template.sendAndReceive(record);
        ConsumerRecord<String, String> reply = future.get();
        System.out.println("Reply: " + reply.value() + " myHeader="
                + new String(reply.headers().lastHeader("myHeader").value()));
    };
}
* */
