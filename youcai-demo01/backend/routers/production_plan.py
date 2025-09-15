from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
import datetime
from typing import List, Optional, Dict

from ..database import get_db
from ..models.production_plan import ProductionPlan as ProductionPlanModel

router = APIRouter()

# 请求和响应模型
class FertilizationPlan(BaseModel):
    nitrogen: float
    phosphorus: float
    potassium: float

class ProductionPlanItem(BaseModel):
    id: str
    year: int
    season: str = Field(..., pattern="^(spring|autumn)$")
    area: float
    yield_target: float
    status: str = Field(..., pattern="^(draft|approved|executing|completed)$")
    created_at: str
    updated_at: str

class CreateProductionPlanRequest(BaseModel):
    year: int
    season: str = Field(..., pattern="^(spring|autumn)$")
    area: float
    yield_target: float
    crop_variety: str
    planting_date: str
    fertilization_plan: FertilizationPlan

class UpdateProductionPlanRequest(BaseModel):
    year: Optional[int] = None
    season: Optional[str] = Field(None, pattern="^(spring|autumn)$")
    area: Optional[float] = None
    yield_target: Optional[float] = None
    crop_variety: Optional[str] = None
    planting_date: Optional[str] = None
    fertilization_plan: Optional[FertilizationPlan] = None
    status: Optional[str] = Field(None, pattern="^(draft|approved|executing|completed)$")

@router.get("/production-plans", response_model=List[ProductionPlanItem])
def get_production_plans(
    db: Session = Depends(get_db)
):
    """获取生产计划列表"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # plans = db.query(ProductionPlanModel).order_by(ProductionPlanModel.year.desc(), ProductionPlanModel.season).all()
        
        # 模拟数据
        plans = [
            {
                "id": "plan_001",
                "year": 2023,
                "season": "spring",
                "area": 50.0,
                "yield_target": 25000.0,
                "status": "executing",
                "created_at": "2022-12-15T10:30:00",
                "updated_at": "2023-01-10T14:45:00"
            },
            {
                "id": "plan_002",
                "year": 2023,
                "season": "autumn",
                "area": 60.0,
                "yield_target": 28000.0,
                "status": "draft",
                "created_at": "2023-01-05T09:15:00",
                "updated_at": "2023-01-05T09:15:00"
            },
            {
                "id": "plan_003",
                "year": 2022,
                "season": "autumn",
                "area": 45.0,
                "yield_target": 22000.0,
                "status": "completed",
                "created_at": "2022-08-20T16:20:00",
                "updated_at": "2022-12-30T11:30:00"
            }
        ]
        
        return plans
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/production-plans/{plan_id}", response_model=ProductionPlanItem)
def get_production_plan_detail(
    plan_id: str,
    db: Session = Depends(get_db)
):
    """获取生产计划详情"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # plan = db.query(ProductionPlanModel).filter(ProductionPlanModel.id == plan_id).first()
        # if not plan:
        #     raise HTTPException(status_code=404, detail="生产计划不存在")
        
        # 模拟数据
        plan = {
            "id": plan_id,
            "year": 2023,
            "season": "spring",
            "area": 50.0,
            "yield_target": 25000.0,
            "status": "executing",
            "created_at": "2022-12-15T10:30:00",
            "updated_at": "2023-01-10T14:45:00"
        }
        
        return plan
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/production-plans", response_model=ProductionPlanItem)
def create_production_plan(
    plan_data: CreateProductionPlanRequest,
    db: Session = Depends(get_db)
):
    """创建生产计划"""
    try:
        # 创建生产计划
        production_plan = ProductionPlanModel(
            year=plan_data.year,
            season=plan_data.season,
            area=plan_data.area,
            yield_target=plan_data.yield_target,
            crop_variety=plan_data.crop_variety,
            planting_date=datetime.datetime.strptime(plan_data.planting_date, "%Y-%m-%d"),
            fertilization_plan=plan_data.fertilization_plan.dict(),
            status="draft",
            created_at=datetime.datetime.now(),
            updated_at=datetime.datetime.now()
        )
        
        # 保存到数据库
        db.add(production_plan)
        db.commit()
        db.refresh(production_plan)
        
        return {
            "id": production_plan.id,
            "year": production_plan.year,
            "season": production_plan.season,
            "area": production_plan.area,
            "yield_target": production_plan.yield_target,
            "status": production_plan.status,
            "created_at": production_plan.created_at.isoformat(),
            "updated_at": production_plan.updated_at.isoformat()
        }
    except ValueError as e:
        # 处理日期格式错误
        raise HTTPException(status_code=400, detail=f"日期格式错误: {str(e)}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.patch("/production-plans/{plan_id}", response_model=ProductionPlanItem)
def update_production_plan(
    plan_id: str,
    plan_data: UpdateProductionPlanRequest,
    db: Session = Depends(get_db)
):
    """更新生产计划"""
    try:
        # 这里应该有实际的查询和更新逻辑，现在返回模拟数据
        # plan = db.query(ProductionPlanModel).filter(ProductionPlanModel.id == plan_id).first()
        # if not plan:
        #     raise HTTPException(status_code=404, detail="生产计划不存在")
        
        # 模拟数据 - 模拟从数据库获取的计划
        original_plan = {
            "id": plan_id,
            "year": 2023,
            "season": "spring",
            "area": 50.0,
            "yield_target": 25000.0,
            "status": "draft",
            "created_at": "2022-12-15T10:30:00",
            "updated_at": "2022-12-15T10:30:00"
        }
        
        # 构建更新后的计划数据
        updated_plan = original_plan.copy()
        
        # 应用更新
        if plan_data.year is not None:
            updated_plan["year"] = plan_data.year
        if plan_data.season is not None:
            updated_plan["season"] = plan_data.season
        if plan_data.area is not None:
            updated_plan["area"] = plan_data.area
        if plan_data.yield_target is not None:
            updated_plan["yield_target"] = plan_data.yield_target
        if plan_data.status is not None:
            updated_plan["status"] = plan_data.status
        
        # 更新更新时间
        updated_plan["updated_at"] = datetime.datetime.now().isoformat()
        
        # 实际应用中应该更新数据库记录
        # for key, value in plan_data.dict(exclude_unset=True).items():
        #     if key == "planting_date" and value:
        #         setattr(plan, key, datetime.datetime.strptime(value, "%Y-%m-%d"))
        #     elif key == "fertilization_plan" and value:
        #         setattr(plan, key, value.dict())
        #     else:
        #         setattr(plan, key, value)
        # 
        # plan.updated_at = datetime.datetime.now()
        # db.commit()
        # db.refresh(plan)
        
        return updated_plan
    except HTTPException as e:
        raise e
    except ValueError as e:
        # 处理日期格式错误
        raise HTTPException(status_code=400, detail=f"日期格式错误: {str(e)}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))