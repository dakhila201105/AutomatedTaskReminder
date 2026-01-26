package com.taskremainder.app.controller;

import com.taskremainder.app.entity.User;
import com.taskremainder.app.entity.Task;
import com.taskremainder.app.enums.TaskStatus;
import com.taskremainder.app.repository.TaskRepository;
import com.taskremainder.app.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final TaskRepository taskRepository;

    public DashboardController(UserService userService, TaskRepository taskRepository) {
        this.userService = userService;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        User user = userService.getCurrentUser();



        long completedTasks =
                taskRepository.countByUserAndStatusAndDeletedFalse(user, TaskStatus.DONE);

        long pendingTasks =
                taskRepository.countByUserAndStatusAndDeletedFalse(user, TaskStatus.PENDING);

        java.util.List<Task> completedTaskList =
                taskRepository.findByUserAndStatusAndDeletedFalse(user, TaskStatus.DONE);

        double avgCompletionHours = 0.0;

        if (!completedTaskList.isEmpty()) {
            java.util.OptionalDouble avgMinutesOpt = completedTaskList.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCompletedAt() != null)
                    .mapToLong(t -> {
                        java.time.LocalDate createdDate = t.getCreatedAt().toLocalDate();
                        java.time.LocalDate completedDate = t.getCompletedAt();
                        long days = java.time.temporal.ChronoUnit.DAYS.between(createdDate, completedDate);
                        return days * 24L * 60L;
                    })
                    .average();

            if (avgMinutesOpt.isPresent()) {
                avgCompletionHours = avgMinutesOpt.getAsDouble() / 60.0;
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("completed", completedTasks);
        model.addAttribute("pending", pendingTasks);
        model.addAttribute("avgTime", String.format("%.1f", avgCompletionHours));
        model.addAttribute("activePage", "dashboard");

        return "dashboard";
    }
}
