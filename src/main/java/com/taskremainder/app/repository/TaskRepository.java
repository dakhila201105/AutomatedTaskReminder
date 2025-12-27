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
    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 🔍 COMPLEX SEARCH
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:dueDate IS NULL OR t.dueDate = :dueDate)")
    Page<Task> searchTasks(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("status") TaskStatus status,
            @org.springframework.data.repository.query.Param("priority") TaskPriority priority,
            @org.springframework.data.repository.query.Param("dueDate") String dueDate,
            Pageable pageable
    );

    // 🎯 FILTERS
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    // 📅 DATE QUERIES
    Page<Task> findByDueDate(String date, Pageable pageable);

    Page<Task> findByDueDateBefore(String date, Pageable pageable);

    Page<Task> findByDueDateBetween(String start, String end, Pageable pageable);
}
