package com.taskremainder.app.controller;

import com.taskremainder.app.entity.Task;
import com.taskremainder.app.enums.TaskPriority;
import com.taskremainder.app.enums.TaskStatus;
import com.taskremainder.app.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    // ✅ Constructor injection (BEST PRACTICE)
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ================= LIST TASKS WITH PAGINATION =================
    @GetMapping
    public String listTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String dueDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Task> tasks = taskService.getTasks(
                keyword,
                status,
                priority,
                dueDate,
                pageable
        );

        model.addAttribute("tasks", tasks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tasks.getTotalPages());

        // preserve filters in pagination
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("dueDate", dueDate);

        return "tasks";
    }

    // ================= ADD TASK =================
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        return "add-task";
    }

    @PostMapping("/add")
    public String saveTask(@ModelAttribute Task task) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdated(false);
        taskService.addTask(task);
        return "redirect:/tasks";
    }

    // ================= EDIT TASK =================
    @GetMapping("/edit/{id}")
    public String editTask(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return taskService.findById(id)
                .map(task -> {
                    model.addAttribute("task", task);
                    model.addAttribute("statuses", TaskStatus.values());
                    model.addAttribute("priorities", TaskPriority.values());
                    return "edit-task";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Task not found");
                    return "redirect:/tasks";
                });
    }

    @PostMapping("/edit")
    public String updateTask(@ModelAttribute Task task) {
        Task existing = taskService.findById(task.getId()).orElse(null);
        if (existing == null) return "redirect:/tasks";

        task.setCreatedAt(existing.getCreatedAt());
        task.setUpdated(true);
        taskService.updateTask(task);
        return "redirect:/tasks";
    }

    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    // MARK DONE
    @GetMapping("/markdown/{id}")
    public String markDone(@PathVariable Integer id) {

        Task task = taskService.findById(id).orElse(null);
        if (task != null) {
            task.setStatus(TaskStatus.DONE);
            task.setCompletedAt(java.time.LocalDate.now());
            taskService.updateTask(task);
        }
        return "redirect:/tasks";
    }


    // ================= VIEW =================
    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return taskService.findById(id)
                .map(task -> {
                    model.addAttribute("task", task);
                    return "view-task";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "No task found");
                    return "redirect:/tasks";
                });
    }
}
