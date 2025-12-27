package com.taskremainder.app.service;

import com.taskremainder.app.entity.Task;
import com.taskremainder.app.enums.TaskPriority;
import com.taskremainder.app.enums.TaskStatus;
import com.taskremainder.app.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task addTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task updateTask(Task task) {
        Task existing = taskRepository.findById(task.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        existing.setPriority(task.getPriority());
        existing.setDueDate(task.getDueDate());
        existing.setCompletedAt(task.getCompletedAt());
        existing.setUpdated(true);

        return taskRepository.save(existing);
    }

    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found");
        }
        taskRepository.deleteById(id);
    }

    public Page<Task> getTasks(
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            String dueDate,
            Pageable pageable
    ) {
        return taskRepository.searchTasks(keyword, status, priority, dueDate, pageable);
    }

    public Page<Task> getTasksDueToday(Pageable pageable) {
        return taskRepository.findByDueDate(LocalDate.now().toString(), pageable);
    }

    public Page<Task> getUpcomingTasks(int days, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return taskRepository.findByDueDateBetween(today.toString(), endDate.toString(), pageable);
    }

    public Page<Task> getOverdueTasks(Pageable pageable) {
        return taskRepository.findByDueDateBefore(LocalDate.now().toString(), pageable);
    }

    public Optional<Task> findById(Integer id) {
        return taskRepository.findById(id);
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public void delete(Integer id) {
        taskRepository.deleteById(id);
    }
}
