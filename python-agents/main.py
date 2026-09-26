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
            "top3": [
                {
                    "disease": "番茄叶霉病",
                    "confidence": 0.92,
                    "symptoms": "叶片出现黄色斑块，背面可能有霉层。",
                    "advice": "加强通风，控制湿度。"
                },
                {
                    "disease": "番茄早疫病",
                    "confidence": 0.65,
                    "symptoms": "叶片出现褐色同心轮纹病斑。",
                    "advice": "及时摘除病叶，喷施杀菌剂。"
                },
                {
                    "disease": "番茄灰霉病",
                    "confidence": 0.41,
                    "symptoms": "叶尖和叶缘出现水浸状腐烂。",
                    "advice": "降低湿度，及时清除病残体。"
                }
            ]
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
    {"id": 1, "x": 100, "y": 120, "w": 50, "h": 50, "score": 0.91},
    {"id": 2, "x": 200, "y": 140, "w": 55, "h": 55, "score": 0.89},
    {"id": 3, "x": 320, "y": 130, "w": 48, "h": 48, "score": 0.93},
    {"id": 4, "x": 150, "y": 250, "w": 52, "h": 52, "score": 0.88},
    {"id": 5, "x": 280, "y": 260, "w": 50, "h": 50, "score": 0.90},
    {"id": 6, "x": 380, "y": 280, "w": 46, "h": 46, "score": 0.87}
]
        }
    }
#语音转文字
from xfyun_stt import transcribe
import os
from dotenv import load_dotenv

load_dotenv()

XF_APPID = os.getenv("XF_APPID", "")
XF_APIKEY = os.getenv("XF_APIKEY", "")
XF_APISECRET = os.getenv("XF_APISECRET", "")


@app.post("/ai/voice/transcribe")
async def voice_transcribe(file: UploadFile = File(...)):
    audio_bytes = await file.read()
    try:
        text = transcribe(audio_bytes, XF_APPID, XF_APIKEY, XF_APISECRET)
        return {"code": 0, "message": "success", "data": {"text": text}}
    except Exception as e:
        return {"code": 500, "message": str(e), "data": {"text": ""}}
#质量检测
from fastapi import UploadFile, File
from quality_check import check_quality


@app.post("/ai/quality/check")
async def quality_check(file: UploadFile = File(...)):
    image_bytes = await file.read()
    result = check_quality(image_bytes)
    return {"code": 0, "message": "success", "data": result}