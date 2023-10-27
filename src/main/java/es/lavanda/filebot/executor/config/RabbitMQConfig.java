package es.lavanda.filebot.executor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_MESSAGES = "filebot-telegram-resolution";
    public static final String QUEUE_MESSAGES_DLQ = "filebot-telegram-resolution-dlq";

    public static final String QUEUE_MESSAGES_EXECUTION = "filebot-new-execution-resolution";
    public static final String QUEUE_MESSAGES_EXECUTION_DLQ = "filebot-new-execution-resolution-dlq";

    public static final String EXCHANGE_MESSAGES = "lavandadelpatio-exchange";

    @Bean
    DirectExchange messagesExchange() {
        return new DirectExchange(EXCHANGE_MESSAGES);
    }

    @Bean
    Binding bindingMessages() {
        return BindingBuilder.bind(messagesQueue()).to(messagesExchange()).with(QUEUE_MESSAGES);
    }

    @Bean
    Queue messagesQueue() {
        return QueueBuilder.durable(QUEUE_MESSAGES).withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_MESSAGES_DLQ).build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_MESSAGES_DLQ).build();
    }

    @Bean
    Binding bindingMessagesExecution() {
        return BindingBuilder.bind(messagesQueue()).to(messagesExchange()).with(QUEUE_MESSAGES_EXECUTION);
    }

    @Bean
    Queue messagesQueueExecution() {
        return QueueBuilder.durable(QUEUE_MESSAGES_EXECUTION).withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_MESSAGES_EXECUTION_DLQ).build();
    }

    @Bean
    Queue deadLetterQueueExecution() {
        return QueueBuilder.durable(QUEUE_MESSAGES_EXECUTION_DLQ).build();
    }

}
