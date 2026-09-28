package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class FarmRecordController {

    @Autowired
    private FarmRecordRepository repository;

    // 查询某天的记录（默认今天）
    @GetMapping
    public Map<String, Object> list(@RequestParam String userId,
                                    @RequestParam(required = false) String date) {
        LocalDate d = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
        return ok(repository.findByUserIdAndRecordDateOrderByRecordTimeAsc(userId, d));
    }

    // 查询历史记录
    @GetMapping("/history")
    public Map<String, Object> history(@RequestParam String userId) {
        return ok(repository.findByUserIdOrderByRecordDateDescRecordTimeDesc(userId));
    }

    // 新增记录（手动）
    @PostMapping
    public Map<String, Object> add(@RequestBody FarmRecord record) {
        if (record.getInputType() == null) record.setInputType("manual");
        return ok(repository.save(record));
    }

    // 编辑记录
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody FarmRecord patch) {
        return repository.findById(id).map(r -> {
            if (patch.getTitle() != null) r.setTitle(patch.getTitle());
            if (patch.getContent() != null) r.setContent(patch.getContent());
            if (patch.getCategory() != null) r.setCategory(patch.getCategory());
            if (patch.getRecordTime() != null) r.setRecordTime(patch.getRecordTime());
            return ok(repository.save(r));
        }).orElseGet(() -> error(404, "记录不存在"));
    }

    // 标记完成
    @PutMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id) {
        return repository.findById(id).map(r -> {
            r.setStatus(1);
            return ok(repository.save(r));
        }).orElseGet(() -> error(404, "记录不存在"));
    }

    // 删除
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ok(null);
    }

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