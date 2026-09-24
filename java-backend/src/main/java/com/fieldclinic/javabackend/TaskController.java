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

    // 查询某天的任务
    @GetMapping
    public Map<String, Object> list(@RequestParam String userId,
                                    @RequestParam(required = false) String date) {
        LocalDate d = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
        List<Task> list = taskRepository.findByUserIdAndTaskDateOrderByTaskTimeAsc(userId, d);
        return ok(list);
    }

    // 查询所有历史任务
    @GetMapping("/history")
    public Map<String, Object> history(@RequestParam String userId) {
        List<Task> list = taskRepository.findByUserIdOrderByTaskDateDesc(userId);
        return ok(list);
    }

    // 新增任务
    @PostMapping
    public Map<String, Object> add(@RequestBody Task task) {
        if (task.getTaskDate() == null) {
            task.setTaskDate(LocalDate.now());
        }
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        Task saved = taskRepository.save(task);
        return ok(saved);
    }

    // 标记完成
    @PutMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id) {
        return taskRepository.findById(id).map(task -> {
            task.setStatus(1);
            taskRepository.save(task);
            return ok(task);
        }).orElseGet(() -> {
            Map<String, Object> res = new HashMap<>();
            res.put("code", 404);
            res.put("message", "任务不存在");
            return res;
        });
    }

    // 删除任务
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return ok(null);
    }

    // 统一返回结构
    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", data);
        return res;
    }
}