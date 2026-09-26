from fastapi import FastAPI, UploadFile, File

app = FastAPI(title="田间诊 AI 服务")


@app.get("/ai/health")
def health():
    return {"code": 0, "message": "success", "data": {"status": "ok"}}


from shennong_disease import detect_disease

@app.post("/ai/disease/detect")
async def detect_disease_api(file: UploadFile = File(...)):
    image_bytes = await file.read()
    result = detect_disease(image_bytes)
    return {"code": 0, "message": "success", "data": result}

from fruit_detector import detect_fruit

@app.post("/ai/fruit/count")
async def count_fruit(file: UploadFile = File(...)):
    image_bytes = await file.read()
    result = detect_fruit(image_bytes)
    return {"code": 0, "message": "success", "data": result}
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