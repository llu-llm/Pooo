package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class VoiceRecordController {

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    @Autowired
    private FarmRecordRepository farmRecordRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/voice")
    @SuppressWarnings("unchecked")
    public Map<String, Object> voiceRecord(
            @RequestParam("file") MultipartFile file,
            @RequestParam String userId,
            @RequestParam(required = false) String category) {

        try {
            // 1. 转发音频给 Python 语音识别服务
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            Map<String, Object> aiResult = restTemplate.postForObject(
                    aiServiceUrl + "/ai/voice/transcribe", request, Map.class);

            if (aiResult == null || aiResult.get("data") == null) {
                return error(500, "语音识别失败，AI 服务无返回");
            }

            // 2. 从返回里取 text
            Map<String, Object> data = (Map<String, Object>) aiResult.get("data");
            String text = data.get("text") == null ? "" : data.get("text").toString().trim();

            if (text.isEmpty()) {
                return error(400, "语音识别结果为空，请重新录音");
            }

            // 3. 保存到 farm_record 表
            FarmRecord record = new FarmRecord();
            record.setUserId(userId);
            record.setContent(text);
            record.setInputType("voice");
            if (category != null && !category.isEmpty()) {
                record.setCategory(category);
            } else {
                record.setCategory("其他");
            }
            record.setRecordDate(LocalDate.now());
            record.setRecordTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

            FarmRecord saved = farmRecordRepository.save(record);

            // 4. 返回统一格式
            Map<String, Object> res = new HashMap<>();
            res.put("code", 0);
            res.put("message", "success");
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("text", text);
            resultData.put("record", saved);
            res.put("data", resultData);
            return res;

        } catch (Exception e) {
            return error(500, "语音记录失败：" + e.getMessage());
        }
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", code);
        res.put("message", message);
        return res;
    }
}