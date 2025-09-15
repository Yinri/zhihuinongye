""" 
 灌溉预测模型模块 
 包含基于Penman-Monteith公式的灌溉量计算算法 
 """ 
import math 
 
 
def estimate_solar_radiation(temp): 
     """ 
     根据温度估算太阳辐射 (MJ/m²/day) 
     简化估算方法：Ra = 15 + 0.2 * T_avg 
     """ 
     return max( 5, 15 + 0.2 * temp)  # 确保不低于5 MJ/m²/day 
 
 
def calculate_et0_penman_monteith(temp , humidity , wind_speed , solar_radiation): 
     """ 
     使用Penman-Monteith公式计算参考蒸散量(ET₀) 
     参数: 
     temp: 平均气温 (°C) 
     humidity: 相对湿度 (%) 
     wind_speed: 风速 (m/s) 
     solar_radiation: 太阳辐射 (MJ/m²/day) 
 
     返回: ET₀ (mm/day) 
     """ 
     # 常量 
     DELTA = 4098 * ( 0.6108 * math.exp(( 17.27 * temp) / (temp + 237.3 ))) / ((temp + 237.3 ) ** 2 )  # 饱和水汽压曲线斜率 
     GAMMA = 0.067  # 干湿表常数 (kPa/°C) 
     LAMBDA = 2.45  # 蒸发潜热 (MJ/kg) 
 
     # 饱和水汽压 (kPa) 
     es = 0.6108 * math.exp(( 17.27 * temp) / (temp + 237.3 )) 
 
     # 实际水汽压 (kPa) 
     ea = (humidity / 100 ) * es 
 
     # 净辐射 (近似) 
     Rn = solar_radiation * 0.8 - 0.5 * (temp / 20 )  # 简化计算 
 
     # 土壤热通量 (白天近似为0) 
     G = 0 
 
     # 风速函数 (假设在2米高度测量) 
     u2 = wind_speed 
 
     # Penman-Monteith公式 
     et0 = ( 0.408 * DELTA * (Rn - G) + 
            GAMMA * ( 900 / (temp + 273 )) * u2 * (es - ea)) /(DELTA + GAMMA * (1 + 0.34 * u2))
 
     return max( 0, et0)  # 确保不为负值 
 
 
def calculate_improved_irrigation(historical_data , latest_data): 
     """ 
     改进的灌溉量计算算法 
     """ 
     # 1. 计算参考蒸散量(ET₀) 
     et0 = calculate_et0_penman_monteith( 
         latest_data['avg_temp'] , 
         latest_data['humidity'] , 
         latest_data['wind_speed'] , 
         latest_data['solar_radiation'] 
     ) 
 
     # 2. 根据作物生长阶段确定作物系数(Kc) 
     # 蕾薹期作物系数 
     kc = 1.15 
 
     # 3. 计算作物需水量(ETc) 
     etc = kc * et0 
 
     # 4. 考虑有效降雨量 
     effective_rainfall = latest_data['rainfall'] * 0.8  # 降雨有效利用系数 
 
     # 5. 计算净灌溉需水量 
     net_irrigation = etc - effective_rainfall 
 
     # 6. 考虑土壤水分状况 
     target_soil_moisture = 0.70  # 蕾薹期目标土壤湿度 
     current_soil_moisture = latest_data['soil_moisture'] 
 
     # 土壤水分补偿量 (假设根系深度为0.5m，土壤容重为1.3g/cm³) 
     soil_compensation = (target_soil_moisture - current_soil_moisture) * 0.5 * 1300  # mm 
 
     # 7. 计算总灌溉需水量 
     total_irrigation = max( 0, net_irrigation + soil_compensation) 
 
     # 8. 考虑灌溉效率 (假设为0.85) 
     irrigation_efficiency = 0.85 
     actual_irrigation = total_irrigation / irrigation_efficiency 
 
     # 9. 根据NDVI趋势调整 
     if len(historical_data) >= 5 : 
         # 计算最近5天的NDVI趋势 
         recent_ndvi = [d['ndvi'] for d in historical_data[- 5 :]] 
         ndvi_trend = (recent_ndvi[- 1 ] - recent_ndvi[ 0 ]) / 4  # 每天变化率 
 
         # 如果NDVI下降，增加灌溉量 
         if ndvi_trend < 0 : 
             actual_irrigation *= ( 1 - ndvi_trend * 10 )  # 根据下降幅度调整 
 
     return round(actual_irrigation , 1 )