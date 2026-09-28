package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private DetectRecordRepository detectRepo;

    @Autowired
    private FarmRecordRepository farmRepo;

    @GetMapping
    public Map<String, Object> list(@RequestParam String userId,
                                    @RequestParam(defaultValue = "all") String type) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 识别记录
        if ("all".equals(type) || "disease".equals(type) || "fruit".equals(type)) {
            List<DetectRecord> detects = detectRepo.findByUserIdOrderByCreatedAtDesc(userId);
            for (DetectRecord d : detects) {
                if (!"all".equals(type) && !type.equals(d.getType())) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("id", d.getId());
                item.put("type", d.getType());
                item.put("title", "disease".equals(d.getType()) ? "识病结果" : "数果结果");
                item.put("icon", "disease".equals(d.getType()) ? "🔍" : "🍎");
                item.put("createdAt", d.getCreatedAt());
                item.put("raw", d.getResultJson());
                list.add(item);
            }
        }

        // 农事记录（含语音、手动、农药计算）
        if ("all".equals(type) || "farm".equals(type) || "pesticide".equals(type)) {
            List<FarmRecord> farms = farmRepo.findByUserIdOrderByRecordDateDescRecordTimeDesc(userId);
            for (FarmRecord f : farms) {
                if ("pesticide".equals(type) && !"calc".equals(f.getInputType())) continue;
                if ("farm".equals(type) && "calc".equals(f.getInputType())) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("id", f.getId());
                item.put("type", "calc".equals(f.getInputType()) ? "pesticide" : "farm");
                item.put("title", f.getTitle());
                item.put("summary", f.getContent());
                item.put("icon", "calc".equals(f.getInputType()) ? "💧" :
                        "voice".equals(f.getInputType()) ? "🎤" : "📝");
                item.put("createdAt", f.getCreatedAt());
                item.put("raw", f);
                list.add(item);
            }
        }

        // 按时间倒序
        list.sort((a, b) -> String.valueOf(b.get("createdAt")).compareTo(String.valueOf(a.get("createdAt"))));

        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", list);
        return res;
    }
}