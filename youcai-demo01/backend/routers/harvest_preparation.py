from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
import datetime
from typing import List, Optional

from ..database import get_db
from ..models.harvest_preparation import HarvestPlan as HarvestPlanModel, PreparationTask as PreparationTaskModel, YieldForecast as YieldForecastModel

router = APIRouter()

# 请求和响应模型
class HarvestPlanBase(BaseModel):
    crop_type: str = Field(..., description="作物类型")
    planned_start_date: str = Field(..., description="计划开始日期")
    planned_end_date: str = Field(..., description="计划结束日期")
    area: float = Field(..., description="收获面积(亩)")
    expected_yield: float = Field(..., description="预计产量(kg/亩)")

class HarvestPlan(HarvestPlanBase):
    id: str = Field(..., description="计划ID")
    status: str = Field(..., pattern="^(draft|in_progress|completed)$", description="状态")

class HarvestPlanCreate(HarvestPlanBase):
    pass

class PreparationTaskBase(BaseModel):
    task_name: str = Field(..., description="任务名称")
    task_type: str = Field(..., pattern="^(equipment|labor|field|other)$", description="任务类型")
    status: str = Field(..., pattern="^(pending|in_progress|completed)$", description="状态")
    deadline: str = Field(..., description="截止日期")
    responsible_person: str = Field(..., description="负责人")

class PreparationTask(PreparationTaskBase):
    id: str = Field(..., description="任务ID")
    harvest_plan_id: str = Field(..., description="收获计划ID")

class YieldForecastFactor(BaseModel):
    name: str
    value: float
    impact: str = Field(..., pattern="^(positive|negative|neutral)$")

class YieldForecast(BaseModel):
    date: str
    forecast_value: float
    confidence: float
    factors: List[YieldForecastFactor]

class UpdateTaskStatus(BaseModel):
    status: str = Field(..., pattern="^(pending|in_progress|completed)$")

