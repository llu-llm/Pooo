package com.fieldclinic.javabackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${wechat.appid:}")
    private String appId;

    @Value("${wechat.secret:}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    @SuppressWarnings("unchecked")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        Map<String, Object> res = new HashMap<>();

        if (code == null || code.isEmpty()) {
            res.put("code", 400);
            res.put("message", "code 不能为空");
            return res;
        }

        // 没配 AppID 时用 Mock 模式
        if (appId == null || appId.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("openid", "mock_openid_001");
            data.put("mock", true);
            res.put("code", 0);
            res.put("message", "success (Mock 模式，未配置微信 AppID)");
            res.put("data", data);
            return res;
        }

        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session"
                    + "?appid=" + appId
                    + "&secret=" + appSecret
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";

            // 微信可能返回 JSON 或 text/plain，统一用 String 接
            String raw = restTemplate.getForObject(url, String.class);

            // 简单字符串提取 openid，不依赖 JSON 库
            String openid = null;
            int idx = raw.indexOf("\"openid\"");
            if (idx > 0) {
                int start = raw.indexOf("\"", idx + 8) + 1;
                int end = raw.indexOf("\"", start);
                openid = raw.substring(start, end);
            }

            if (openid == null) {
                res.put("code", 500);
                res.put("message", "微信登录失败：" + raw);
                return res;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("openid", openid);
            res.put("code", 0);
            res.put("message", "success");
            res.put("data", data);
            return res;

        } catch (Exception e) {
            res.put("code", 500);
            res.put("message", "调用微信接口失败：" + e.getMessage());
            return res;
        }
    }
}