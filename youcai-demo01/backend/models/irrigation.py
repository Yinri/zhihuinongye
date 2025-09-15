from sqlalchemy import Column, Integer, String, Float, DateTime, JSON, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime
import uuid

from ..database import Base

class IrrigationData(Base):
    __tablename__ = "irrigation_data"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    field_id = Column(String, index=True)
    timestamp = Column(DateTime, default=datetime.now)
    soil_moisture = Column(Float)
    temperature = Column(Float)
    humidity = Column(Float)
    rainfall = Column(Float)
    irrigation_amount = Column(Float)
    
    # 可以添加与其他表的关系
    # reports = relationship("Report", back_populates="irrigation_data")

class Report(Base):
    __tablename__ = "reports"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    created_at = Column(DateTime, default=datetime.now)
    content = Column(String)
    # 可以添加更多字段，如报告类型、关联的田块等
    
    # 可以添加与其他表的关系
    # irrigation_data_id = Column(String, ForeignKey("irrigation_data.id"))
    # irrigation_data = relationship("IrrigationData", back_populates="reports")

class TrendData(Base):
    __tablename__ = "trend_data"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    field_id = Column(String, index=True)
    data_type = Column(String)  # 例如: "soil_moisture", "temperature", "humidity"
    timestamp = Column(DateTime, default=datetime.now)
    value = Column(Float)

class AnalysisData(Base):
    __tablename__ = "analysis_data"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    field_id = Column(String, index=True)
    analysis_date = Column(DateTime, default=datetime.now)
    current_stage = Column(String)
    irrigation_needed = Column(Integer)  # 0表示不需要，1表示需要
    recommended_amount = Column(Float)
    next_irrigation_date = Column(DateTime)
    soil_condition = Column(JSON)  # 存储土壤条件的JSON数据

class GrowthStage(Base):
    __tablename__ = "growth_stages"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    crop_type = Column(String)
    stage = Column(String)
    start_date = Column(DateTime)
    end_date = Column(DateTime)
    recommendations = Column(JSON)  # 存储推荐的JSON数组