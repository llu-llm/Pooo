package com.fieldclinic.javabackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    @Autowired
    private DetectRecordRepository detectRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = createRestTemplate();

    @PostMapping("/disease/detect")
    public Map<String, Object> detectDisease(@RequestParam("file") MultipartFile file,
                                             @RequestParam(required = false) String userId) {
        Map<String, Object> validationError = validateFile(file);
        if (validationError != null) return validationError;
        Map<String, Object> result = forwardToAi(aiServiceUrl + "/ai/disease/detect", file);
        saveRecord(userId, "disease", result);
        return result;
    }

    @PostMapping("/fruit/count")
    public Map<String, Object> countFruit(@RequestParam("file") MultipartFile file,
                                          @RequestParam(required = false) String userId) {
        Map<String, Object> validationError = validateFile(file);
        if (validationError != null) return validationError;
        Map<String, Object> result = forwardToAi(aiServiceUrl + "/ai/fruit/count", file);
        saveRecord(userId, "fruit", result);
        return result;
    }

    private Map<String, Object> validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return error(400, "文件不能为空");
        if (file.getSize() > MAX_FILE_SIZE) return error(413, "文件大小不能超过 10MB");
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png"))) {
            return error(400, "仅支持 JPG 或 PNG 图片");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> forwardToAi(String url, MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() { return file.getOriginalFilename(); }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            Map<String, Object> result = restTemplate.postForObject(url, request, Map.class);
            return result == null ? error(502, "AI 服务返回为空") : result;
        } catch (Exception e) {
            return error(502, "AI 服务暂时不可用，请稍后重试");
        }
    }

    private void saveRecord(String userId, String type, Map<String, Object> result) {
        if (userId == null || userId.isBlank() || result == null || !isSuccess(result)) return;
        try {
            DetectRecord record = new DetectRecord();
            record.setUserId(userId.trim());
            record.setType(type);
            record.setResultJson(objectMapper.writeValueAsString(result));
            detectRecordRepository.save(record);
        } catch (JsonProcessingException ignored) {
            // 识别结果无法序列化时不阻断主流程
        }
    }

    private boolean isSuccess(Map<String, Object> result) {
        Object code = result.get("code");
        return code instanceof Number && ((Number) code).intValue() == 0;
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        return result;
    }
}
