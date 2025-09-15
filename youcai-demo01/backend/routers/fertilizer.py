from fastapi import APIRouter, HTTPException, Depends, File, UploadFile, Form
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
import datetime
from typing import List, Optional

from ..database import get_db
from ..models.fertilizer import SoilTestData as SoilTestDataModel, FertilizerRecommendation as FertilizerRecommendationModel

router = APIRouter()

# 请求和响应模型
class SoilTestData(BaseModel):
    nitrogen: float = Field(..., description="土壤氮含量(kg/ha)")
    phosphorus: float = Field(..., description="土壤磷含量(kg/ha)")
    potassium: float = Field(..., description="土壤钾含量(kg/ha)")
    organic_matter: float = Field(..., description="有机质含量(%)")
    ph: float = Field(..., description="pH值")

class CropGrowthStage(BaseModel):
    stage: str = Field(..., pattern="^(seedling|bud|flowering|maturity)$", description="作物生长阶段")
    days_after_planting: int = Field(..., description="播种后天数")

class FertilizerRecommendation(BaseModel):
    nitrogen_recommendation: float = Field(..., description="氮肥推荐量(kg/ha)")
    phosphorus_recommendation: float = Field(..., description="磷肥推荐量(kg/ha)")
    potassium_recommendation: float = Field(..., description="钾肥推荐量(kg/ha)")
    fertilizer_type: str = Field(..., description="推荐肥料类型")
    application_method: str = Field(..., description="推荐施用方法")
    application_timing: str = Field(..., description="推荐施用时间")

class SoilTestHistoryItem(BaseModel):
    id: str
    field_id: str
    nitrogen: float
    phosphorus: float
    potassium: float
    organic_matter: float
    ph: float
    test_date: str

