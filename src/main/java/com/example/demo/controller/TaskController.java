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


    @GetMapping("/tasks")
    public String getTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks";
    }


    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("task", new Task());
        return "add-task";
    }

    @PostMapping("/add")
    public  String saveTask(@ModelAttribute Task task,Model model)
    {
        if (task.getTitle()==null||task.getTitle().trim().isEmpty()){
            model.addAttribute("errorMessage","Title is required!");
            model.addAttribute("task",task);
            return "add-task";
        }
        if (task.getDueDate()==null||task.getDueDate().trim().isEmpty()){
            model.addAttribute("errorMessage","Due Date is required!");
            model.addAttribute("task",task);
            return "add-task";
        }
        //task.setId(TaskService.nextId()); as in memory is not being used not needed
        task.setCreatedAt(LocalDateTime.now().toString());
        taskService.addTask(task);
        return "redirect:/tasks";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id ,Model model){
        Task task =taskService.findById(id).orElse(null);

        if(task==null){
            return "redirect:/tasks";
        }
        model.addAttribute("task",task);
        return "edit-task";
    }
    @PostMapping("/update")
    public String updateTask(@ModelAttribute Task task,Model model){
        if(task.getTitle()==null||task.getTitle().trim().isEmpty()){
            model.addAttribute("errorMessage","Title is required!");
            model.addAttribute("task",task);
            return "edit-task";
        }
        if (task.getDueDate()==null||task.getDueDate().trim().isEmpty()){
            model.addAttribute("errorMessage","Due Date is required!");
            model.addAttribute("task",task);
            return "edit-task";
        }
        Task existing = taskService.findById(task.getId()).orElse(null);
        task.setCreatedAt(existing.getCreatedAt());
        taskService.updateTask(task);
        return "redirect:/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Integer id){
        if(taskService.findById(id).isEmpty()){//do not go with .findById(id)==null
            return "redirect:/tasks";
        }
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }



}
