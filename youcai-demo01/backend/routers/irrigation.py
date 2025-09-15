from fastapi import APIRouter, UploadFile, File, HTTPException, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
import pandas as pd
import os
import uuid
import datetime
from typing import List, Optional
from io import BytesIO

from ..database import get_db
from ..models.irrigation import IrrigationData, Report, TrendData, AnalysisData, GrowthStage
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from pydantic import BaseModel
from typing import List
from backend.irrigation_model import calculate_improved_irrigation
import logging

router = APIRouter()

# 创建上传文件保存目录
UPLOAD_DIR = "data"
if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)

@router.get("/validate-data")
def validate_irrigation_data(db: Session = Depends(get_db)):
    """验证灌溉相关数据文件"""
    try:
        # 这里应该有实际的验证逻辑，现在返回模拟数据
        return JSONResponse({
            "status": "success",
            "message": "数据验证成功",
            "data": {
                "ndvi_data": True,
                "soil_data": True,
                "weather_data": True
            }
        })
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@router.post("/upload")
async def upload_irrigation_data(
    ndvi_file: UploadFile = File(...),
    soil_file: UploadFile = File(...),
    weather_file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    # 文件类型验证
    allowed_extensions = [".xlsx", ".xls"]
    
    files = [ndvi_file, soil_file, weather_file]
    file_contents = []
    column_names = {}
    
    for file in files:
        logger.info(f"开始处理文件: {file.filename}")
        # 验证文件扩展名
        ext = os.path.splitext(file.filename)[1].lower()
        if ext not in allowed_extensions:
            logger.error(f"文件 {file.filename} 扩展名无效: {ext}")
            raise HTTPException(
                status_code=422,
                detail=f"文件 {file.filename} 格式无效。允许的格式: {', '.join(allowed_extensions)}"
            )
        
        # 读取文件内容一次并保存
        file_content = await file.read()
        logger.info(f"读取文件 {file.filename} 内容，大小: {len(file_content)} 字节")
        if len(file_content) == 0:
            logger.error(f"文件 {file.filename} 内容为空")
            raise HTTPException(
                status_code=422,
                detail=f"文件 {file.filename} 内容为空"
            )
        
        # 验证Excel内容
        try:
            # 使用BytesIO直接从内存内容读取，避免二次文件操作
            df = pd.read_excel(BytesIO(file_content))
            logger.info(f"成功解析文件 {file.filename}，数据行数: {len(df)}")
            if df.empty:
                logger.error(f"文件 {file.filename} 解析后内容为空")
                raise HTTPException(
                    status_code=422,
                    detail=f"文件 {file.filename} 内容为空或无法解析"
                )
            # 更新文件验证部分的列检查
            if 'ndvi' in file.filename.lower():
                required_columns = ['日期', 'ndvi']
            elif 'weather' in file.filename.lower():
                required_columns = ['日期', 'avg_temp', 'humidity', 'wind_speed', 'solar_radiation', 'rainfall']
            elif 'soil' in file.filename.lower():
                required_columns = ['日期', 'soil_moisture']
            else:
                required_columns = ['date', 'value']
            
            # 检查列是否存在（不区分大小写）
            df_columns = [col.lower() for col in df.columns]
            missing_columns = [col for col in required_columns if col.lower() not in df_columns]
            
            if missing_columns:
                logger.error(f"文件 {file.filename} 缺少必要的列: {missing_columns}")
                raise HTTPException(
                    status_code=422,
                    detail=f"文件 {file.filename} 格式不正确，缺少必要的列: {missing_columns}。请检查Excel文件是否包含这些列。"
                )
            
            # 验证数据类型
            try:
                # 转换日期列
                date_col = next(col for col in df.columns if col.lower() == '日期'.lower())
                df[date_col] = pd.to_datetime(df[date_col])
                
                # 验证数值列
                if 'ndvi' in file.filename.lower():
                    value_col = next(col for col in df.columns if col.lower() == 'ndvi')
                    df[value_col] = pd.to_numeric(df[value_col])
                    if not (df[value_col] >= 0).all() or not (df[value_col] <= 1).all():
                        raise HTTPException(
                            status_code=422,
                            detail=f"文件 {file.filename} 中的NDVI值必须在0到1之间。"
                        )
                elif 'soil' in file.filename.lower():
                    value_col = next(col for col in df.columns if col.lower() == 'soil_moisture')
                    df[value_col] = pd.to_numeric(df[value_col])
                    if not (df[value_col] >= 0).all() or not (df[value_col] <= 100).all():
                        raise HTTPException(
                            status_code=422,
                            detail=f"文件 {file.filename} 中的土壤湿度值必须在0到100之间。"
                        )
                elif 'weather' in file.filename.lower():
                    temp_col = next(col for col in df.columns if col.lower() == 'avg_temp')
                    humidity_col = next(col for col in df.columns if col.lower() == 'humidity')
                    wind_speed_col = next(col for col in df.columns if col.lower() == 'wind_speed')
                    solar_radiation_col = next(col for col in df.columns if col.lower() == 'solar_radiation')
                    rainfall_col = next(col for col in df.columns if col.lower() == 'rainfall')
                    column_names['temp_col'] = temp_col
                    column_names['humidity_col'] = humidity_col
                    column_names['wind_speed_col'] = wind_speed_col
                    column_names['solar_radiation_col'] = solar_radiation_col
                    column_names['rainfall_col'] = rainfall_col
                    df[temp_col] = pd.to_numeric(df[temp_col])
                    df[humidity_col] = pd.to_numeric(df[humidity_col])
                    df[wind_speed_col] = pd.to_numeric(df[wind_speed_col])
                    df[solar_radiation_col] = pd.to_numeric(df[solar_radiation_col])
                    df[rainfall_col] = pd.to_numeric(df[rainfall_col])
                    if not (df[temp_col] >= -20).all() or not (df[temp_col] <= 50).all():
                        raise HTTPException(
                            status_code=422,
                            detail=f"文件 {file.filename} 中的温度值必须在-20到50之间。"
                        )
                    if not (df[rainfall_col] >= 0).all():
                        raise HTTPException(
                            status_code=422,
                            detail=f"文件 {file.filename} 中的降雨量不能为负数。"
                        )
            except ValueError as e:
                logger.error(f"文件 {file.filename} 数据格式错误: {str(e)}")
                if 'date' in str(e).lower():
                    detail = f"文件 {file.filename} 中的日期格式无效，请使用有效的日期格式（如YYYY-MM-DD）。"
                else:
                    detail = f"文件 {file.filename} 中的数据格式无效，请确保数值列包含有效的数字。"
                raise HTTPException(status_code=422, detail=detail)
            except StopIteration:
                # 这应该不会发生，因为前面已经检查了列存在性
                pass
        except Exception as e:
            logger.error(f"文件 {file.filename} 解析失败: {str(e)}", exc_info=True)
            # 捕获并返回更具体的错误信息
            error_msg = str(e)
            if 'Unsupported format' in error_msg or 'File is not a zip file' in error_msg:
                detail = f"文件 {file.filename} 不是有效的Excel文件，请确保上传的是.xlsx或.xls格式的文件。"
            elif 'Worksheet' in error_msg:
                detail = f"文件 {file.filename} 缺少必要的工作表，请检查Excel文件结构。"
            else:
                detail = f"文件 {file.filename} 解析失败: {error_msg}"
            raise HTTPException(
                status_code=422,
                detail=detail
            )
        
        file_contents.append((file, file_content))
    
    # 保存上传的文件
    upload_dir = os.path.abspath("data")
    os.makedirs(upload_dir, exist_ok=True)
    os.chmod(upload_dir, 0o777)  # More permissive for Windows compatibility
    
    filenames = []
    for file, content in file_contents:
        file_path = os.path.join(upload_dir, file.filename)
        try:
            with open(file_path, "wb") as f:
                f.write(content)
            filenames.append(file.filename)
        except PermissionError:
            # Handle locked file on Windows
            if os.path.exists(file_path):
                os.remove(file_path)
            with open(file_path, "wb") as f:
                f.write(content)
            filenames.append(file.filename)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"无法写入文件: {str(e)}")
    
    # 根据文件名解析对应的数据
    ndvi_df = None
    soil_df = None
    weather_df = None
    
    for file, content in file_contents:
        filename = file.filename.lower()
        try:
            if 'ndvi' in filename:
                df = pd.read_excel(BytesIO(content), sheet_name=0, header=0, converters={'ndvi': float})
            elif 'soil' in filename:
                df = pd.read_excel(BytesIO(content), sheet_name=0, header=0, converters={'soil_moisture': float})
            elif 'weather' in filename:
                df = pd.read_excel(BytesIO(content), sheet_name=0, header=0, converters={
                    'avg_temp': float,
                    'humidity': float,
                    'wind_speed': float,
                    'rainfall': float,
                    'solar_radiation': float
                })
            df.columns = df.columns.str.strip().str.lower()
            
            if 'ndvi' in filename:
                required_columns = ['日期', 'ndvi']
                missing_columns = [col for col in required_columns if col not in df.columns]
                if missing_columns:
                    raise ValueError(f"NDVI文件缺少必要的列: {missing_columns}")
                if df.empty:
                    raise ValueError("NDVI文件中没有数据行")
                ndvi_df = df
                ndvi_df['ndvi'] = pd.to_numeric(ndvi_df['ndvi'], errors='coerce')
                # 显式指定日期格式以处理中文日期
                ndvi_df['日期'] = pd.to_datetime(ndvi_df['日期'], format='%Y-%m-%d', errors='coerce')
                ndvi_df.dropna(subset=['日期'], inplace=True)
                if ndvi_df.empty:
                    raise ValueError("NDVI文件中没有有效日期数据，请检查日期格式是否为YYYY-MM-DD")
                ndvi_df.sort_values('日期', inplace=True)
            elif 'soil' in filename:
                df = pd.read_excel(BytesIO(content), sheet_name=0, header=0, converters={'soil_moisture': float})
                df.columns = df.columns.str.strip().str.lower()
                required_columns = ['日期', 'soil_moisture']
                missing_columns = [col for col in required_columns if col not in df.columns]
                if missing_columns:
                    raise ValueError(f"土壤湿度文件缺少必要的列: {missing_columns}")
                if df.empty:
                    raise ValueError("土壤湿度文件中没有数据行")
                soil_df = df
                soil_df['soil_moisture'] = pd.to_numeric(soil_df['soil_moisture'], errors='coerce')
                # 显式指定日期格式以处理中文日期
                soil_df['日期'] = pd.to_datetime(soil_df['日期'], format='%Y-%m-%d', errors='coerce')
                soil_df.dropna(subset=['日期'], inplace=True)
                if soil_df.empty:
                    raise ValueError("土壤湿度文件中没有有效日期数据，请检查日期格式是否为YYYY-MM-DD")
                soil_df.sort_values('日期', inplace=True)
            elif 'weather' in filename:
                # 使用转换器确保数值列正确解析
                df = pd.read_excel(BytesIO(content), sheet_name=0, header=0, converters={
                    'avg_temp': float,
                    'humidity': float,
                    'wind_speed': float,
                    'rainfall': float,
                    'solar_radiation': float
                })
                df.columns = df.columns.str.strip().str.lower()
                required_columns = ['日期', 'avg_temp', 'humidity', 'wind_speed', 'rainfall', 'solar_radiation']
                missing_columns = [col for col in required_columns if col not in df.columns]
                if missing_columns:
                    raise ValueError(f"气象数据文件缺少必要的列: {missing_columns}")
                if df.empty:
                    raise ValueError("气象数据文件中没有数据行")
                weather_df = df
                weather_df['日期'] = pd.to_datetime(weather_df['日期'], format='%Y-%m-%d', errors='coerce')
                # 显式指定日期格式以处理中文日期
                weather_df['日期'] = pd.to_datetime(weather_df['日期'], format='%Y-%m-%d', errors='coerce')
                weather_df.dropna(subset=['日期'], inplace=True)
                if weather_df.empty:
                    raise ValueError("气象文件中没有有效日期数据，请检查日期格式是否为YYYY-MM-DD")
                weather_df.sort_values('日期', inplace=True)
                weather_df['humidity'] = pd.to_numeric(weather_df['humidity'], errors='coerce')
                weather_df['wind_speed'] = pd.to_numeric(weather_df['wind_speed'], errors='coerce')
                weather_df['rainfall'] = pd.to_numeric(weather_df['rainfall'], errors='coerce')
                weather_df['solar_radiation'] = pd.to_numeric(weather_df['solar_radiation'], errors='coerce')
            else:
                raise ValueError(f"无法识别的文件类型: {filename}。请确保文件名包含'ndvi'、'soil'或'weather'。")
        except Exception as e:
            logger.error(f"文件 {file.filename} 解析失败: 类型 {type(e).__name__}, 消息 {str(e)}, 参数 {e.args}", exc_info=True)
            raise HTTPException(status_code=422, detail=f"文件 {file.filename} 解析失败: {str(e)}")
    
    # 转换日期列并按日期排序
    for df in [ndvi_df, soil_df, weather_df]:
        df['日期'] = pd.to_datetime(df['日期'])
        df.sort_values('日期', inplace=True)

    # 验证所有必要数据都已解析
    if ndvi_df is None or soil_df is None or weather_df is None:
        raise HTTPException(status_code=422, detail="缺少必要的数据文件: 请确保上传了NDVI、土壤湿度和气象数据文件")

    # 提取各数据列名并存储到column_names字典
    column_names = {
        'temp_col': 'avg_temp',
        'humidity_col': 'humidity',
        'wind_speed_col': 'wind_speed',
        'solar_radiation_col': 'solar_radiation',
        'rainfall_col': 'rainfall'
    }

    # 提取历史NDVI数据
    historical_data = [{
        "ndvi": row["ndvi"]
    } for _, row in ndvi_df.iterrows()]

    # 获取最新气象数据
    latest_weather = weather_df.iloc[-1].fillna(0)
    latest_soil = soil_df.iloc[-1].fillna(0)

    # 准备算法输入数据
    # 获取解析阶段确定的实际列名
    ndvi_col = next(col for col in ndvi_df.columns if col.lower() == 'ndvi')
    soil_moisture_col = next(col for col in soil_df.columns if col.lower() == 'soil_moisture')
        
    latest_data = {
            "avg_temp": latest_weather[column_names['temp_col']],
        "humidity": latest_weather[column_names['humidity_col']],
        "wind_speed": latest_weather[column_names['wind_speed_col']],
        "solar_radiation": latest_weather[column_names['solar_radiation_col']],
        "rainfall": latest_weather[column_names['rainfall_col']],
            "soil_moisture": latest_soil[soil_moisture_col],
            "ndvi": ndvi_df.iloc[-1][ndvi_col]
        }

    # 计算灌溉量
    irrigation_amount = calculate_improved_irrigation(
        historical_data=historical_data,
        latest_data=latest_data
    )

    # 准备前端需要的数据格式 - 匹配前端SmartIrrigation.vue中uploadFiles函数的期望格式
    return {
        "irrigationAmount": round(irrigation_amount, 2),  # 使用驼峰命名以匹配前端期望
        "soilMoisture": latest_data['soil_moisture'],      # 直接放在顶层，不嵌套
        "ndvi": latest_data['ndvi'],
        "temperature": latest_data['avg_temp'],
        "rainfall": latest_data['rainfall'],
        "humidity": latest_data['humidity'],
        "windSpeed": latest_data['wind_speed'],
        "solarRadiation": latest_data['solar_radiation']
    }