@router.post("/fertilizer/recommendation", response_model=FertilizerRecommendation)
def get_fertilizer_recommendation(
    soil_data: SoilTestData,
    crop_stage: CropGrowthStage,
    target_yield: int,
    db: Session = Depends(get_db)
):
    """获取施肥推荐"""
    try:
        # 这里应该有实际的施肥推荐算法，现在返回模拟数据
        # 根据土壤数据、作物生长阶段和目标产量计算推荐施肥量
        nitrogen_recommendation = 0
        phosphorus_recommendation = 0
        potassium_recommendation = 0
        fertilizer_type = "复合肥"
        application_method = "撒施"
        application_timing = "早晨或傍晚"
        
        # 基于养分平衡法的追肥推荐算法
        # 作物养分需求系数 (kg/吨产量)
        CROP_NUTRIENT_REQUIREMENT = {
            'nitrogen': 2.5,    # 每生产1吨作物需要氮2.5kg
            'phosphorus': 1.2,  # 每生产1吨作物需要磷1.2kg
            'potassium': 2.0    # 每生产1吨作物需要钾2.0kg
        }
        
        # 肥料利用率 (%)
        FERTILIZER_EFFICIENCY = {
            'nitrogen': 0.5,    # 氮素利用率约50%
            'phosphorus': 0.6,  # 磷素利用率约60%
            'potassium': 0.5    # 钾素利用率约50%
        }
        
        # 肥料养分含量 (%)
        FERTILIZER_CONTENT = {
            'nitrogen': 0.46,   # 尿素含氮量约46%
            'phosphorus': 0.16, # 过磷酸钙含磷量约16%
            'potassium': 0.6   # 氯化钾含钾量约60%
        }
        
        # 目标产量养分总需求
        total_nitrogen_need = target_yield * CROP_NUTRIENT_REQUIREMENT['nitrogen']
        total_phosphorus_need = target_yield * CROP_NUTRIENT_REQUIREMENT['phosphorus']
        total_potassium_need = target_yield * CROP_NUTRIENT_REQUIREMENT['potassium']
        
        # 土壤养分供给量 (假设土壤养分有效供给系数为0.5)
        soil_nitrogen_supply = soil_data.nitrogen * 0.5
        soil_phosphorus_supply = soil_data.phosphorus * 0.5
        soil_potassium_supply = soil_data.potassium * 0.5
        
        # 计算需补充养分量
        nitrogen_to_supply = max(0, total_nitrogen_need - soil_nitrogen_supply)
        phosphorus_to_supply = max(0, total_phosphorus_need - soil_phosphorus_supply)
        potassium_to_supply = max(0, total_potassium_need - soil_potassium_supply)
        
        # 计算推荐施肥量
        nitrogen_recommendation = round(nitrogen_to_supply / FERTILIZER_CONTENT['nitrogen'] / FERTILIZER_EFFICIENCY['nitrogen'], 2)
        phosphorus_recommendation = round(phosphorus_to_supply / FERTILIZER_CONTENT['phosphorus'] / FERTILIZER_EFFICIENCY['phosphorus'], 2)
        potassium_recommendation = round(potassium_to_supply / FERTILIZER_CONTENT['potassium'] / FERTILIZER_EFFICIENCY['potassium'], 2)
        
        # 确保最低施肥量
        nitrogen_recommendation = max(20, nitrogen_recommendation)
        phosphorus_recommendation = max(10, phosphorus_recommendation)
        potassium_recommendation = max(15, potassium_recommendation)
        
        # 根据作物生长阶段调整推荐
        if crop_stage.stage == "seedling":
            fertilizer_type = "缓释氮肥"
            application_method = "穴施"
        elif crop_stage.stage == "flowering":
            fertilizer_type = "高磷钾肥"
            application_method = "根外追肥"
        
        # 保存推荐结果到数据库
        recommendation = FertilizerRecommendationModel(
            nitrogen_recommendation=nitrogen_recommendation,
            phosphorus_recommendation=phosphorus_recommendation,
            potassium_recommendation=potassium_recommendation,
            fertilizer_type=fertilizer_type,
            application_method=application_method,
            application_timing=application_timing,
            soil_data=soil_data.dict(),
            crop_stage=crop_stage.dict(),
            target_yield=target_yield
        )
        db.add(recommendation)
        db.commit()
        
        return FertilizerRecommendation(
            nitrogen_recommendation=nitrogen_recommendation,
            phosphorus_recommendation=phosphorus_recommendation,
            potassium_recommendation=potassium_recommendation,
            fertilizer_type=fertilizer_type,
            application_method=application_method,
            application_timing=application_timing
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/soil-test-history/{field_id}", response_model=List[SoilTestHistoryItem])
def get_soil_test_history(
    field_id: str,
    db: Session = Depends(get_db)
):
    """获取土壤检测历史数据"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        # 从数据库查询指定田块的土壤检测历史
        # history = db.query(SoilTestDataModel).filter(SoilTestDataModel.field_id == field_id).order_by(SoilTestDataModel.test_date.desc()).all()
        
        # 模拟数据
        history = [
            {
                "id": "test_001",
                "field_id": field_id,
                "nitrogen": 120.5,
                "phosphorus": 60.2,
                "potassium": 90.8,
                "organic_matter": 2.5,
                "ph": 6.8,
                "test_date": "2023-01-15T10:30:00"
            },
            {
                "id": "test_002",
                "field_id": field_id,
                "nitrogen": 110.2,
                "phosphorus": 55.7,
                "potassium": 85.3,
                "organic_matter": 2.3,
                "ph": 6.9,
                "test_date": "2022-11-20T09:15:00"
            },
            {
                "id": "test_003",
                "field_id": field_id,
                "nitrogen": 130.8,
                "phosphorus": 65.1,
                "potassium": 95.6,
                "organic_matter": 2.7,
                "ph": 6.7,
                "test_date": "2022-09-10T14:20:00"
            }
        ]
        
        return history
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/upload-soil-data")
async def upload_soil_test_data(
    file: UploadFile = File(...),
    field_id: str = Form(...),
    db: Session = Depends(get_db)
):
    """上传土壤检测数据Excel文件"""
    try:
        # 文件类型验证
        allowed_extensions = [".xlsx", ".xls"]
        ext = os.path.splitext(file.filename)[1].lower()
        if ext not in allowed_extensions:
            raise HTTPException(
                status_code=422,
                detail=f"文件格式无效。允许的格式: {', '.join(allowed_extensions)}"
            )
        
        # 读取文件内容
        file_content = await file.read()
        if len(file_content) == 0:
            raise HTTPException(status_code=422, detail="文件内容为空")
        
        # 解析Excel文件
        df = pd.read_excel(BytesIO(file_content))
        if df.empty:
            raise HTTPException(status_code=422, detail="Excel文件中没有数据")
        
        # 验证必要的列
        required_columns = ['nitrogen', 'phosphorus', 'potassium', 'organic_matter', 'ph']
        df.columns = df.columns.str.strip().str.lower()
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            raise HTTPException(
                status_code=422,
                detail=f"Excel文件缺少必要的列: {missing_columns}"
            )
        
        # 数据类型验证
        for col in required_columns:
            if not pd.api.types.is_numeric_dtype(df[col]):
                raise HTTPException(
                    status_code=422,
                    detail=f"列 '{col}' 包含非数值数据，请检查Excel文件"
                )
        
        # 保存数据到数据库
        records = []
        for _, row in df.iterrows():
            soil_test = SoilTestDataModel(
                field_id=field_id,
                nitrogen=row['nitrogen'],
                phosphorus=row['phosphorus'],
                potassium=row['potassium'],
                organic_matter=row['organic_matter'],
                ph=row['ph'],
                test_date=datetime.datetime.now()
            )
            db.add(soil_test)
            records.append(soil_test)
        
        db.commit()
        
        return {
            "status": "success",
            "message": f"成功导入 {len(records)} 条土壤检测数据",
            "data": {
                "count": len(records),
                "field_id": field_id,
                "import_time": datetime.datetime.now().isoformat()
            }
        }
    except Exception as e:
        db.rollback()
        logger.error(f"土壤数据导入失败: {str(e)}", exc_info=True)
        if 'Unsupported format' in str(e) or 'File is not a zip file' in str(e):
            raise HTTPException(status_code=422, detail="无效的Excel文件格式，请上传.xlsx或.xls文件")
        elif 'No columns to parse from file' in str(e):
            raise HTTPException(status_code=422, detail="Excel文件格式不正确，无法解析数据列")
        else:
            raise HTTPException(status_code=500, detail=f"数据导入失败: {str(e)}")

@router.post("/submit-soil-data")
def submit_soil_test_data(
    soil_data: SoilTestData,
    field_id: str,
    db: Session = Depends(get_db)
):
    """提交单条土壤检测数据"""
    try:
        # 创建土壤检测数据记录
        soil_test = SoilTestDataModel(
            field_id=field_id,
            nitrogen=soil_data.nitrogen,
            phosphorus=soil_data.phosphorus,
            potassium=soil_data.potassium,
            organic_matter=soil_data.organic_matter,
            ph=soil_data.ph,
            test_date=datetime.datetime.now()
        )
        
        # 保存到数据库
        db.add(soil_test)
        db.commit()
        db.refresh(soil_test)
        
        return {
            "status": "success",
            "message": "土壤检测数据提交成功",
            "data": {
                "id": soil_test.id,
                "field_id": soil_test.field_id,
                "test_date": soil_test.test_date.isoformat()
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))