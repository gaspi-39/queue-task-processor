package dev.gaspar.taskprocessor.task;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.gaspar.taskprocessor.task.dto.CreateTaskRequest;
import dev.gaspar.taskprocessor.task.dto.ScheduleTaskRequest;
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
        rabbitTemplate.convertAndSend(TaskQueueConfig.EXCHANGE_NAME, TaskQueueConfig.ROUTING_KEY, task.getId().toString(), message -> {
            message.getMessageProperties().setPriority(req.priority());
            return message;
        });
        return new TaskResponse(task.getId(), task.getStatus(), task.getCreatedAt(), task.getResult());
    }
    public void publishRetry (Task task) {
        int ttlMs = (1 << task.getTries()) * 10_000;
        rabbitTemplate.convertAndSend("", TaskQueueConfig.RETRY_QUEUE_NAME, task.getId().toString(), message -> {
        message.getMessageProperties().setExpiration(String.valueOf(ttlMs));
        return message;
        });
    }
    public void publishDlq(Task task){
        rabbitTemplate.convertAndSend("", TaskQueueConfig.DLQ_QUEUE_NAME, task.getId().toString());
    }
    public TaskResponse scheduleTask(ScheduleTaskRequest req) {
        long delayMs = Duration.between(Instant.now(), req.scheduleFor()).toMillis();
        if (delayMs < 0) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "executeAt must be in the future");
        }

        if (delayMs > MessageProperties.X_DELAY_MAX) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "executeAt exceeds the maximum delay");
        }

        Task task = new Task(UUID.randomUUID(), Instant.now(), req.payload(), TaskStatus.PENDING, req.type());
        task.setScheduleFor(req.scheduleFor());
        repository.save(task);
        rabbitTemplate.convertAndSend(TaskQueueConfig.SCHEDULE_EXCHANGE_NAME, TaskQueueConfig.ROUTING_KEY, task.getId().toString(), message -> {
          message.getMessageProperties().setDelayLong(delayMs);
            return message;
        });

        return new TaskResponse(task.getId(), task.getStatus(), task.getCreatedAt(), task.getResult());
    }

    public List<TaskResponse> getByStatus (TaskStatus status){
        List<Task> list = this.repository.findByStatus(status);
        List<TaskResponse> responseList = list.stream().map(task -> new TaskResponse(task.getId(), task.getStatus(), task.getCreatedAt(), task.getResult())).toList();
        return responseList;
    }

}
