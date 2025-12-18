package com.example.demo.controller;

import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

    @Autowired
    private TaskService taskService;

    // ---------- GET ALL TASKS ----------
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // ---------- GET TASK BY ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Integer id) {
        Optional<Task> task = taskService.findById(id);
        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Task not present with id " + id);
        }
    }

    // ---------- CREATE TASK ----------
    @PostMapping("/add")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        task.setCreatedAt(String.valueOf(LocalDateTime.now()));
        task.setUpdated(false);
        Task saved = taskService.addTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ---------- UPDATE TASK ----------
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Integer id,
            @RequestBody Task task) {

        Optional<Task> existing = taskService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        task.setId(id);
        task.setCreatedAt(existing.get().getCreatedAt());
        task.setUpdated(true);

        Task updated = taskService.updateTask(task);
        return ResponseEntity.ok(updated);
    }

    // ---------- DELETE TASK ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        if (taskService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- FILTER BY STATUS ----------
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(taskService.findByStatus(status));
    }

    // ---------- FILTER BY PRIORITY ----------
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(taskService.findByPriority(priority));
    }

    // ---------- SEARCH BY TITLE ----------
    @GetMapping("/search")
    public ResponseEntity<List<Task>> getBySearch(
            @RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(taskService.searchByTitle(keyword));
    }

    // ---------- FILTER BY DUE DATE ----------
    @GetMapping("/due")
    public ResponseEntity<List<Task>> getByDueDate(
            @RequestParam("date") String date) {
        return ResponseEntity.ok(taskService.findByDueDate(date));
    }
}
