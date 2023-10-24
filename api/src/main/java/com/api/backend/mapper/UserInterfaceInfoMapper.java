package com.api.backend.mapper;

import com.api.backend.model.vo.UserInterfaceInfoAnalysisVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.api.apicommon.model.entity.UserInterfaceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

//    List<UserInterfaceInfoAnalysisVO> listTopInterfaceInfo(@Param("size") int size);

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}




