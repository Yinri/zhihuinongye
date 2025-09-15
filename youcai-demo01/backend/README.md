# YouCai 后端项目

这是YouCai前端项目的后端API服务，使用FastAPI框架开发，提供灌溉管理、施肥推荐、收获准备、病虫害防控和生产计划等功能。

## 项目结构

```
backend/
├── main.py              # 主应用入口文件
├── database.py          # 数据库配置文件
├── init_db.py           # 数据库初始化脚本
├── requirements.txt     # 项目依赖
├── routers/             # API路由
│   ├── irrigation.py           # 灌溉管理路由
│   ├── fertilizer.py           # 施肥推荐路由
│   ├── harvest_preparation.py  # 收获准备路由
│   ├── pest_control.py         # 病虫害防控路由
│   └── production_plan.py      # 生产计划路由
├── models/              # 数据库模型
│   ├── irrigation.py           # 灌溉相关模型
│   ├── fertilizer.py           # 施肥相关模型
│   ├── harvest_preparation.py  # 收获准备相关模型
│   ├── pest_control.py         # 病虫害防控相关模型
│   └── production_plan.py      # 生产计划相关模型
└── venv/                # Python虚拟环境
```

## 技术栈

- Python 3.8+
- FastAPI - Web框架
- SQLAlchemy - ORM工具
- SQLite - 数据库
- Pydantic - 数据验证
- Uvicorn - ASGI服务器

## 环境设置

### 1. 安装Python依赖

首先，确保你已经激活了虚拟环境：

```bash
# Windows
venv\Scripts\activate

# macOS/Linux
source venv/bin/activate
```

然后安装依赖：

```bash
pip install -r requirements.txt
```

### 2. 初始化数据库

运行初始化脚本创建数据库表并插入初始数据：

```bash
python init_db.py
```

## 运行项目

使用Uvicorn运行FastAPI应用：

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

- `--reload` - 开发模式，代码更改时自动重启服务器
- `--host 0.0.0.0` - 允许从其他设备访问
- `--port 5000` - 使用5000端口

## API文档

启动服务器后，可以访问以下URL查看自动生成的API文档：

- Swagger UI: http://localhost:5000/docs
- ReDoc: http://localhost:5000/redoc

## 主要功能模块

### 1. 灌溉管理
- 验证灌溉相关数据文件
- 上传灌溉相关数据文件
- 获取灌溉报告
- 获取趋势数据
- 获取分析数据
- 获取生长阶段信息

### 2. 施肥推荐
- 获取施肥推荐
- 获取土壤检测历史数据
- 提交土壤检测数据

### 3. 收获准备
- 获取收获计划列表
- 获取收获计划详情
- 创建收获计划
- 获取收获准备任务列表
- 更新任务状态
- 获取产量预测数据

### 4. 病虫害防控
- 获取病虫害趋势数据
- 获取病虫害检测记录
- 提交病虫害检测结果
- 获取病虫害预警信息

### 5. 生产计划
- 获取生产计划列表
- 获取生产计划详情
- 创建生产计划
- 更新生产计划

## 开发说明

1. **数据库操作**：使用SQLAlchemy ORM进行数据库操作，所有模型定义在`models/`目录下

2. **路由设计**：遵循RESTful API设计原则，每个功能模块有独立的路由文件

3. **数据验证**：使用Pydantic模型进行请求和响应数据的验证

4. **跨域处理**：已配置CORS中间件，允许前端应用访问API

## 注意事项

1. 本项目使用SQLite数据库，适合开发和测试环境。在生产环境中，建议使用PostgreSQL或MySQL等更强大的数据库

2. 默认端口为5000，请确保该端口未被其他应用占用

3. 数据库文件`youcai.db`将自动创建在项目根目录

4. 所有API接口都支持JSON格式的数据交换