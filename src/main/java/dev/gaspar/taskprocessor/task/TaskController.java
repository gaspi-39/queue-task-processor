package dev.gaspar.taskprocessor.task;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    @GetMapping("/tasks/failed")
    public ResponseEntity<List<TaskResponse>> getFailed(){
        return ResponseEntity.ok().body(service.getByStatus(TaskStatus.FAILED));
    }

}
