package com.api.backend.model.vo;

import com.api.apicommon.model.entity.UserInterfaceInfo;
import lombok.Data;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Data
public class UserInterfaceInfoAnalysisVO extends UserInterfaceInfo {

    /**
     * 统计每个接口被用户调用的总数
     */
    private Integer sumNum;
}
