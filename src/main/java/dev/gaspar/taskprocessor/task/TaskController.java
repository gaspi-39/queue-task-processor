package dev.gaspar.taskprocessor.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.gaspar.taskprocessor.task.dto.CreateTaskRequest;
import dev.gaspar.taskprocessor.task.dto.TaskResponse;
import jakarta.validation.Valid;

@RestController
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service){
        this.service = service;
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest req){
        return ResponseEntity.accepted().body(service.createTask(req));
    }

}
