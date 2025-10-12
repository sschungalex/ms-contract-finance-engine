package com.windsurf.contractengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 时间要素 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeElementsDto {

    /**
     * 服务周期
     */
    @JsonProperty("servicePeriod")
    private ServicePeriodDto servicePeriod;

    /**
     * 交付节点列表
     */
    @JsonProperty("deliveryNodes")
    private List<DeliveryNodeDto> deliveryNodes;

    /**
     * 服务周期 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicePeriodDto {
        
        /**
         * 服务周期类型
         */
        @JsonProperty("type")
        private String type;

        /**
         * 持续时长
         */
        @JsonProperty("duration")
        private Integer duration;

        /**
         * 描述信息
         */
        @JsonProperty("description")
        private String description;
    }

    /**
     * 交付节点 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryNodeDto {
        
        /**
         * 里程碑名称
         */
        @JsonProperty("milestone")
        private String milestone;

        /**
         * 交付百分比
         */
        @JsonProperty("percentage")
        private Integer percentage;

        /**
         * 预期交付日期
         */
        @JsonProperty("dueDate")
        private String dueDate;
    }
}
