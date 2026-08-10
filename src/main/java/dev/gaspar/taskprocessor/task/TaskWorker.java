package dev.gaspar.taskprocessor.task;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.rabbitmq.client.Channel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class TaskWorker {
    private final TaskRepository repository;
    private final TaskService service;
    private static final Integer MAX_TRIES = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskWorker.class);

    public TaskWorker(TaskRepository repository, TaskService service){
        this.repository = repository;
        this.service = service;
    }
    @RabbitListener(queues = TaskQueueConfig.QUEUE_NAME)
    public void reciveTask(String id, Message msg, Channel channel) throws IOException{
        UUID uuid = UUID.fromString(id);
        Optional<Task> task = repository.findById(uuid);

        if (!task.isEmpty()){

            Task task2 = task.get();
            task2.setProcessing();
            repository.save(task2);

            try {
                String condition = "fail";

                if (task2.getPayload().equals(condition)) {
                    throw new RuntimeException("task failed");
                }

                task2.setDone("task succesfully");

            } catch (RuntimeException e) {

                task2.setTries(task2.getTries() + 1);
                
                if (task2.getTries() < MAX_TRIES) {
                    this.service.publishRetry(task2);
                } else {
                    task2.setFailed(e.toString());
                    this.service.publishDlq(task2);
                }
            }
            repository.save(task2);
        } else {
            LOGGER.warn("task with id {} not found", id);
        }
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }
}
