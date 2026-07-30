package dev.gaspar.taskprocessor.task;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskQueueConfig {
    public static final String EXCHANGE_NAME = "task.exchange";
    public static final String QUEUE_NAME = "task.queue";
    public static final String ROUTING_KEY = "task.created";
    public static final String RETRY_QUEUE_NAME = "task.retry";
    @Bean
    public DirectExchange createDirectExchange(){
        return new DirectExchange(EXCHANGE_NAME); 
    }
    @Bean
    public Queue createQueue(){
        return new Queue(QUEUE_NAME);
    }
    @Bean
    public Queue createRetryQueue(){
        Queue queue = QueueBuilder.durable(RETRY_QUEUE_NAME).withArgument("x-dead-letter-exchange", EXCHANGE_NAME).withArgument("x-dead-letter-routing-key",ROUTING_KEY).build();
        return queue;
    }
    @Bean
    public Binding createBinding(Queue createQueue, DirectExchange exchange){
        return BindingBuilder.bind(createQueue).to(exchange).with(ROUTING_KEY);
    }
}
