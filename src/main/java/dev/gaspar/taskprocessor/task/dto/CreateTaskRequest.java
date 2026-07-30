package dev.gaspar.taskprocessor.task.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(@NotBlank String type, @NotBlank String payload){}