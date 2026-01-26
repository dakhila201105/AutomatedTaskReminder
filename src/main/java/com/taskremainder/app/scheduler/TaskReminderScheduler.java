package com.taskremainder.app.scheduler;

import com.taskremainder.app.entity.Task;
import com.taskremainder.app.repository.TaskRepository;
import com.taskremainder.app.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TaskReminderScheduler {

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    public TaskReminderScheduler(TaskRepository taskRepository, EmailService emailService) {
        this.taskRepository = taskRepository;
        this.emailService = emailService;
    }

    // Run on startup for verification
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void init() {
        checkDueTasks();
    }

    // Run every hour to check for due tasks
    @Scheduled(cron = "0 0 * * * *")
    public void checkDueTasks() {
        String today = LocalDate.now().toString();
        // Only log if we find tasks or once a day to avoid spamming logs? 
        // For now, minimal logging is better, but debugging is helpful.
        // Let's log only if tasks are found.
        
        List<Task> tasks = taskRepository.findByDueDateAndReminderSentFalseAndDeletedFalse(today);
        
        if (!tasks.isEmpty()) {
            System.out.println("⏰ Checking for tasks due today: " + today);
            System.out.println("   Found " + tasks.size() + " tasks pending reminder.");
            
            for (Task task : tasks) {
                try {
                    System.out.println("   Sending reminder for task: " + task.getTitle());
                    emailService.sendTaskReminder(task.getUser().getEmail(), task.getTitle(), task.getDueDate());
                    
                    task.setReminderSent(true);
                    taskRepository.save(task);
                } catch (Exception e) {
                    System.err.println("   Error sending reminder for task " + task.getId() + ": " + e.getMessage());
                }
            }
        }
    }
}
