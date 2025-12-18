package com.example.demo.controller;

import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ✅ ROOT URL FIX
    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }

    // ✅ LIST TASKS (FIXED)
    @GetMapping("/tasks")
    public String getTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks";
    }

    // ADD TASK PAGE
    @GetMapping("/tasks/add")
    public String showAddForm(Model model) {
        model.addAttribute("task", new Task());
        return "add-task";
    }

    // SAVE TASK
    @PostMapping("/tasks/add")
    public String saveTask(@ModelAttribute Task task, Model model) {

        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Title is required!");
            model.addAttribute("task", task);
            return "add-task";
        }

        if (task.getDueDate() == null || task.getDueDate().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Due Date is required!");
            model.addAttribute("task", task);
            return "add-task";
        }

        task.setCreatedAt(LocalDateTime.now().toString());
        task.setUpdated(false);

        taskService.addTask(task);
        return "redirect:/tasks";
    }

    // EDIT
    @GetMapping("/tasks/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Task task = taskService.findById(id).orElse(null);
        if (task == null) return "redirect:/tasks";

        model.addAttribute("task", task);
        return "edit-task";
    }

    // UPDATE
    @PostMapping("/tasks/update")
    public String updateTask(@ModelAttribute Task task, Model model) {

        Task existing = taskService.findById(task.getId()).orElse(null);
        if (existing == null) return "redirect:/tasks";

        task.setCreatedAt(existing.getCreatedAt());
        task.setUpdated(true);

        taskService.updateTask(task);
        return "redirect:/tasks";
    }

    // DELETE
    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    // MARK DONE
    @GetMapping("/tasks/markdown/{id}")
    public String markDone(@PathVariable Integer id) {
        Task task = taskService.findById(id).orElse(null);
        if (task != null) {
            task.setStatus("Completed");
            taskService.updateTask(task);
        }
        return "redirect:/tasks";
    }
}
