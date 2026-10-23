package dev.gaspar.taskprocessor.task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dev.gaspar.taskprocessor.task.dto.CreateTaskRequest;
import dev.gaspar.taskprocessor.task.dto.TaskResponse;

@Component
public class PeriodicTaskJob {
  private final TaskService service;
  private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicTaskJob.class);

  public PeriodicTaskJob(TaskService service) {
    this.service = service;
  }

  @Scheduled(cron = "${task.periodic.cron:0 */5 * * * *}")
  public void periodicTask() {
    TaskResponse res = service.createTask(new CreateTaskRequest("periodic", "cron", 0));
    LOGGER.info("periodic task enqueued: {}", res.id());
  }

}
