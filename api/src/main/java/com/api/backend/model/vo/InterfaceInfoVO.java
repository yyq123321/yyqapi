package com.api.backend.model.vo;

import com.api.apicommon.model.entity.InterfaceInfo;
import lombok.Data;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Data
public class InterfaceInfoVO extends InterfaceInfo {
    /**
     * 统计每个接口被用户调用的总数
     */
    private Integer totalNum;

    private String name;

    /**
     * 计费规则（元/条）
     */
    private Double charging;

    /**
     * 计费Id
     */
    private Long chargingId;

    /**
     * 接口剩余可调用次数
     */
    private String availablePieces;

    private Long price;
}
