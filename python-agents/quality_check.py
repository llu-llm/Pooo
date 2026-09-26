import cv2
import numpy as np


def check_quality(image_bytes: bytes) -> dict:
    arr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
    if img is None:
        return {"quality": "error", "issues": ["无法解析图片"], "score": 0}

    issues = []

    # 1. 模糊检测：Laplacian 方差
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blur_score = cv2.Laplacian(gray, cv2.CV_64F).var()
    if blur_score < 100:
        issues.append("模糊")

    # 2. 过暗检测：平均亮度
    brightness = gray.mean()
    if brightness < 50:
        issues.append("过暗")
    elif brightness > 220:
        issues.append("过亮")

    # 3. 分辨率检测
    h, w = img.shape[:2]
    if min(h, w) < 300:
        issues.append("分辨率过低")

    quality = "ok" if not issues else "poor"
    score = round(min(blur_score / 500, 1.0), 2)

    return {
        "quality": quality,
        "issues": issues,
        "score": score,
    }