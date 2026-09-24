package com.fieldclinic.javabackend;

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

    // 从环境变量 AI_SERVICE_URL 读取，本地开发默认 http://localhost:8001
    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 拍照识病
    @PostMapping("/disease/detect")
    public Map<String, Object> detectDisease(@RequestParam("file") MultipartFile file) {
        return forwardToAi(aiServiceUrl + "/ai/disease/detect", file);
    }

    // 拍照数果
    @PostMapping("/fruit/count")
    public Map<String, Object> countFruit(@RequestParam("file") MultipartFile file) {
        return forwardToAi(aiServiceUrl + "/ai/fruit/count", file);
    }

    // 把图片转发给 Python AI 服务，返回结果
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
}