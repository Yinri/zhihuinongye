from sqlalchemy import Column, Integer, String, Float, DateTime, JSON
from datetime import datetime
import uuid

from ..database import Base

class SoilTestData(Base):
    __tablename__ = "soil_test_data"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    field_id = Column(String, index=True)
    nitrogen = Column(Float)  # 土壤氮含量(kg/ha)
    phosphorus = Column(Float)  # 土壤磷含量(kg/ha)
    potassium = Column(Float)  # 土壤钾含量(kg/ha)
    organic_matter = Column(Float)  # 有机质含量(%)
    ph = Column(Float)  # pH值
    test_date = Column(DateTime, default=datetime.now)

class FertilizerRecommendation(Base):
    __tablename__ = "fertilizer_recommendations"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    field_id = Column(String, index=True)
    nitrogen_recommendation = Column(Float)  # 氮肥推荐量(kg/ha)
    phosphorus_recommendation = Column(Float)  # 磷肥推荐量(kg/ha)
    potassium_recommendation = Column(Float)  # 钾肥推荐量(kg/ha)
    fertilizer_type = Column(String)  # 推荐肥料类型
    application_method = Column(String)  # 推荐施用方法
    application_timing = Column(String)  # 推荐施用时间
    created_at = Column(DateTime, default=datetime.now)
    
    # 存储输入参数，用于追溯
    soil_data = Column(JSON)  # 存储土壤数据的JSON
    crop_stage = Column(JSON)  # 存储作物生长阶段的JSON
    target_yield = Column(Float)  # 目标产量(kg/ha)