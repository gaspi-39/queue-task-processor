package dev.gaspar.taskprocessor.task;

import java.time.Instant;
import java.util.UUID;

import org.springframework.lang.Nullable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder.In;

@Entity
public class Task {
    @Id
    private UUID id;
    private Instant createdAt;
    private String payload;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private String type;
    private String result;
    @Nullable
    private Integer tries;

    protected Task(){}
    public Task(UUID id, Instant createdAt, String payload, TaskStatus status, String type){
        this.id = id;
        this.createdAt = createdAt;
        this.payload = payload;
        this.status = status;
        this.type = type;
        this.tries = 0;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public UUID getId() {
        return id;
    }
    public String getPayload() {
        return payload;
    }
    public TaskStatus getStatus() {
        return status;
    }
    public String getType() {
        return type;
    }
    public String getResult() {
        return result;
    }
    public Integer getTries() {
        return tries;
    }
    public void setTries(int tries) {
        this.tries = tries;
    }
    public void setProcessing(){
        this.status = TaskStatus.PROCESSING;
    }
    public void setDone(String result){
        this.status = TaskStatus.DONE;
        this.result = result;
    }
    public void setFailed(String error){
        this.status = TaskStatus.FAILED;
        this.result = error;
    }
}
