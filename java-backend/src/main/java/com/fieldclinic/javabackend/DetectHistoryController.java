package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/detect/history")
public class DetectHistoryController {

    private static final Set<String> TYPES = Set.of("disease", "fruit");

    @Autowired
    private DetectRecordRepository repository;

    @GetMapping
    public Map<String, Object> history(@RequestParam String userId,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        if (userId == null || userId.isBlank()) return error(400, "userId 不能为空");
        if (type != null && !type.isBlank() && !TYPES.contains(type)) {
            return error(400, "type 仅支持 disease 或 fruit");
        }

        String normalizedUserId = userId.trim();
        String normalizedType = type == null ? "" : type.trim();
        if (page == null && size == null) {
            List<DetectRecord> list = normalizedType.isEmpty()
                    ? repository.findByUserIdOrderByCreatedAtDesc(normalizedUserId)
                    : repository.findByUserIdAndTypeOrderByCreatedAtDesc(normalizedUserId, normalizedType);
            return ok(list);
        }

        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        if (pageNumber < 0) return error(400, "page 不能小于 0");
        if (pageSize < 1 || pageSize > 100) return error(400, "size 必须在 1-100 之间");

        PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DetectRecord> records = normalizedType.isEmpty()
                ? repository.findByUserId(normalizedUserId, pageable)
                : repository.findByUserIdAndType(normalizedUserId, normalizedType, pageable);

        Map<String, Object> res = ok(records.getContent());
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", records.getNumber());
        pagination.put("size", records.getSize());
        pagination.put("totalElements", records.getTotalElements());
        pagination.put("totalPages", records.getTotalPages());
        pagination.put("last", records.isLast());
        res.put("pagination", pagination);
        return res;
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", data);
        return res;
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", code);
        res.put("message", message);
        return res;
    }
}
