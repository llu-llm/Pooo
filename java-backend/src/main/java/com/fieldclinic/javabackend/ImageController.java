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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageController {

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    @Autowired
    private DetectRecordRepository detectRecordRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/disease/detect")
    public Map<String, Object> detectDisease(@RequestParam("file") MultipartFile file,
                                             @RequestParam(required = false) String userId) {
        Map<String, Object> result = forwardToAi(aiServiceUrl + "/ai/disease/detect", file);
        saveRecord(userId, "disease", result);
        return result;
    }

    @PostMapping("/fruit/count")
    public Map<String, Object> countFruit(@RequestParam("file") MultipartFile file,
                                          @RequestParam(required = false) String userId) {
        Map<String, Object> result = forwardToAi(aiServiceUrl + "/ai/fruit/count", file);
        saveRecord(userId, "fruit", result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> forwardToAi(String url, MultipartFile file) {
        try {
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
            return restTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 500);
            err.put("message", "调用AI服务失败：" + e.getMessage());
            return err;
        }
    }

    private void saveRecord(String userId, String type, Map<String, Object> result) {
        if (userId == null || userId.isEmpty()) return;
        try {
            DetectRecord record = new DetectRecord();
            record.setUserId(userId);
            record.setType(type);
            record.setResultJson(result.toString().replace("?", "?"));
            detectRecordRepository.save(record);
        } catch (Exception e) {
            System.out.println("保存识别记录失败：" + e.getMessage());
        }
    }
}