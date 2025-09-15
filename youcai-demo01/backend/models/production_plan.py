from sqlalchemy import Column, Integer, String, Float, DateTime, JSON
from datetime import datetime
import uuid

from ..database import Base

class ProductionPlan(Base):
    __tablename__ = "production_plans"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    year = Column(Integer)  # 年份
    season = Column(String)  # 季节: spring, autumn
    area = Column(Float)  # 计划面积(ha)
    yield_target = Column(Float)  # 目标产量(kg/ha)
    crop_variety = Column(String)  # 作物品种
    planting_date = Column(DateTime)  # 种植日期
    fertilization_plan = Column(JSON)  # 施肥计划，存储为JSON
    status = Column(String)  # 状态: draft, approved, executing, completed
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)