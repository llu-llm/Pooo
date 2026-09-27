import io
import numpy as np
from PIL import Image
from ultralytics import YOLO

# 第一次运行会自动下载 yolov8n.pt（约 6MB）
model = YOLO("yolov8n.pt")


def detect_fruit(image_bytes: bytes) -> dict:
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    arr = np.array(img)

    results = model(arr)
    boxes = []
    count = 0

    for r in results:
        for i, box in enumerate(r.boxes):
            x1, y1, x2, y2 = box.xyxy[0].tolist()
            conf = float(box.conf[0])
            cls = int(box.cls[0])
            name = model.names[cls]

            boxes.append({
                "id": i + 1,
                "x": int(x1),
                "y": int(y1),
                "w": int(x2 - x1),
                "h": int(y2 - y1),
                "score": round(conf, 2),
                "label": name,
            })
            count += 1

    return {"count": count, "boxes": boxes}