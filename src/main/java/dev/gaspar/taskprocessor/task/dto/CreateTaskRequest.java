package dev.gaspar.taskprocessor.task.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(@NotBlank String type, @NotBlank String payload, @Min(0) @Max(10) int priority){}