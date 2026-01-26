package com.taskremainder.app.controller;

import com.taskremainder.app.entity.Task;
import com.taskremainder.app.service.TaskService;
import com.taskremainder.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BinController {

    private final TaskService taskService;
    private final UserService userService;

    public BinController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/bin")
    public String viewBin(Model model) {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> deletedTasks = taskService.getDeletedTasks(pageable);

        model.addAttribute("tasks", deletedTasks);
        model.addAttribute("activePage", "bin");
        model.addAttribute("user", userService.getCurrentUser());

        return "bin";
    }

    @GetMapping("/bin/restore/{id}")
    public String restoreTask(@PathVariable Integer id) {
        taskService.restoreTask(id);
        return "redirect:/tasks";
    }

    @GetMapping("/bin/delete-forever/{id}")
    public String deleteForever(@PathVariable Integer id) {
        taskService.deleteForever(id);
        return "redirect:/bin";
    }
}
