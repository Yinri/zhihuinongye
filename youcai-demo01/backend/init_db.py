from sqlalchemy.orm import Session
from datetime import datetime, timedelta
import uuid

from database import engine, Base
from models.irrigation import IrrigationData, Report, TrendData, AnalysisData, GrowthStage
from models.fertilizer import SoilTestData, FertilizerRecommendation
from models.harvest_preparation import HarvestPlan, PreparationTask, YieldForecast
from models.pest_control import PestDetectionData, PestTrendData, PestWarning
from models.production_plan import ProductionPlan

# 创建所有数据库表
def create_tables():
    print("创建数据库表...")
    Base.metadata.create_all(bind=engine)
    print("数据库表创建完成!")

# 插入初始数据
def insert_initial_data():
    print("插入初始数据...")
    
    # 创建数据库会话
    db = Session(bind=engine)
    
    try:
        # 插入灌溉相关的初始数据
        insert_irrigation_initial_data(db)
        
        # 插入施肥相关的初始数据
        insert_fertilizer_initial_data(db)
        
        # 插入收获准备相关的初始数据
        insert_harvest_preparation_initial_data(db)
        
        # 插入病虫害防控相关的初始数据
        insert_pest_control_initial_data(db)
        
        # 插入生产计划相关的初始数据
        insert_production_plan_initial_data(db)
        
        # 提交事务
        db.commit()
        print("初始数据插入完成!")
    except Exception as e:
        print(f"插入初始数据时出错: {e}")
        db.rollback()
    finally:
        # 关闭会话
        db.close()

# 插入灌溉相关的初始数据
def insert_irrigation_initial_data(db):
    # 创建一些生长阶段数据
    growth_stages = [
        GrowthStage(
            id=str(uuid.uuid4()),
            crop_type="油菜",
            stage="发芽期",
            start_date=datetime(2022, 11, 1),
            end_date=datetime(2022, 11, 15),
            recommendations=["保持土壤湿润", "温度控制在15-20℃"]
        ),
        GrowthStage(
            id=str(uuid.uuid4()),
            crop_type="油菜",
            stage="幼苗期",
            start_date=datetime(2022, 11, 16),
            end_date=datetime(2022, 12, 31),
            recommendations=["适量浇水", "注意防治病虫害"]
        ),
        GrowthStage(
            id=str(uuid.uuid4()),
            crop_type="油菜",
            stage="抽薹期",
            start_date=datetime(2023, 1, 1),
            end_date=datetime(2023, 1, 31),
            recommendations=["增加灌溉量", "追施氮肥"]
        )
    ]
    
    db.add_all(growth_stages)

# 插入施肥相关的初始数据
def insert_fertilizer_initial_data(db):
    # 创建一些土壤检测数据
    soil_test_data = [
        SoilTestData(
            id=str(uuid.uuid4()),
            field_id="field_001",
            nitrogen=120.5,
            phosphorus=60.2,
            potassium=90.8,
            organic_matter=2.5,
            ph=6.8,
            test_date=datetime(2023, 1, 15)
        ),
        SoilTestData(
            id=str(uuid.uuid4()),
            field_id="field_001",
            nitrogen=110.2,
            phosphorus=55.7,
            potassium=85.3,
            organic_matter=2.3,
            ph=6.9,
            test_date=datetime(2022, 11, 20)
        )
    ]
    
    db.add_all(soil_test_data)

# 插入收获准备相关的初始数据
def insert_harvest_preparation_initial_data(db):
    # 创建一个收获计划
    harvest_plan = HarvestPlan(
        id=str(uuid.uuid4()),
        crop_type="油菜",
        planned_start_date=datetime(2023, 3, 20),
        planned_end_date=datetime(2023, 4, 10),
        area=50.5,
        expected_yield=250.0,
        status="in_progress"
    )
    
    db.add(harvest_plan)
    db.flush()  # 确保获取到ID
    
    # 创建相关的准备任务
    tasks = [
        PreparationTask(
            id=str(uuid.uuid4()),
            harvest_plan_id=harvest_plan.id,
            task_name="检查收获设备",
            task_type="equipment",
            status="in_progress",
            deadline=datetime(2023, 3, 13),
            responsible_person="张三"
        ),
        PreparationTask(
            id=str(uuid.uuid4()),
            harvest_plan_id=harvest_plan.id,
            task_name="组织收获人员",
            task_type="labor",
            status="pending",
            deadline=datetime(2023, 3, 15),
            responsible_person="李四"
        )
    ]
    
    db.add_all(tasks)

# 插入病虫害防控相关的初始数据
def insert_pest_control_initial_data(db):
    # 创建一些病虫害检测记录
    detection_records = [
        PestDetectionData(
            id=str(uuid.uuid4()),
            date=datetime(2023, 1, 15),
            severity="轻度",
            description="发现少量蚜虫，主要集中在叶片背面",
            solution="使用生物农药进行防治",
            area_affected=5.2
        ),
        PestDetectionData(
            id=str(uuid.uuid4()),
            date=datetime(2023, 1, 10),
            severity="中度",
            description="部分区域发现菜青虫，叶片有明显啃食痕迹",
            solution="喷洒低毒农药，每周一次，连续两周",
            area_affected=12.5
        )
    ]
    
    db.add_all(detection_records)

# 插入生产计划相关的初始数据
def insert_production_plan_initial_data(db):
    # 创建一些生产计划
    production_plans = [
        ProductionPlan(
            id=str(uuid.uuid4()),
            year=2023,
            season="spring",
            area=50.0,
            yield_target=25000.0,
            crop_variety="油菜品种A",
            planting_date=datetime(2022, 10, 30),
            fertilization_plan={"nitrogen": 120, "phosphorus": 80, "potassium": 100},
            status="executing"
        ),
        ProductionPlan(
            id=str(uuid.uuid4()),
            year=2023,
            season="autumn",
            area=60.0,
            yield_target=28000.0,
            crop_variety="油菜品种B",
            planting_date=datetime(2023, 8, 15),
            fertilization_plan={"nitrogen": 130, "phosphorus": 85, "potassium": 110},
            status="draft"
        )
    ]
    
    db.add_all(production_plans)

if __name__ == "__main__":
    create_tables()
    insert_initial_data()