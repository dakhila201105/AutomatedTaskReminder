package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    public List<Task> getAllTasks(){
        return taskRepository.findAll();//select* from task
    }
    public Task addTask(Task task){
        return taskRepository.save(task);
    }
    public Task updateTask(Task task) {
        task.setUpdated(true);
        return taskRepository.save(task);
    }

    public void deleteTask(Integer id){
        taskRepository.deleteById(id);
    }
    public Optional<Task> findById(Integer id){
        return taskRepository.findById(id);
    }

    // ---------- FILTER BY STATUS ----------
    public List<Task> findByStatus(String status) {
        return taskRepository.findByStatusIgnoreCase(status);
    }

    // ---------- FILTER BY PRIORITY ----------
    public List<Task> findByPriority(String priority) {
        return taskRepository.findByPriorityIgnoreCase(priority);
    }

    // ---------- SEARCH BY TITLE ----------
    public List<Task> searchByTitle(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ---------- FILTER BY DUE DATE ----------
    public List<Task> findByDueDate(String dueDate) {
        return taskRepository.findByDueDate(dueDate);
    }
}



//@Service
//public class TaskService {

//    private final List<Task> tasks = new ArrayList<>();
//    private static int counter = 100;
//
//    public TaskService() {
//        tasks.add(new Task(
//                1,
//                "Learn Spring Boot",
//                "Practice building applications with Spring Boot",
//                "2025-12-04",
//                "Pending",
//                "High",
//                "2025-12-02 10:00"
//        ));
//
//        tasks.add(new Task(
//                2,
//                "Build Task App",
//                "Complete the Task Reminder application project",
//                "2025-12-05",
//                "In Progress",
//                "Medium",
//                "2025-12-02 12:00"
//        ));
//    }
//
//    public List<Task> getAllTasks() {
//        return tasks;
//    }
//
//    public void addTask(Task task) {
//        tasks.add(task);
//    }
//
//    public static int nextId() {
//        return counter++;
//    }
//
//    // optional, you already had these:
//    public Task getTaskById(int id) {
//        return tasks.stream()
//                .filter(t -> t.getId() == id)
//                .findFirst()
//                .orElse(null);
//    }
//
//    public void updateTask(Task updatedTask) {
//        Task old = getTaskById(updatedTask.getId());
//        if (old == null) {
//            return;
//        }
//
//        old.setTitle(updatedTask.getTitle());
//        old.setDescription(updatedTask.getDescription());
//            old.setDueDate(updatedTask.getDueDate());
//            old.setStatus(updatedTask.getStatus());
//            old.setPriority(updatedTask.getPriority());
//            old.setUpdated(true);
//
//
//    }
//
//    public void deleteTask(Integer id) {
//        Iterator<Task> it=tasks.iterator();
//        while(it.hasNext()){
//            if(it.next().getId().equals(id)){
//                it.remove();
//                return;
//            }
//        }
//
//    }
//}
