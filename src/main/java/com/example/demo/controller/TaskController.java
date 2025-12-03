package com.example.demo.controller;

import com.example.demo.service.TaskService;
import com.example.demo.entity.Task;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public String getTasks(Model model) {
        // renamed to getAllTasks() in service
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks"; // View name
    }

    // GET TASK BY ID (delegating to service)
    public Task getTaskById(int id) {
        return taskService.getTaskById(id);
    }

    // UPDATE TASK (delegating to service)
    public void updateTask(Task updatedTask) {
        taskService.updateTask(updatedTask);
    }

    // DELETE TASK (delegating to service)
    public void deleteTask(int id) {
        taskService.deleteTask(id);
    }
}
