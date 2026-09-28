package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public Map<String, Object> list(@RequestParam String userId,
                                    @RequestParam(required = false) String date) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        try {
            LocalDate d = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
            return ok(taskRepository.findByUserIdAndTaskDateOrderByTaskTimeAsc(userId, d));
        } catch (java.time.format.DateTimeParseException e) {
            return error(400, "date 格式必须为 yyyy-MM-dd");
        }
    }

    @GetMapping("/history")
    public Map<String, Object> history(@RequestParam String userId) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        return ok(taskRepository.findByUserIdOrderByTaskDateDesc(userId));
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Task task) {
        if (task == null || isBlank(task.getUserId())) return error(400, "userId 不能为空");
        if (isBlank(task.getTitle())) return error(400, "title 不能为空");
        if (task.getTaskDate() == null) task.setTaskDate(LocalDate.now());
        if (task.getStatus() == null) task.setStatus(0);
        task.setId(null);
        return ok(taskRepository.save(task));
    }

    @PutMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id, @RequestParam String userId) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        return taskRepository.findByIdAndUserId(id, userId).map(task -> {
            task.setStatus(1);
            return ok(taskRepository.save(task));
        }).orElseGet(() -> error(404, "任务不存在"));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, @RequestParam String userId) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        return taskRepository.findByIdAndUserId(id, userId).map(task -> {
            taskRepository.delete(task);
            return ok(null);
        }).orElseGet(() -> error(404, "任务不存在"));
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", data);
        return res;
    }

    private Map<String, Object> error(int code, String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", code);
        res.put("message", msg);
        return res;
    }
}
