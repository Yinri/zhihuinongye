package org.jeecg.modules.youcai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("youcai_insect_trend_suggest")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "基地虫情趋势与防治建议")
public class YoucaiInsectTrendSuggest extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private String baseId;

    @Excel(name = "基地名称", width = 15)
    @Schema(description = "基地名称")
    private String baseName;

    @Excel(name = "虫情日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "虫情日期")
    private Date analysisDate;

    @Excel(name = "分析时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "分析时间")
    private Date analysisTime;

    @Excel(name = "最新虫情图片", width = 30)
    @Schema(description = "最新虫情图片")
    private String imageUrl;

    @Excel(name = "虫子名称和数量", width = 30)
    @Schema(description = "虫子名称和数量")
    private String insectSummary;

    @Schema(description = "害虫趋势分析")
    private String trendAnalysis;

    @Schema(description = "防治建议")
    private String controlSuggestion;

    @Schema(description = "原始虫情JSON")
    private String rawInsectJson;

    @Schema(description = "原始图片JSON")
    private String rawImageJson;

    @Excel(name = "生成状态", width = 15)
    @Schema(description = "生成状态：SUCCESS/FAILED/FALLBACK")
    private String status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "所属部门")
    private String sysOrgCode;

    @Excel(name = "删除标志", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private Integer delFlag;
}
