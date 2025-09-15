from sqlalchemy import Column, Integer, String, Float, DateTime, JSON
from datetime import datetime
import uuid

from ..database import Base

class PestDetectionData(Base):
    __tablename__ = "pest_detection_data"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    date = Column(DateTime, default=datetime.now)  # 检测日期
    severity = Column(String)  # 病虫害严重程度: 轻微, 轻度, 中度, 严重
    description = Column(String)  # 病虫害描述
    solution = Column(String)  # 防治方案
    area_affected = Column(Float)  # 受影响面积(亩)
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)

class PestTrendData(Base):
    __tablename__ = "pest_trend_data"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    period = Column(String)  # 时间周期: month, quarter, year
    period_label = Column(String)  # 周期标签，如"1月", "第一季度", "2023年"
    pest_type = Column(String)  # 病虫害类型
    value = Column(Float)  # 病虫害发生程度值
    recorded_at = Column(DateTime, default=datetime.now)

class PestWarning(Base):
    __tablename__ = "pest_warnings"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    pest_type = Column(String)  # 病虫害类型
    warning_level = Column(String)  # 预警等级: 蓝色预警, 黄色预警, 红色预警
    affected_area = Column(Float)  # 受影响面积(亩)
    issued_date = Column(DateTime, default=datetime.now)  # 发布日期
    recommended_actions = Column(JSON)  # 推荐措施，存储为JSON数组
    status = Column(String, default="active")  # 状态: active, resolved
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)