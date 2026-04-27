package org.jeecg.modules.youcai.dto.decision;

import lombok.Data;

@Data
public class YoucaiDecisionSoilDTO {

    private String sid;

    private String nam;

    private Metric wtr;

    private Metric ecz;

    @Data
    public static class Metric {
        private String nmb;
        private String lvl;
        private String sgt;
    }
}
