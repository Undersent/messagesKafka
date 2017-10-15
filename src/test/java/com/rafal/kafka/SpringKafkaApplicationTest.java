package com.rafal.kafka;

import com.rafal.kafka.consumer.Receiver;
import com.rafal.kafka.producer.Sender;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringKafkaApplicationTest {
    protected final static String HELLOKAFKA_TOPIC = "helloKafka.t";

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    @Autowired
    private Receiver receiver;
    @Autowired
    private Sender sender;
    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, HELLOKAFKA_TOPIC);

    @Before
    public void runBeforeTestMethod() throws Exception {
        // wait cause all partitions must be assigned
        for(MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
                .getListenerContainers()){
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    kafkaEmbedded.getPartitionsPerTopic());
        }
    }
    @Test
    public void testReceive() throws Exception {
        sender.send(HELLOKAFKA_TOPIC, "Hello Spring Kafka!");

        receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
        assertThat(receiver.getLatch().getCount()).isEqualTo(0);
    }
}
