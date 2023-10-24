package com.api.backend.model.dto.interfaceinfo;

import com.api.apicommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InterfaceInfoSearchTextRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -6337349622479990038L;

    private String searchText;
}
