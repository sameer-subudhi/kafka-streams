package com.learn2code.kafka.streams.topology;


import com.learn2code.kafka.streams.model.Book;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;


@Service
public class KafkaTopology {
    Logger logger = LoggerFactory.getLogger(KafkaTopology.class);

    private String outputTopic;

    public KafkaTopology(@Value("${spring.kafka.topics.output}") String outputTopic) {
        super();
        this.setOutputTopic(outputTopic);
    }

    private void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public void processRecords(KStream<String, Book> inboundStream) {
        logger.info("************ Transformation starts ***********");
        inboundStream.peek((key, value) -> logger.info("Message: {}", inboundStream));
        inboundStream.mapValues(this::transformValue)
                .to(outputTopic, Produced.with(Serdes.String(), new JsonSerde<>(Book.class).noTypeInfo()));

    }

    private Book transformValue(Book value) {
        return value;
    }
}
