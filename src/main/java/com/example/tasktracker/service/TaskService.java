package com.example.tasktracker.service;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.exception.ResourceNotFoundException;
import com.example.tasktracker.model.Category;
import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.User;
import com.example.tasktracker.repository.CategoryRepository;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository,
                        UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public Page<Task> search(String username, String title, Boolean completed,
                              Priority priority, Long categoryId, Pageable pageable) {
        User owner = currentUser(username);

        List<Specification<Task>> specs = new ArrayList<>();
        specs.add((root, query, cb) -> cb.equal(root.get("owner").get("id"), owner.getId()));

        if (title != null && !title.isBlank()) {
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        if (completed != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("completed"), completed));
        }
        if (priority != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (categoryId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        Specification<Task> combined = specs.stream().reduce(Specification::and).orElse(null);
        return taskRepository.findAll(combined, pageable);
    }

    public Task findById(String username, Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        assertOwner(task, username);
        return task;
    }

    public List<Task> findOverdue(String username) {
        User owner = currentUser(username);
        return taskRepository.findByOwnerIdAndCompletedFalseAndDueDateBefore(owner.getId(), LocalDate.now());
    }

    public Task create(String username, TaskRequest request) {
        User owner = currentUser(username);

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setCompleted(request.isCompleted());
        task.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        task.setDueDate(request.getDueDate());
        task.setOwner(owner);
        task.setCategory(resolveCategory(request.getCategoryId()));

        return taskRepository.save(task);
    }

    public Task update(String username, Long id, TaskRequest request) {
        Task task = findById(username, id);

        task.setTitle(request.getTitle());
        task.setCompleted(request.isCompleted());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        task.setDueDate(request.getDueDate());
        task.setCategory(resolveCategory(request.getCategoryId()));

        return taskRepository.save(task);
    }

    public void delete(String username, Long id) {
        Task task = findById(username, id);
        taskRepository.delete(task);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private void assertOwner(Task task, String username) {
        if (!task.getOwner().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Task not found: " + task.getId());
        }
    }
}
