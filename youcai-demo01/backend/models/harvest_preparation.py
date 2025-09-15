from sqlalchemy import Column, Integer, String, Float, DateTime, JSON, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime
import uuid

from ..database import Base

class HarvestPlan(Base):
    __tablename__ = "harvest_plans"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    crop_type = Column(String)  # 作物类型
    planned_start_date = Column(DateTime)  # 计划开始日期
    planned_end_date = Column(DateTime)  # 计划结束日期
    area = Column(Float)  # 收获面积(亩)
    expected_yield = Column(Float)  # 预计产量(kg/亩)
    status = Column(String)  # 状态: draft, in_progress, completed
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    
    # 与准备任务的关系
    tasks = relationship("PreparationTask", back_populates="harvest_plan")
    # 与产量预测的关系
    yield_forecasts = relationship("YieldForecast", back_populates="harvest_plan")

class PreparationTask(Base):
    __tablename__ = "preparation_tasks"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    harvest_plan_id = Column(String, ForeignKey("harvest_plans.id"))  # 关联的收获计划ID
    task_name = Column(String)  # 任务名称
    task_type = Column(String)  # 任务类型: equipment, labor, field, other
    status = Column(String)  # 状态: pending, in_progress, completed
    deadline = Column(DateTime)  # 截止日期
    responsible_person = Column(String)  # 负责人
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    
    # 与收获计划的关系
    harvest_plan = relationship("HarvestPlan", back_populates="tasks")

class YieldForecast(Base):
    __tablename__ = "yield_forecasts"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    harvest_plan_id = Column(String, ForeignKey("harvest_plans.id"))  # 关联的收获计划ID
    date = Column(DateTime)  # 预测日期
    forecast_value = Column(Float)  # 预测产量(kg/亩)
    confidence = Column(Float)  # 置信度(%)
    factors = Column(JSON)  # 影响因素，存储为JSON数组
    created_at = Column(DateTime, default=datetime.now)
    
    # 与收获计划的关系
    harvest_plan = relationship("HarvestPlan", back_populates="yield_forecasts")