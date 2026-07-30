package dev.gaspar.taskprocessor.task;
import java.time.Instant;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import dev.gaspar.taskprocessor.task.dto.CreateTaskRequest;
import dev.gaspar.taskprocessor.task.dto.TaskResponse;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public TaskService(TaskRepository repository, RabbitTemplate rabbitTemplate){
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }
    public TaskResponse createTask(CreateTaskRequest req){
        Task task = new Task(UUID.randomUUID(), Instant.now(), req.payload(), TaskStatus.PENDING, req.type());
        repository.save(task);
        rabbitTemplate.convertAndSend(TaskQueueConfig.EXCHANGE_NAME, TaskQueueConfig.ROUTING_KEY, task.getId().toString());
        return new TaskResponse(task.getId(), task.getStatus(), task.getCreatedAt());
    }
    public void publishRetry (Task task) {
        int ttlMs = (1 << task.getTries()) * 10_000;
        rabbitTemplate.convertAndSend("", TaskQueueConfig.RETRY_QUEUE_NAME, task.getId().toString(), message -> {
        message.getMessageProperties().setExpiration(String.valueOf(ttlMs));
        return message;
});
    }

}