@router.get("/harvest-plans", response_model=List[HarvestPlan])
def get_harvest_plans(db: Session = Depends(get_db)):
    """获取收获计划列表"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # plans = db.query(HarvestPlanModel).all()
        
        # 模拟数据
        plans = [
            {
                "id": "plan_001",
                "crop_type": "油菜",
                "planned_start_date": "2023-03-20",
                "planned_end_date": "2023-04-10",
                "area": 50.5,
                "expected_yield": 250.0,
                "status": "in_progress"
            },
            {
                "id": "plan_002",
                "crop_type": "小麦",
                "planned_start_date": "2023-05-15",
                "planned_end_date": "2023-06-05",
                "area": 100.0,
                "expected_yield": 450.0,
                "status": "draft"
            }
        ]
        
        return plans
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/harvest-plans/{plan_id}", response_model=HarvestPlan)
def get_harvest_plan_detail(
    plan_id: str,
    db: Session = Depends(get_db)
):
    """获取收获计划详情"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # plan = db.query(HarvestPlanModel).filter(HarvestPlanModel.id == plan_id).first()
        # if not plan:
        #     raise HTTPException(status_code=404, detail="收获计划不存在")
        
        # 模拟数据
        plan = {
            "id": plan_id,
            "crop_type": "油菜",
            "planned_start_date": "2023-03-20",
            "planned_end_date": "2023-04-10",
            "area": 50.5,
            "expected_yield": 250.0,
            "status": "in_progress"
        }
        
        return plan
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/harvest-plans", response_model=HarvestPlan)
def create_harvest_plan(
    plan_data: HarvestPlanCreate,
    db: Session = Depends(get_db)
):
    """创建收获计划"""
    try:
        # 创建收获计划
        harvest_plan = HarvestPlanModel(
            crop_type=plan_data.crop_type,
            planned_start_date=datetime.datetime.strptime(plan_data.planned_start_date, "%Y-%m-%d"),
            planned_end_date=datetime.datetime.strptime(plan_data.planned_end_date, "%Y-%m-%d"),
            area=plan_data.area,
            expected_yield=plan_data.expected_yield,
            status="draft"
        )
        
        # 保存到数据库
        db.add(harvest_plan)
        db.commit()
        db.refresh(harvest_plan)
        
        # 创建默认的准备任务
        default_tasks = [
            {
                "task_name": "检查收获设备",
                "task_type": "equipment",
                "status": "pending",
                "deadline": (datetime.datetime.strptime(plan_data.planned_start_date, "%Y-%m-%d") - datetime.timedelta(days=7)).strftime("%Y-%m-%d"),
                "responsible_person": "张三"
            },
            {
                "task_name": "组织收获人员",
                "task_type": "labor",
                "status": "pending",
                "deadline": (datetime.datetime.strptime(plan_data.planned_start_date, "%Y-%m-%d") - datetime.timedelta(days=5)).strftime("%Y-%m-%d"),
                "responsible_person": "李四"
            },
            {
                "task_name": "准备存储设施",
                "task_type": "field",
                "status": "pending",
                "deadline": (datetime.datetime.strptime(plan_data.planned_start_date, "%Y-%m-%d") - datetime.timedelta(days=3)).strftime("%Y-%m-%d"),
                "responsible_person": "王五"
            }
        ]
        
        for task_data in default_tasks:
            task = PreparationTaskModel(
                harvest_plan_id=harvest_plan.id,
                task_name=task_data["task_name"],
                task_type=task_data["task_type"],
                status=task_data["status"],
                deadline=datetime.datetime.strptime(task_data["deadline"], "%Y-%m-%d"),
                responsible_person=task_data["responsible_person"]
            )
            db.add(task)
        
        db.commit()
        
        return {
            "id": harvest_plan.id,
            "crop_type": harvest_plan.crop_type,
            "planned_start_date": harvest_plan.planned_start_date.strftime("%Y-%m-%d"),
            "planned_end_date": harvest_plan.planned_end_date.strftime("%Y-%m-%d"),
            "area": harvest_plan.area,
            "expected_yield": harvest_plan.expected_yield,
            "status": harvest_plan.status
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/harvest-plans/{plan_id}/tasks", response_model=List[PreparationTask])
def get_preparation_tasks(
    plan_id: str,
    db: Session = Depends(get_db)
):
    """获取收获准备任务列表"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # tasks = db.query(PreparationTaskModel).filter(PreparationTaskModel.harvest_plan_id == plan_id).all()
        
        # 模拟数据
        tasks = [
            {
                "id": "task_001",
                "harvest_plan_id": plan_id,
                "task_name": "检查收获设备",
                "task_type": "equipment",
                "status": "in_progress",
                "deadline": "2023-03-13",
                "responsible_person": "张三"
            },
            {
                "id": "task_002",
                "harvest_plan_id": plan_id,
                "task_name": "组织收获人员",
                "task_type": "labor",
                "status": "pending",
                "deadline": "2023-03-15",
                "responsible_person": "李四"
            },
            {
                "id": "task_003",
                "harvest_plan_id": plan_id,
                "task_name": "准备存储设施",
                "task_type": "field",
                "status": "pending",
                "deadline": "2023-03-17",
                "responsible_person": "王五"
            }
        ]
        
        return tasks
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.patch("/preparation-tasks/{task_id}/status")
def update_task_status(
    task_id: str,
    status_update: UpdateTaskStatus,
    db: Session = Depends(get_db)
):
    """更新任务状态"""
    try:
        # 这里应该有实际的更新逻辑，现在返回模拟数据
        # task = db.query(PreparationTaskModel).filter(PreparationTaskModel.id == task_id).first()
        # if not task:
        #     raise HTTPException(status_code=404, detail="任务不存在")
        # 
        # task.status = status_update.status
        # db.commit()
        
        return {
            "status": "success",
            "message": f"任务 {task_id} 状态已更新为 {status_update.status}"
        }
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/harvest-plans/{plan_id}/yield-forecast", response_model=List[YieldForecast])
def get_yield_forecast(
    plan_id: str,
    db: Session = Depends(get_db)
):
    """获取产量预测数据"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # forecasts = db.query(YieldForecastModel).filter(YieldForecastModel.plan_id == plan_id).order_by(YieldForecastModel.date.asc()).all()
        
        # 模拟数据
        forecasts = [
            {
                "date": "2023-03-01",
                "forecast_value": 240.0,
                "confidence": 85.0,
                "factors": [
                    {"name": "天气", "value": 90.0, "impact": "positive"},
                    {"name": "土壤条件", "value": 80.0, "impact": "positive"},
                    {"name": "病虫害", "value": 95.0, "impact": "positive"}
                ]
            },
            {
                "date": "2023-03-10",
                "forecast_value": 245.0,
                "confidence": 88.0,
                "factors": [
                    {"name": "天气", "value": 92.0, "impact": "positive"},
                    {"name": "土壤条件", "value": 82.0, "impact": "positive"},
                    {"name": "病虫害", "value": 93.0, "impact": "positive"}
                ]
            },
            {
                "date": "2023-03-20",
                "forecast_value": 250.0,
                "confidence": 90.0,
                "factors": [
                    {"name": "天气", "value": 95.0, "impact": "positive"},
                    {"name": "土壤条件", "value": 85.0, "impact": "positive"},
                    {"name": "病虫害", "value": 90.0, "impact": "positive"}
                ]
            }
        ]
        
        return forecasts
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))