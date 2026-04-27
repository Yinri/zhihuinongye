package org.jeecg.modules.youcai.dto.decision;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class YoucaiDecisionPestDTO {

    private String sid;

    private String nam;

    private List<PestItem> bug = new ArrayList<>();

    private String sgt;

    @Data
    public static class PestItem {
        private String name;
        private Integer value;
    }
}
