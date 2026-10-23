package dev.gaspar.taskprocessor.task.dto;
import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScheduleTaskRequest(@NotBlank String type, @NotBlank String payload, @NotNull @Future Instant scheduleFor){}
