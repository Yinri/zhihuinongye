-- 假设基地表名为 youcai_bases（根据实际表名修改）
ALTER TABLE youcai_plots
    ADD COLUMN growth_stage VARCHAR(20)
    COMMENT '生长阶段（枚举值：未播种、已播种、苗期、蕾薹期、开花期、角果成熟期、收获与整地）'
DEFAULT '未播种'; -- 默认值设为最初始状态，避免空值
-- 1. 先查询重复数据（确认重复情况，可选但建议执行）
SELECT variety_name, COUNT(*) AS duplicate_count, GROUP_CONCAT(id) AS duplicate_ids
FROM youcai_rape_varieties
GROUP BY variety_name
HAVING COUNT(*) > 1;

-- 2. 安全删除重复数据（保留每个品种 ID 最小的一条，规避 MySQL 子查询限制）
DELETE t1 FROM youcai_rape_varieties t1
                   JOIN (
    -- 子查询包装一层，避免直接引用目标表
    SELECT MIN(id) AS min_id, variety_name
    FROM youcai_rape_varieties
    GROUP BY variety_name
    HAVING COUNT(*) > 1
) t2 ON t1.variety_name = t2.variety_name AND t1.id != t2.min_id;

-- 3. （可选）设置字段非空（避免多个 NULL 不冲突的情况）
ALTER TABLE youcai_rape_varieties
    MODIFY COLUMN variety_name VARCHAR(255) NOT NULL; -- 按实际字段类型/长度调整

-- 4. 添加唯一约束
ALTER TABLE youcai_rape_varieties
    ADD CONSTRAINT uk_youcai_variety_name UNIQUE (variety_name);