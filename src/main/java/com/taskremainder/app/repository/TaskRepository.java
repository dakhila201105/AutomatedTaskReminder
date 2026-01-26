package com.taskremainder.app.repository;

import com.taskremainder.app.entity.Task;
import com.taskremainder.app.enums.TaskPriority;
import com.taskremainder.app.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    // 🔍 SEARCH
    Page<Task> findByUserEmailAndTitleContainingIgnoreCase(String email, String title, Pageable pageable);

    // 🔍 COMPLEX SEARCH
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t LEFT JOIN FETCH t.user WHERE " +
            "t.user.email = :email AND t.deleted = false AND " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:dueDate IS NULL OR t.dueDate = :dueDate)")
    Page<Task> searchTasks(
            @org.springframework.data.repository.query.Param("email") String email,
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("status") TaskStatus status,
            @org.springframework.data.repository.query.Param("priority") TaskPriority priority,
            @org.springframework.data.repository.query.Param("dueDate") String dueDate,
            Pageable pageable
    );

    // 🗑️ BIN
    Page<Task> findByUserEmailAndDeletedTrue(String email, Pageable pageable);

    // 📅 DATE QUERIES
    Page<Task> findByUserEmailAndDeletedFalseAndDueDate(String email, String date, Pageable pageable);
    
    // Find all by user (Non-deleted)
    Page<Task> findByUserEmailAndDeletedFalse(String email, Pageable pageable);
    
    // Find all by user (Including deleted) for CSV? Usually just active.
    java.util.List<Task> findByUserEmailAndDeletedFalse(String email);

    Page<Task> findByUserEmailAndDueDateBetween(String email, String startDate, String endDate, Pageable pageable);
    
    Page<Task> findByUserEmailAndDueDateBefore(String email, String date, Pageable pageable);

    // 🔔 REMINDERS
    java.util.List<Task> findByDueDateAndReminderSentFalseAndDeletedFalse(String dueDate);

    long countByUserAndStatusAndDeletedFalse(com.taskremainder.app.entity.User user, TaskStatus status);

    java.util.List<Task> findByUserAndStatusAndDeletedFalse(com.taskremainder.app.entity.User user, TaskStatus status);
}
