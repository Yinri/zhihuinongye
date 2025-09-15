from fastapi import APIRouter, HTTPException, Depends, Query
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
import datetime
from typing import List, Optional
import traceback

from fastapi import UploadFile, File
import numpy as np
from PIL import Image
from ..database import get_db
from ..models.pest_control import PestDetectionData as PestDetectionDataModel, PestTrendData as PestTrendDataModel, PestWarning as PestWarningModel
import random
router = APIRouter()
import os
import io
os.environ["CUDA_VISIBLE_DEVICES"] = "-1"
# 请求和响应模型
from tensorflow.keras.models import load_model

# -------------------------
# 加载模型
# -------------------------
MODEL_PATH = "/root/data/youcai-vue/youcai-demo01/backend/pest.keras"
try:
    model = load_model(MODEL_PATH)
    print(f"模型加载成功: {MODEL_PATH}")
except Exception as e:
    print("模型加载失败:", e)
    model = None

# 模型类别
CLASS_NAMES = ["菜青虫", "露尾甲", "跳甲", "蚜虫"]

# -------------------------
# 上传接口
# -------------------------
@router.post("/upload")
async def upload_pest_image(image: UploadFile = File(...)):
    if model is None:
        raise HTTPException(status_code=500, detail="模型未加载")

    try:
        # -------------------------
        # 读取上传图片
        # -------------------------
        contents = await image.read()
        print(f"上传文件: {image.filename}, 大小: {len(contents)} bytes")
        img = Image.open(io.BytesIO(contents)).convert("RGB")
        img = img.resize((256, 256))
        img_array = np.array(img) / 255.0
        img_array = np.expand_dims(img_array, axis=0)  # (1, 256, 256, 3)
        print("图片数组 shape:", img_array.shape)

        # -------------------------
        # 模型预测
        # -------------------------
        preds = model.predict(img_array)
        print("预测结果:", preds)

        class_idx = int(np.argmax(preds[0]))
        confidence = float(preds[0][class_idx])
        pest_name = CLASS_NAMES[class_idx]
        print(f"预测类别: {pest_name}, 置信度: {confidence:.4f}")

        # 直接返回扁平结构
        return {
            "success": True,
            "name": pest_name,
            "confidence": confidence
        }

    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))




class PestDetectionDataBase(BaseModel):
    severity: str = Field(..., pattern="^(轻微|轻度|中度|严重)$", description="病虫害严重程度")
    description: str = Field(..., description="病虫害描述")
    solution: str = Field(..., description="防治方案")
    area_affected: float = Field(..., description="受影响面积(亩)")

class PestDetectionData(PestDetectionDataBase):
    date: str = Field(..., description="检测日期")

class PestTrendParams(BaseModel):
    period: str = Field(..., pattern="^(month|quarter|year)$", description="时间周期")
    pest_type: Optional[str] = Field(None, description="病虫害类型")

class PestTrendData(BaseModel):
    labels: List[str]
    aphidData: List[float]
    caterpillarData: List[float]
    sclerotiniaData: List[float]

class PestWarning(BaseModel):
    id: str
    pest_type: str
    warning_level: str
    affected_area: float
    issued_date: str
    recommended_actions: List[str]

