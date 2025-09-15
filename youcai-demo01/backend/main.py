from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .routers import irrigation, fertilizer, harvest_preparation, pest_control, production_plan

app = FastAPI(title="YouCai API", version="1.0.0")

# 允许跨域请求
origins = [
    
    "http://localhost:3000",  # Vite开发服务器默认端口
    "http://139.129.109.178:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(irrigation.router, prefix="/api/irrigation", tags=["灌溉管理"])
app.include_router(fertilizer.router, prefix="/api", tags=["施肥推荐"])
app.include_router(harvest_preparation.router, prefix="/api", tags=["收获准备"])
app.include_router(pest_control.router, prefix="/api", tags=["病虫害防控"])
app.include_router(production_plan.router, prefix="/api", tags=["生产计划"])

# 根路由
@app.get("/")
def read_root():
    return {"message": "欢迎使用YouCai API！"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)