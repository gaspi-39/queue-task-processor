package dev.gaspar.taskprocessor.task.dto;

import java.util.UUID;

import dev.gaspar.taskprocessor.task.TaskStatus;

import java.time.Instant;


public record TaskResponse(UUID id, TaskStatus status, Instant createdAt){}
