package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detect/history")
public class DetectHistoryController {

    @Autowired
    private DetectRecordRepository repository;

    @GetMapping
    public Map<String, Object> history(@RequestParam String userId,
                                       @RequestParam(required = false) String type) {
        List<DetectRecord> list;
        if (type == null || type.isEmpty()) {
            list = repository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            list = repository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", list);
        return res;
    }
}