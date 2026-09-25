package com.example.tasktracker.controller;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Page<Task> getAll(Authentication auth,
                              @RequestParam(required = false) String title,
                              @RequestParam(required = false) Boolean completed,
                              @RequestParam(required = false) Priority priority,
                              @RequestParam(required = false) Long categoryId,
                              @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return taskService.search(auth.getName(), title, completed, priority, categoryId, pageable);
    }

    @GetMapping("/overdue")
    public List<Task> getOverdue(Authentication auth) {
        return taskService.findOverdue(auth.getName());
    }

    @GetMapping("/{id}")
    public Task getOne(Authentication auth, @PathVariable Long id) {
        return taskService.findById(auth.getName(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(Authentication auth, @Valid @RequestBody TaskRequest request) {
        return taskService.create(auth.getName(), request);
    }

    @PutMapping("/{id}")
    public Task update(Authentication auth, @PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        taskService.delete(auth.getName(), id);
    }
}
