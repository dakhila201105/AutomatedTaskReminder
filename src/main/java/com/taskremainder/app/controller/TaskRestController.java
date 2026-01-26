package com.taskremainder.app.controller;

import com.taskremainder.app.entity.Task;
import com.taskremainder.app.enums.TaskPriority;
import com.taskremainder.app.enums.TaskStatus;
import com.taskremainder.app.service.TaskService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

    private final TaskService taskService;

    public TaskRestController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String dueDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(direction), sortBy)
        );

        return ResponseEntity.ok(
                taskService.getTasks(keyword, status, priority, dueDate, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Integer id) {
        return taskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        return ResponseEntity.ok(taskService.save(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Integer id,
            @RequestBody Task updatedTask
    ) {
        return taskService.findById(id)
                .map(existing -> {
                    updatedTask.setId(id);
                    updatedTask.setCreatedAt(existing.getCreatedAt());
                    return ResponseEntity.ok(taskService.save(updatedTask));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ MARK AS DONE
    @PatchMapping("/{id}/done")
    public ResponseEntity<Task> markDone(@PathVariable Integer id) {
        return taskService.findById(id)
                .map(task -> {
                    task.setStatus(TaskStatus.DONE);
                    task.setCompletedAt(java.time.LocalDate.now());
                    return ResponseEntity.ok(taskService.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/today")
    public ResponseEntity<Page<Task>> getTodayTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(taskService.getTasksDueToday(pageable));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<Task>> getUpcomingTasks(
            @RequestParam(defaultValue = "3") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(taskService.getUpcomingTasks(days, pageable));
    }

    @GetMapping("/overdue")
    public ResponseEntity<Page<Task>> getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(taskService.getOverdueTasks(pageable));
    }

}
