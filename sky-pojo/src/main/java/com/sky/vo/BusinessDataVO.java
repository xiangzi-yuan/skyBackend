package com.sky.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sky.json.TwoDecimalSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class BusinessDataVO implements Serializable {
    @JsonSerialize(using = TwoDecimalSerializer.class)
    private Double turnover;

    private Integer validOrderCount;

    @JsonSerialize(using = TwoDecimalSerializer.class)
    private Double orderCompletionRate;

    @JsonSerialize(using = TwoDecimalSerializer.class)
    private Double unitPrice;

    private Integer newUsers;
}