@router.get("/report")
def get_report(db: Session = Depends(get_db)):
    """获取灌溉报告"""
    try:
        # 返回符合前端期望的数据结构
        return {
            "irrigation_amount": 125.5,
            "soil_moisture": 65.3,
            "last_irrigation_time": "08:30",
            "recommendations": "今日需灌溉125.5立方米"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/trend")
def get_trend(db: Session = Depends(get_db)):
    """获取趋势数据"""
    try:
        # 返回符合前端期望的数据结构，包含灌溉预测和趋势数据
        return {
            "irrigation_prediction": [120, 135, 128, 140, 132],
            "data": {
                "trend_data": {
                    "dates": ["10/10", "10/11", "10/12", "10/13", "10/14", "10/15", "10/16"],
                    "soil_moisture": [62, 58, 55, 70, 68, 65, 63],
                    "irrigation": [120, 140, 150, 90, 100, 125, 130]
                }
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/analysis")
def get_analysis(db: Session = Depends(get_db)):
    """获取分析数据"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        return {
            "current_stage": "抽薹期",
            "irrigation_needed": True,
            "recommended_amount": 25,  # 推荐灌溉量(mm)
            "next_irrigation_date": "2023-01-25",
            "soil_condition": {
                "moisture": "中等",
                "nutrients": "充足",
                "ph": 6.8
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/growth-stages")
def get_growth_stages(db: Session = Depends(get_db)):
    """获取生长阶段信息"""
    try:
        # 这里应该有实际的查询逻辑，现在返回模拟数据
        return {
            "stages": [
                {
                    "stage": "发芽期",
                    "startDate": "2022-11-01",
                    "endDate": "2022-11-15",
                    "recommendations": ["保持土壤湿润", "温度控制在15-20℃"]
                },
                {
                    "stage": "幼苗期",
                    "startDate": "2022-11-16",
                    "endDate": "2022-12-31",
                    "recommendations": ["适量浇水", "注意防治病虫害"]
                },
                {
                    "stage": "抽薹期",
                    "startDate": "2023-01-01",
                    "endDate": "2023-01-31",
                    "recommendations": ["增加灌溉量", "追施氮肥"]
                },
                {
                    "stage": "开花期",
                    "startDate": "2023-02-01",
                    "endDate": "2023-02-28",
                    "recommendations": ["保持土壤湿润", "注意授粉"]
                },
                {
                    "stage": "成熟期",
                    "startDate": "2023-03-01",
                    "endDate": "2023-03-31",
                    "recommendations": ["减少灌溉量", "准备收获"]
                }
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


class HistoricalDataPoint(BaseModel):
    ndvi: float

class LatestData(BaseModel):
    avg_temp: float
    humidity: float
    wind_speed: float
    solar_radiation: float
    rainfall: float
    soil_moisture: float
    ndvi: float

class IrrigationRequest(BaseModel):
    historical_data: List[HistoricalDataPoint]
    latest_data: LatestData

class IrrigationResponse(BaseModel):
    irrigation_amount: float

@router.post("/predict", response_model=IrrigationResponse)
async def predict_irrigation(request: IrrigationRequest):
    """使用改进的灌溉量计算算法预测灌溉需求"""
    # 将Pydantic模型转换为字典
    latest_data_dict = request.latest_data.dict()
    historical_data_list = [item.dict() for item in request.historical_data]
    
    # 计算灌溉量
    irrigation_amount = calculate_improved_irrigation(
        historical_data=historical_data_list,
        latest_data=latest_data_dict
    )
    
    return IrrigationResponse(irrigation_amount=irrigation_amount)