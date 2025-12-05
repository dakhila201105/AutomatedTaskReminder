package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class Task {
    @Id //for representing the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="task_id")
    private Integer id;
    private String title;
    private String description;
    private String dueDate;
    private String status;
    private String priority;
    private String createdAt;
    private boolean updated;

    public Task() {}

    public Task(Integer id, String title, String description, String dueDate,
                String status, String priority, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public boolean isUpdated(){
        return updated;
    }
    public void setUpdated(boolean updated){
        this.updated=updated;
    }

}
