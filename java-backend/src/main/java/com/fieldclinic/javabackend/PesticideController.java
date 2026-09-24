package com.fieldclinic.javabackend;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pesticide")
public class PesticideController {

    @PostMapping("/calc")
    public Map<String, Object> calc(@RequestBody Map<String, Object> body) {
        // 1. 解析比例，例如 "1:500" 或 500
        String ratioStr = String.valueOf(body.getOrDefault("ratio", "1:500"));
        int ratioDenominator;
        if (ratioStr.contains(":")) {
            String[] parts = ratioStr.split(":");
            ratioDenominator = Integer.parseInt(parts[1].trim());
        } else {
            ratioDenominator = Integer.parseInt(ratioStr.trim());
        }

        // 2. 解析目标药液量，默认单位 L
        double targetVolume = Double.parseDouble(String.valueOf(body.getOrDefault("targetVolume", 10)));
        String volumeUnit = String.valueOf(body.getOrDefault("volumeUnit", "L"));

        // 3. 统一换算成 mL
        double targetVolumeMl;
        if ("mL".equalsIgnoreCase(volumeUnit)) {
            targetVolumeMl = targetVolume;
        } else {
            // 默认按 L 处理
            targetVolumeMl = targetVolume * 1000;
        }

        // 4. 计算药剂用量（简化公式：目标量 / 比例分母）
        double pesticideMl = targetVolumeMl / ratioDenominator;

        // 5. 水量 = 目标量 - 药剂量
        double waterMl = targetVolumeMl - pesticideMl;
        double waterL = waterMl / 1000.0;

        // 6. 组装返回
        Map<String, Object> data = new HashMap<>();
        data.put("pesticideAmount", Math.round(pesticideMl * 100.0) / 100.0);
        data.put("pesticideUnit", "mL");
        data.put("waterAmount", Math.round(waterL * 100.0) / 100.0);
        data.put("waterUnit", "L");
        data.put("ratioUsed", "1:" + ratioDenominator);
        data.put("targetVolume", targetVolume + volumeUnit);
        data.put("disclaimer", "农药使用量、稀释倍数和安全间隔期应以产品标签及当地农业技术部门指导为准，不同药剂不可直接套用相同兑水比例。");

        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", data);
        return res;
    }
}