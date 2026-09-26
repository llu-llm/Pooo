# -*- coding: utf-8 -*-
import base64
import hashlib
import hmac
import json
import ssl
import threading
import time
from datetime import datetime
from time import mktime
from urllib.parse import urlencode
from wsgiref.handlers import format_date_time

import websocket

STATUS_FIRST_FRAME = 0
STATUS_CONTINUE_FRAME = 1
STATUS_LAST_FRAME = 2

IAT_HOST = "iat.cn-huabei-1.xf-yun.com"
IAT_URL = "wss://iat.cn-huabei-1.xf-yun.com/v1"


def _create_url(appid, apikey, apisecret):
    now = datetime.now()
    date = format_date_time(mktime(now.timetuple()))

    signature_origin = f"host: {IAT_HOST}\n"
    signature_origin += f"date: {date}\n"
    signature_origin += "GET /v1 HTTP/1.1"

    signature_sha = hmac.new(
        apisecret.encode("utf-8"),
        signature_origin.encode("utf-8"),
        digestmod=hashlib.sha256,
    ).digest()
    signature_sha = base64.b64encode(signature_sha).decode("utf-8")

    authorization_origin = (
        f'api_key="{apikey}", algorithm="hmac-sha256", '
        f'headers="host date request-line", signature="{signature_sha}"'
    )
    authorization = base64.b64encode(
        authorization_origin.encode("utf-8")
    ).decode("utf-8")

    params = {"authorization": authorization, "date": date, "host": IAT_HOST}
    return IAT_URL + "?" + urlencode(params)


def transcribe(audio_bytes: bytes, appid: str, apikey: str, apisecret: str) -> str:
    """把 PCM 音频 bytes 发给讯飞方言大模型，返回识别文字。"""
    result_text = {"text": ""}
    url = _create_url(appid, apikey, apisecret)

    iat_params = {
        "domain": "slm",
        "language": "zh_cn",
        "accent": "mulacc",
        "result": {"encoding": "utf8", "compress": "raw", "format": "json"},
    }

    def on_message(ws, message):
        msg = json.loads(message)
        code = msg["header"]["code"]
        status = msg["header"]["status"]
        if code != 0:
            print(f"讯飞返回错误码：{code}")
            ws.close()
            return
        payload = msg.get("payload")
        if payload:
            text = payload["result"]["text"]
            text_obj = json.loads(str(base64.b64decode(text), "utf8"))
            for ws_item in text_obj["ws"]:
                for cw in ws_item["cw"]:
                    result_text["text"] += cw["w"]
        if status == 2:
            ws.close()

    def on_error(ws, error):
        print("### error:", error)

    def on_close(ws, close_status_code, close_msg):
        print("### closed ###")

    def on_open(ws):
        def run():
            frame_size = 1280
            interval = 0.04
            status = STATUS_FIRST_FRAME

            for i in range(0, len(audio_bytes), frame_size):
                buf = audio_bytes[i:i + frame_size]
                audio = str(base64.b64encode(buf), "utf-8")

                if status == STATUS_FIRST_FRAME:
                    d = {
                        "header": {"status": 0, "app_id": appid},
                        "parameter": {"iat": iat_params},
                        "payload": {
                            "audio": {
                                "audio": audio,
                                "sample_rate": 16000,
                                "encoding": "raw",
                            }
                        },
                    }
                    ws.send(json.dumps(d))
                    status = STATUS_CONTINUE_FRAME
                else:
                    d = {
                        "header": {"status": 1, "app_id": appid},
                        "payload": {
                            "audio": {
                                "audio": audio,
                                "sample_rate": 16000,
                                "encoding": "raw",
                            }
                        },
                    }
                    ws.send(json.dumps(d))
                time.sleep(interval)

            d = {
                "header": {"status": 2, "app_id": appid},
                "payload": {"audio": {"audio": "", "sample_rate": 16000, "encoding": "raw"}},
            }
            ws.send(json.dumps(d))

        threading.Thread(target=run, daemon=True).start()

    websocket.enableTrace(False)
    ws = websocket.WebSocketApp(
        url, on_message=on_message, on_error=on_error, on_close=on_close
    )
    ws.on_open = on_open
    ws.run_forever(sslopt={"cert_reqs": ssl.CERT_NONE})

    return result_text["text"]