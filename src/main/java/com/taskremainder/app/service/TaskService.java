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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.taskremainder.app.entity.User;
import com.taskremainder.app.repository.UserRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("No authentication data found");
        }
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public Task addTask(Task task) {
        task.setUser(getCurrentUser());
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task updateTask(Task task) {
        Task existing = taskRepository.findById(task.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        // Ensure user owns task
        if (!existing.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new RuntimeException("Unauthorized to update this task");
        }

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        existing.setPriority(task.getPriority());
        
        // Reset reminder if due date changes
        if (task.getDueDate() != null && !task.getDueDate().equals(existing.getDueDate())) {
            existing.setReminderSent(false);
        }
        
        existing.setDueDate(task.getDueDate());
        existing.setCompletedAt(task.getCompletedAt());
        existing.setUpdated(true);

        return taskRepository.save(existing);
    }

    public void deleteTask(Integer id) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
        // Ensure user owns task
        if (!existing.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new RuntimeException("Unauthorized to delete this task");
        }
        
        // Soft delete
        existing.setDeleted(true);
        taskRepository.save(existing);
    }
    public void deleteForever(Integer id) {
        taskRepository.deleteById(id);
    }
    
    public void restoreTask(Integer id) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!existing.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new RuntimeException("Unauthorized");
        }
        
        existing.setDeleted(false);
        taskRepository.save(existing);
    }

    public void permanentlyDeleteTask(Integer id) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!existing.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new RuntimeException("Unauthorized");
        }
        
        taskRepository.delete(existing);
    }

    public Page<Task> getDeletedTasks(Pageable pageable) {
        String email = getCurrentUser().getEmail();
        return taskRepository.findByUserEmailAndDeletedTrue(email, pageable);
    }

    public java.util.List<Task> getAllTasksForExport() {
        String email = getCurrentUser().getEmail();
        return taskRepository.findByUserEmailAndDeletedFalse(email);
    }

    public Page<Task> getTasks(
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            String dueDate,
            Pageable pageable
    ) {
        String email = getCurrentUser().getEmail();
        return taskRepository.searchTasks(email, keyword, status, priority, dueDate, pageable);
    }

    public Page<Task> getTasksDueToday(Pageable pageable) {
        String email = getCurrentUser().getEmail();
        return taskRepository.findByUserEmailAndDeletedFalseAndDueDate(email, LocalDate.now().toString(), pageable);
    }

    public Page<Task> getUpcomingTasks(int days, Pageable pageable) {
        String email = getCurrentUser().getEmail();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return taskRepository.findByUserEmailAndDueDateBetween(email, today.toString(), endDate.toString(), pageable);
    }

    public Page<Task> getOverdueTasks(Pageable pageable) {
        String email = getCurrentUser().getEmail();
        return taskRepository.findByUserEmailAndDueDateBefore(email, LocalDate.now().toString(), pageable);
    }

    public Optional<Task> findById(Integer id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            String email = getCurrentUser().getEmail();
            if (!task.get().getUser().getEmail().equals(email)) {
                return Optional.empty(); // Don't return tasks not owned by user
            }
        }
        return task;
    }

    public Task save(Task task) {
        if (task.getId() == null && task.getUser() == null) {
            task.setUser(getCurrentUser());
        }
        return taskRepository.save(task);
    }

    public void delete(Integer id) {
        deleteTask(id);
    }
}
