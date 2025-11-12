package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 项目信息表
 * @Author: jeecg-boot
 * @Date:   2025-11-10
 * @Version: V1.0
 */
@Data
@TableName("youcai_project_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="项目信息表")
public class YoucaiProjectInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
    
    /**项目ID（从API获取的原始ID）*/
    @Excel(name = "项目ID", width = 15)
    @Schema(description = "项目ID（从API获取的原始ID）")
    private Integer projectId;
    
    /**项目名称*/
    @Excel(name = "项目名称", width = 15)
    @Schema(description = "项目名称")
    private String projectName;
    
    /**项目编号*/
    @Excel(name = "项目编号", width = 15)
    @Schema(description = "项目编号")
    private String projectCode;
    
    /**项目描述*/
    @Excel(name = "项目描述", width = 30)
    @Schema(description = "项目描述")
    private String projectDesc;
    
    /**项目地址*/
    @Excel(name = "项目地址", width = 30)
    @Schema(description = "项目地址")
    private String projectAddress;
    
    /**经度（WGS84坐标系）*/
    @Excel(name = "经度", width = 15)
    @Schema(description = "经度（WGS84坐标系）")
    private String longitude;
    
    /**纬度（WGS84坐标系）*/
    @Excel(name = "纬度", width = 15)
    @Schema(description = "纬度（WGS84坐标系）")
    private String latitude;
    
    /**项目管理员ID*/
    @Excel(name = "项目管理员ID", width = 15)
    @Schema(description = "项目管理员ID")
    private Integer adminId;
    
    /**项目管理员账号*/
    @Excel(name = "管理员账号", width = 15)
    @Schema(description = "项目管理员账号")
    private String adminUserName;
    
    /**项目管理员姓名*/
    @Excel(name = "管理员姓名", width = 15)
    @Schema(description = "项目管理员姓名")
    private String adminFullName;
    
    /**管理员手机号*/
    @Excel(name = "管理员手机号", width = 15)
    @Schema(description = "管理员手机号")
    private String adminPhone;
    
    /**是否删除：0=未删除，1=已删除*/
    @Excel(name = "是否删除", width = 15, dicCode = "yn")
    @Schema(description = "是否删除：0=未删除，1=已删除")
    private Integer isDelete;
    
    /**项目创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name = "项目创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "项目创建时间")
    private LocalDateTime projectCreateTime;
    
    /**数据同步时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name = "数据同步时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据同步时间")
    private LocalDateTime syncTime;
    
    /**创建人*/
    @Schema(description = "创建人")
    private String createBy;
    
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private LocalDateTime createTime;
    
    /**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
    
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private LocalDateTime updateTime;
    
    /**所属部门*/
    @Schema(description = "所属部门")
    private String sysOrgCode;
}