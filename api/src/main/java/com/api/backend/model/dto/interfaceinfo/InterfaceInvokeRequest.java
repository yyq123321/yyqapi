package com.api.backend.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Data
public class InterfaceInvokeRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

    private List<Field> requestParams;
    private String userRequestParams;

    @Data
    public static class Field {
        private String fieldName;
        private String value;
    }


    private static final long serialVersionUID = 1L;
}
