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
            // 1. 预处理：中文冒号转英文冒号，去掉所有空格
            String ratioStr = String.valueOf(body.getOrDefault("ratio", "1:500"))
                    .replace("：", ":")
                    .replace(" ", "")
                    .trim();

            // 2. 解析比例，兼容 "1:500" 和 "500"
            int ratioDenominator;
            if (ratioStr.contains(":")) {
                String[] parts = ratioStr.split(":");
                if (parts.length < 2) {
                    return error(400, "比例格式不正确，应为 1:500");
                }
                ratioDenominator = Integer.parseInt(parts[1]);
            } else {
                ratioDenominator = Integer.parseInt(ratioStr);
            }

            if (ratioDenominator <= 0) {
                return error(400, "比例分母必须大于 0");
            }

            // 3. 解析目标药液量
            double targetVolume = Double.parseDouble(
                    String.valueOf(body.getOrDefault("targetVolume", 10)).replace(" ", ""));
            if (targetVolume <= 0) {
                return error(400, "目标药液量必须大于 0");
            }
            String volumeUnit = String.valueOf(body.getOrDefault("volumeUnit", "L")).trim();

            // 4. 统一换算成 mL
            double targetVolumeMl = "mL".equalsIgnoreCase(volumeUnit) ? targetVolume : targetVolume * 1000;

            // 5. 计算药剂用量
            double pesticideMl = targetVolumeMl / ratioDenominator;
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