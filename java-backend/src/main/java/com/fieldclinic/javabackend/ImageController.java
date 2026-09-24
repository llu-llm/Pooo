package com.fieldclinic.javabackend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageController {
    // 拍照识病（基础版：返回 Mock 数据）
    @PostMapping("/disease/detect")
    public Map<String, Object> detectDisease(@RequestParam("file") MultipartFile file) {
        // 打印一下文件信息，方便调试
        System.out.println("收到图片：" + file.getOriginalFilename() + "，大小：" + file.getSize());

        // 基础版：返回固定的 Mock 结果
        // 后续升级：把 file 转发给 Python AI 服务 /ai/disease/detect
        Map<String, Object> data = new HashMap<>();
        data.put("crop", "番茄");
        data.put("disease", "叶霉病");
        data.put("confidence", 0.92);
        data.put("symptoms", "叶片出现黄色斑块，叶片背面可能出现霉层。");
        data.put("advice", "加强通风，合理控制田间湿度，并咨询当地植保部门。");
        data.put("disclaimer", "AI识别结果仅供农业生产辅助参考，复杂或疑难情况建议咨询专业农技人员。");

        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", data);
        return res;
    }

    // 拍照数果（基础版：返回 Mock 数据）
    @PostMapping("/fruit/count")
    public Map<String, Object> countFruit(@RequestParam("file") MultipartFile file) {
        System.out.println("收到图片：" + file.getOriginalFilename() + "，大小：" + file.getSize());

        // 基础版：返回固定的 Mock 结果（6 个果实，带框选坐标）
        Map<String, Object> data = new HashMap<>();
        data.put("count", 6);
        List<Map<String, Object>> boxes = new ArrayList<>();
        boxes.add(box(100, 120, 50, 50));
        boxes.add(box(200, 140, 55, 55));
        boxes.add(box(320, 130, 48, 48));
        boxes.add(box(150, 250, 52, 52));
        boxes.add(box(280, 260, 50, 50));
        boxes.add(box(380, 280, 46, 46));
        data.put("boxes", boxes);

        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("message", "success");
        res.put("data", data);
        return res;
    }

    private Map<String, Object> box(int x, int y, int w, int h) {
        Map<String, Object> b = new HashMap<>();
        b.put("x", x);
        b.put("y", y);
        b.put("w", w);
        b.put("h", h);
        b.put("score", 0.9);
        return b;
    }
}