@router.get("/pest-trend", response_model=PestTrendData)
def get_pest_trend_data(
    period: str = Query(..., pattern="^(month|quarter|year)$", description="时间周期"),
    pest_type: Optional[str] = Query(None, description="病虫害类型"),
    db: Session = Depends(get_db)
):
    """获取病虫害趋势数据"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        
        # 根据不同的时间周期生成不同的标签
        if period == "month":
            labels = ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"]
        elif period == "quarter":
            labels = ["第一季度", "第二季度", "第三季度", "第四季度"]
        else:  # year
            labels = ["2020年", "2021年", "2022年", "2023年"]
        
        # 模拟数据
        aphid_data = [20, 35, 50, 45, 30, 25, 20, 15, 10, 5, 15, 20][:len(labels)]
        caterpillar_data = [10, 15, 25, 30, 40, 45, 50, 55, 45, 35, 25, 15][:len(labels)]
        sclerotinia_data = [5, 10, 15, 20, 25, 30, 25, 20, 15, 10, 5, 5][:len(labels)]
        
        # 如果指定了病虫害类型，过滤数据
        if pest_type == "蚜虫":
            caterpillar_data = [0] * len(labels)
            sclerotinia_data = [0] * len(labels)
        elif pest_type == "菜青虫":
            aphid_data = [0] * len(labels)
            sclerotinia_data = [0] * len(labels)
        elif pest_type == "菌核病":
            aphid_data = [0] * len(labels)
            caterpillar_data = [0] * len(labels)
        
        return {
            "labels": labels,
            "aphidData": aphid_data,
            "caterpillarData": caterpillar_data,
            "sclerotiniaData": sclerotinia_data
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/pest-detection-records", response_model=List[PestDetectionData])
def get_detection_records(
    limit: int = Query(10, ge=1, le=100, description="记录数量限制"),
    db: Session = Depends(get_db)
):
    """获取病虫害检测记录"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # records = db.query(PestDetectionDataModel).order_by(PestDetectionDataModel.date.desc()).limit(limit).all()
        
        # 模拟数据
        records = [
            {
                "date": "2023-01-15",
                "severity": "轻度",
                "description": "发现少量蚜虫，主要集中在叶片背面",
                "solution": "使用生物农药进行防治",
                "area_affected": 5.2
            },
            {
                "date": "2023-01-10",
                "severity": "中度",
                "description": "部分区域发现菜青虫，叶片有明显啃食痕迹",
                "solution": "喷洒低毒农药，每周一次，连续两周",
                "area_affected": 12.5
            },
            {
                "date": "2023-01-05",
                "severity": "轻微",
                "description": "发现少量菌核病症状",
                "solution": "加强田间管理，及时清除病株",
                "area_affected": 3.8
            }
        ]
        
        # 如果limit小于模拟数据数量，只返回limit条
        return records[:limit]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/pest-detection")
def submit_detection_result(
    detection_data: PestDetectionDataBase,
    db: Session = Depends(get_db)
):
    """提交病虫害检测结果"""
    try:
        # 创建病虫害检测记录
        pest_detection = PestDetectionDataModel(
            date=datetime.datetime.now(),
            severity=detection_data.severity,
            description=detection_data.description,
            solution=detection_data.solution,
            area_affected=detection_data.area_affected
        )
        
        # 保存到数据库
        db.add(pest_detection)
        db.commit()
        db.refresh(pest_detection)
        
        # 根据严重程度自动生成预警
        if detection_data.severity in ["中度", "严重"]:
            warning_level = "红色预警" if detection_data.severity == "严重" else "黄色预警"
            pest_type = "未知病虫害"
            
            # 简单的病虫害类型判断
            if "蚜虫" in detection_data.description:
                pest_type = "蚜虫"
            elif "菜青虫" in detection_data.description:
                pest_type = "菜青虫"
            elif "菌核病" in detection_data.description:
                pest_type = "菌核病"
            
            warning = PestWarningModel(
                pest_type=pest_type,
                warning_level=warning_level,
                affected_area=detection_data.area_affected,
                issued_date=datetime.datetime.now(),
                recommended_actions=[detection_data.solution]
            )
            
            db.add(warning)
            db.commit()
        
        return {
            "status": "success",
            "message": "病虫害检测结果提交成功",
            "data": {
                "id": pest_detection.id,
                "date": pest_detection.date.isoformat()
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/pest-warning", response_model=List[PestWarning])
def get_pest_warning(
    db: Session = Depends(get_db)
):
    """获取病虫害预警信息"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # warnings = db.query(PestWarningModel).filter(PestWarningModel.warning_level.in_(['黄色预警', '红色预警'])).order_by(PestWarningModel.issued_date.desc()).all()
        
        # 模拟数据
        warnings = [
            {
                "id": "warning_001",
                "pest_type": "菜青虫",
                "warning_level": "黄色预警",
                "affected_area": 12.5,
                "issued_date": "2023-01-10T14:30:00",
                "recommended_actions": ["喷洒低毒农药，每周一次，连续两周"]
            },
            {
                "id": "warning_002",
                "pest_type": "蚜虫",
                "warning_level": "蓝色预警",
                "affected_area": 5.2,
                "issued_date": "2023-01-15T09:15:00",
                "recommended_actions": ["使用生物农药进行防治"]
            }
        ]
        
        return warnings
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



