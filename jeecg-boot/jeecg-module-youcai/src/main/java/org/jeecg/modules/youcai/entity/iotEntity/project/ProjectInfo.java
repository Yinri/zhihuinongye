package org.jeecg.modules.youcai.entity.iotEntity.project;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目列表数据模型
 */
@Data
public class ProjectInfo {
    private Project q; // 项目基本信息
    private ProjectAdmin s; // 项目管理员信息

    /**
     * 项目基本信息
     */
    @Data
    public static class Project {
        private Integer id; // 项目ID（关键参数，后续接口需用）
        private String projectName; // 项目名称
        private String projectCode; // 项目编号
        private String projectDesc; // 项目描述
        private String projectAdress; // 项目地址
        private Integer isDelete; // 是否删除：0=未删除，1=已删除
        private String lat; // 经度（WGS84坐标系）
        private String lng; // 纬度（WGS84坐标系）
        private Integer userId; // 项目管理员ID
        private LocalDateTime dateCreated; // 创建时间
    }

    /**
     * 项目管理员信息
     */
    @Data
    public static class ProjectAdmin {
        private Integer id; // 用户ID
        private String userName; // 管理员账号
        private String fullName; // 管理员姓名
        private String sex; // 性别
        private String phone; // 手机号
        private Integer roleId; // 角色ID
        private LocalDateTime dateCreated; // 创建时间
    }
}