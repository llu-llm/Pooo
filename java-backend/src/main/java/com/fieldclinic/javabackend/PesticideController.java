package com.fieldclinic.javabackend;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pesticide")
public class PesticideController {

    @PostMapping("/calc")
    public Map<String, Object> calc(@RequestBody Map<String, Object> body) {
        try {
            // 1. 解析倍数（兼容 "1:500"、"500"、中文冒号、空格）
            String ratioStr = String.valueOf(body.getOrDefault("ratio", "500"))
                    .replace("：", ":").replace(" ", "").trim();

            int ratioDenominator;
            if (ratioStr.contains(":")) {
                String[] parts = ratioStr.split(":");
                if (parts.length < 2) return error(400, "比例格式不正确，应为 1:500");
                ratioDenominator = Integer.parseInt(parts[1]);
            } else {
                ratioDenominator = Integer.parseInt(ratioStr);
            }

            // 2. 倍数范围校验：50-5000
            if (ratioDenominator < 50 || ratioDenominator > 5000) {
                return error(400, "稀释倍数必须在 50-5000 之间");
            }

            // 3. 解析目标药液量
            double targetVolume = Double.parseDouble(
                    String.valueOf(body.getOrDefault("targetVolume", 10)).replace(" ", ""));
            if (targetVolume <= 0 || targetVolume > 100) {
                return error(400, "药液量必须大于 0 且不超过 100L");
            }
            String volumeUnit = String.valueOf(body.getOrDefault("volumeUnit", "L")).trim();

            // 4. 药剂单位（g 或 mL）—— 农药类型
            String pesticideUnit = String.valueOf(body.getOrDefault("pesticideUnit", "mL")).trim();
            if (!"g".equalsIgnoreCase(pesticideUnit) && !"mL".equalsIgnoreCase(pesticideUnit)) {
                pesticideUnit = "mL";
            }

            // 5. 统一换算成 mL 或 g
            double targetVolumeMl = "mL".equalsIgnoreCase(volumeUnit) ? targetVolume : targetVolume * 1000;

            // 6. 计算药剂
            double pesticideAmount = targetVolumeMl / ratioDenominator;
            double waterMl = targetVolumeMl - pesticideAmount;
            double waterL = waterMl / 1000.0;

            // 7. 组装返回
            Map<String, Object> data = new HashMap<>();
            data.put("pesticideAmount", Math.round(pesticideAmount * 100.0) / 100.0);
            data.put("pesticideUnit", pesticideUnit);
            data.put("waterAmount", Math.round(waterL * 100.0) / 100.0);
            data.put("waterUnit", "L");
            data.put("ratioUsed", "1:" + ratioDenominator);
            data.put("targetVolume", targetVolume + volumeUnit);
            data.put("disclaimer", "本结果仅依据兑水比例进行数学计算，请以农药产品标签和当地植保部门指导为准。");

            Map<String, Object> res = new HashMap<>();
            res.put("code", 0);
            res.put("message", "success");
            res.put("data", data);
            return res;

        } catch (NumberFormatException e) {
            return error(400, "输入格式不正确：" + e.getMessage());
        } catch (Exception e) {
            return error(500, "服务器错误：" + e.getMessage());
        }
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", code);
        res.put("message", message);
        return res;
    }
}