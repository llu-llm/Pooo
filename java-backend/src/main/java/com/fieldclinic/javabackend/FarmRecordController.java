package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class FarmRecordController {

    @Autowired
    private FarmRecordRepository repository;

    @GetMapping
    public Map<String, Object> list(@RequestParam String userId,
                                    @RequestParam(required = false) String date) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        try {
            LocalDate d = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
            return ok(repository.findByUserIdAndRecordDateOrderByRecordTimeAsc(userId, d));
        } catch (java.time.format.DateTimeParseException e) {
            return error(400, "date 格式必须为 yyyy-MM-dd");
        }
    }

    @GetMapping("/history")
    public Map<String, Object> history(@RequestParam String userId) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        return ok(repository.findByUserIdOrderByRecordDateDescRecordTimeDesc(userId));
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody FarmRecord record) {
        if (record == null || isBlank(record.getUserId())) return error(400, "userId 不能为空");
        if (isBlank(record.getTitle())) return error(400, "title 不能为空");
        if (record.getInputType() == null) record.setInputType("manual");
        record.setId(null);
        return ok(repository.save(record));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestParam String userId,
                                      @RequestBody FarmRecord patch) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        return repository.findByIdAndUserId(id, userId).map(r -> {
            if (patch.getTitle() != null) r.setTitle(patch.getTitle());
            if (patch.getContent() != null) r.setContent(patch.getContent());
            if (patch.getCategory() != null) r.setCategory(patch.getCategory());
            if (patch.getRecordTime() != null) r.setRecordTime(patch.getRecordTime());
            return ok(repository.save(r));
        }).orElseGet(() -> error(404, "记录不存在"));
    }

    @PutMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id, @RequestParam String userId) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        return repository.findByIdAndUserId(id, userId).map(r -> {
            r.setStatus(1);
            return ok(repository.save(r));
        }).orElseGet(() -> error(404, "记录不存在"));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, @RequestParam String userId) {
        if (isBlank(userId)) return error(400, "userId 不能为空");
        return repository.findByIdAndUserId(id, userId).map(r -> {
            repository.delete(r);
            return ok(null);
        }).orElseGet(() -> error(404, "记录不存在"));
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0); res.put("message", "success"); res.put("data", data); return res;
    }
    private Map<String, Object> error(int code, String msg) {
        Map<String, Object> res = new HashMap<>(); res.put("code", code); res.put("message", msg); return res;
    }
}
