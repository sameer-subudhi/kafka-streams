package com.learn2code.kafka.streams.config;

import com.learn2code.kafka.streams.model.Book;
import com.learn2code.kafka.streams.topology.KafkaTopology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.support.serializer.JsonSerde;

import static org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_BUILDER_BEAN_NAME;
import static org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME;

@EnableKafka
@EnableKafkaStreams
@Configuration
public class KafkaStreamsConfig {

    Logger logger = LoggerFactory.getLogger(KafkaStreamsConfig.class);

    private final String inputTopic;

    @Autowired
    private KafkaTopology topologyBuilder;

    public KafkaStreamsConfig(@Value("${spring.kafka.topics.input}") String inputTopic) {
        super();
        this.inputTopic = inputTopic;
    }

    @Bean(name = DEFAULT_STREAMS_BUILDER_BEAN_NAME)
    public StreamsBuilderFactoryBean defaultKafkaStreamsBuilder(
            @Qualifier(DEFAULT_STREAMS_CONFIG_BEAN_NAME) ObjectProvider<KafkaStreamsConfiguration> streamsConfigProvider) {
        KafkaStreamsConfiguration streamsConfig = streamsConfigProvider.getIfAvailable();
        CleanupConfig cleanupConfig = new CleanupConfig();
        if (streamsConfig != null) {
            StreamsBuilderFactoryBean factoryBean = new StreamsBuilderFactoryBean(streamsConfig, cleanupConfig);
            //factoryBean.setStateRestoreListener(stateRestoreListener);
            factoryBean.setUncaughtExceptionHandler((t, e) -> {
                logger.error("Unhandled exception caught, attempting to close KafkaStreams", e);
                factoryBean.getKafkaStreams().close();
                factoryBean.getKafkaStreams().cleanUp();
            });
            return factoryBean;
        } else {
            throw new UnsatisfiedDependencyException(KafkaStreamsConfig.class.getName(),
                    DEFAULT_STREAMS_BUILDER_BEAN_NAME, "streamsConfig", "There is no '" +
                    DEFAULT_STREAMS_CONFIG_BEAN_NAME + "' " + KafkaStreamsConfiguration.class.getName() +
                    " bean in the application context.");
        }
    }


    @Bean
    public KStream<?, ?> kStream(StreamsBuilder streamsBuilder) {
        logger.info("************** INPUT TOPIC: " + inputTopic + " **************");
        KStream<String, Book> enrichedStream = streamsBuilder.stream(inputTopic,
                Consumed.with(Serdes.String(), new JsonSerde<>(Book.class)));
        topologyBuilder.processRecords(enrichedStream);
        return enrichedStream;
    }

}
