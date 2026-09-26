from fastapi import FastAPI, UploadFile, File

app = FastAPI(title="田间诊 AI 服务")


@app.get("/ai/health")
def health():
    return {"code": 0, "message": "success", "data": {"status": "ok"}}


@app.post("/ai/disease/detect")
async def detect_disease(file: UploadFile = File(...)):
    return {
        "code": 0,
        "message": "success",
        "data": {
            "crop": "番茄",
            "disease": "叶霉病",
            "confidence": 0.92,
            "symptoms": "叶片出现黄色斑块，叶片背面可能出现霉层。",
            "advice": "加强通风，合理控制田间湿度，并咨询当地植保部门。",
            "disclaimer": "AI识别结果仅供农业生产辅助参考，复杂或疑难情况建议咨询专业农技人员。"
        }
    }


@app.post("/ai/fruit/count")
async def count_fruit(file: UploadFile = File(...)):
    return {
        "code": 0,
        "message": "success",
        "data": {
            "count": 6,
            "boxes": [
                {"x": 100, "y": 120, "w": 50, "h": 50, "score": 0.91},
                {"x": 200, "y": 140, "w": 55, "h": 55, "score": 0.89},
                {"x": 320, "y": 130, "w": 48, "h": 48, "score": 0.93},
                {"x": 150, "y": 250, "w": 52, "h": 52, "score": 0.88},
                {"x": 280, "y": 260, "w": 50, "h": 50, "score": 0.90},
                {"x": 380, "y": 280, "w": 46, "h": 46, "score": 0.87}
            ]
        }
    }
import os
from dotenv import load_dotenv

load_dotenv()

XF_APPID = os.getenv("XF_APPID", "")
XF_APIKEY = os.getenv("XF_APIKEY", "")
XF_APISECRET = os.getenv("XF_APISECRET", "")


@app.post("/ai/voice/transcribe")
async def voice_transcribe(file: UploadFile = File(...)):
    # 基础版：返回 Mock 文本
    # 升级版：调用讯飞方言识别大模型
    return {
        "code": 0,
        "message": "success",
        "data": {
            "text": "今天给番茄浇了水，叶片有点发黄"
        }
    }