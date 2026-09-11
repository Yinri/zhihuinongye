package org.jeecg.modules.youcai.dto.harvest;

import lombok.Data;

@Data
public class HarvesterStatusDTO {
    private String name;     // 农机名称
    private String status;   // working / idle / maintenance
    private String location; // 最近作业或停放位置
}