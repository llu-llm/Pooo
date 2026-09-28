import io
import numpy as np
from PIL import Image
from ultralytics import YOLO

model = YOLO(r"D:\Users\29269\Documents\GitHub\Pooo\python-agents\best.pt")


def detect_fruit(image_bytes: bytes) -> dict:
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    arr = np.array(img)

    results = model(arr, conf=0.25)

    # ===== 调试：打印原始输出 =====
    for r in results:
        print("### 原始 boxes.cls:", r.boxes.cls.tolist())
        print("### 原始 boxes.conf:", r.boxes.conf.tolist())
        print("### 原始 boxes.xyxy:", r.boxes.xyxy.tolist())
        print("### model.names:", model.names)
    # =============================

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