import base64
import json
import os
import re
from dotenv import load_dotenv
from openai import OpenAI
from PIL import Image

load_dotenv()

SHENNONG_API_KEY = os.getenv("SHENNONG_API_KEY", "")

client = OpenAI(
    api_key=SHENNONG_API_KEY,
    base_url="https://api.agent-tech.cc/api/v1",
)


def detect_disease(image_bytes: bytes) -> dict:
    """
    把图片转成 base64 data URL，发给神农大模型做病害识别。
    返回统一格式：{"top3": [...]}
    """
    img_base64 = base64.b64encode(image_bytes).decode("utf-8")
    image_url = f"data:image/jpeg;base64,{img_base64}"

    prompt = (
        "请分析这张作物图片，判断它可能是什么病害。"
        "请严格按以下 JSON 格式返回，不要加其他任何文字、不要加 markdown 代码块标记："
        '{"top3":[{"disease":"病害名称","confidence":0.92,'
        '"symptoms":"症状描述","advice":"防治建议"}]}'
    )

    try:
        resp = client.chat.completions.create(
            model="sn",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {"type": "image_url", "image_url": {"url": image_url}},
                    ],
                }
            ],
        )
        content = resp.choices[0].message.content
        print("### 神农原始返回:", content)

        # 去掉 markdown 代码块标记
        cleaned = content.strip()
        cleaned = re.sub(r"^```(?:json)?\s*", "", cleaned)
        cleaned = re.sub(r"\s*```$", "", cleaned)

        # 尝试解析 JSON
        parsed = json.loads(cleaned)
        return parsed

    except json.JSONDecodeError as e:
        print("### JSON 解析失败:", e)
        return {
            "top3": [
                {
                    "disease": "未知",
                    "confidence": 0.0,
                    "symptoms": content,
                    "advice": "",
                }
            ]
        }
    except Exception as e:
        print("### 神农调用失败:", e)
        return {
            "top3": [
                {
                    "disease": "识别失败",
                    "confidence": 0.0,
                    "symptoms": str(e),
                    "advice": "",
                }
            ]
        }