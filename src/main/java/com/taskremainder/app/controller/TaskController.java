package com.taskremainder.app.controller;

import com.taskremainder.app.entity.Task;
import com.taskremainder.app.enums.TaskPriority;
import com.taskremainder.app.enums.TaskStatus;
import com.taskremainder.app.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.taskremainder.app.entity.User;
import com.taskremainder.app.service.EmailService;
import com.taskremainder.app.service.UserService;
import com.taskremainder.app.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final EmailService emailService;

    // ✅ Constructor injection (BEST PRACTICE)
    public TaskController(TaskService taskService, UserService userService, EmailService emailService) {
        this.taskService = taskService;
        this.userService = userService;
        this.emailService = emailService;
    }
    @GetMapping("/mail-report")
    public String sendMailReport(RedirectAttributes redirectAttributes) {

        try {
            User user = userService.getCurrentUser();

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not logged in");
                return "redirect:/tasks";
            }

            List<Task> tasks = taskService.getAllTasksForExport();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Title,Status,Priority,Due Date\n");

            for (Task task : tasks) {
                csv.append(task.getId()).append(",")
                        .append(safe(task.getTitle())).append(",")
                        .append(safe(task.getStatus())).append(",")
                        .append(safe(task.getPriority())).append(",")
                        .append(task.getDueDate() != null ? task.getDueDate() : "")
                        .append("\n");
            }

            emailService.sendCsvReport(
                    user.getEmail(),
                    "Task Report",
                    "Attached is your task report.",
                    csv.toString().getBytes(StandardCharsets.UTF_8),
                    "task-report.csv"
            );

            redirectAttributes.addFlashAttribute("success", "Report email sent!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to send report");
        }

        return "redirect:/tasks";
    }

    @ModelAttribute("user")
    public User addUserToModel() {
        try {
            return userService.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
    @GetMapping("/tasks")
    public String tasks(Model model) {
        model.addAttribute("activePage", "tasks");
        return "tasks";
    }



    // ================= LIST TASKS WITH PAGINATION =================
    @GetMapping
    public String listTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String dueDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        model.addAttribute("pageSizes", List.of(5, 10, 25, 50));


        try {
            // Handle empty strings for optional parameters
            if (keyword != null && keyword.trim().isEmpty()) keyword = null;
            if (dueDate != null && dueDate.trim().isEmpty()) dueDate = null;

            Pageable pageable = PageRequest.of(page, size);

            Page<Task> tasks = taskService.getTasks(
                    keyword,
                    status,
                    priority,
                    dueDate,
                    pageable
            );

            model.addAttribute("tasks", tasks);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", tasks.getTotalPages());

            // preserve filters in pagination
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("priority", priority);
            model.addAttribute("dueDate", dueDate);

            // Add Enums for filters
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("activePage", "bin");


            return "tasks";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }


    // ================= EXPORT CSV =================
    @GetMapping("/export")
    public void exportToCSV(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        java.text.DateFormat dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new java.util.Date());
        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=tasks_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);
        
        java.util.List<Task> listTasks = taskService.getAllTasksForExport();
        
        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("Task ID,Title,Description,Status,Priority,Due Date,Created At");
            
            for (Task task : listTasks) {
                writer.println(
                    task.getId() + "," +
                    escapeSpecialCharacters(task.getTitle()) + "," +
                    escapeSpecialCharacters(task.getDescription()) + "," +
                    task.getStatus() + "," +
                    task.getPriority() + "," +
                    task.getDueDate() + "," +
                    task.getId() // placeholder for created at if needed or just use ID
                );
            }
        }
    }
    
    private String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    // ================= BIN (DELETED TASKS) =================
//    //@GetMapping("/bin")
//    public String viewBin(@RequestParam(defaultValue = "0") int page,
//                          Model model) {
//
//        Pageable pageable = PageRequest.of(page, 10);
//        Page<Task> deletedTasks = taskService.getDeletedTasks(pageable);
//
//        model.addAttribute("tasks", deletedTasks);
//        model.addAttribute("activePage", "bin");
//
//        // IMPORTANT: sidebar needs user
//        model.addAttribute("user", userService.getCurrentUser());
//
//        return "/tasks/bin";
//    }
//
//
//
//    @GetMapping("/restore/{id}")
//    public String restoreTask(@PathVariable Integer id,
//                              RedirectAttributes redirectAttributes) {
//        taskService.restoreTask(id);
//        try {
//            taskService.restoreTask(id);
//            redirectAttributes.addFlashAttribute("success", "Task restored successfully!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Error restoring task.");
//        }
//        return "redirect:/tasks/bin";
//    }
//
//
//    @GetMapping("/delete-forever/{id}")
//    public String deleteForever(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
//        taskService.deleteForever(id);
//        try {
//            taskService.permanentlyDeleteTask(id);
//            redirectAttributes.addFlashAttribute("success", "Task permanently deleted.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Error deleting task.");
//        }
//        return "redirect:/tasks/bin";
//    }

    // ================= ADD TASK =================
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        return "add-task";
    }

    @PostMapping("/add")
    public String saveTask(jakarta.servlet.http.HttpServletRequest request) {
        System.out.println("📝 saveTask called (Manual Binding)");
        try {
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String priorityStr = request.getParameter("priority");
            String dueDate = request.getParameter("dueDate");

            System.out.println("   Params: title=" + title + ", priority=" + priorityStr);

            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            if (priorityStr != null) {
                task.setPriority(TaskPriority.valueOf(priorityStr));
            } else {
                task.setPriority(TaskPriority.LOW);
            }
            task.setStatus(TaskStatus.PENDING);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdated(false);

            taskService.addTask(task);
            System.out.println("✅ saveTask success");
            return "redirect:/tasks";
        } catch (Exception e) {
            System.err.println("❌ saveTask failed: " + e.getMessage());
            e.printStackTrace();
            // In case of error, redirect to tasks for now to avoid rendering issues
            return "redirect:/tasks?error=save_failed";
        }
    }


    // ================= EDIT TASK =================
    @GetMapping("/edit/{id}")
    public String editTask(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return taskService.findById(id)
                .map(task -> {
                    model.addAttribute("task", task);
                    model.addAttribute("statuses", TaskStatus.values());
                    model.addAttribute("priorities", TaskPriority.values());
                    return "edit-task";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Task not found");
                    return "redirect:/tasks";
                });
    }

    @PostMapping("/edit")
    public String updateTask(@ModelAttribute Task task) {
        Task existing = taskService.findById(task.getId()).orElse(null);
        if (existing == null) return "redirect:/tasks";

        task.setCreatedAt(existing.getCreatedAt());
        task.setUpdated(true);
        taskService.updateTask(task);
        return "redirect:/tasks";
    }

    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    // MARK DONE
    @GetMapping("/markdown/{id}")
    public String markDone(@PathVariable Integer id) {

        Task task = taskService.findById(id).orElse(null);
        if (task != null) {
            task.setStatus(TaskStatus.DONE);
            
            task.setCompletedAt(LocalDate.now());
            taskService.updateTask(task);
        }
        return "redirect:/tasks";
    }


    // ================= VIEW =================
    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return taskService.findById(id)
                .map(task -> {
                    model.addAttribute("task", task);
                    return "view-task";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "No task found");
                    return "redirect:/tasks";
                });
    }


}
