package org.jeecg.modules.youcai.dto.decision;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class YoucaiDecisionHeightRiskDTO {

    private String sid;

    private String nam;

    private List<TrendItem> dta = new ArrayList<>();

    @Data
    public static class TrendItem {
        private String dat;
        private String hgt;
        private String low;
        private String hig;
    }
}
