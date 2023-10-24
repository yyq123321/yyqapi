package com.api.backend.controller;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.api.apicommon.common.BaseResponse;
import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.common.ResultUtils;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.model.entity.InterfaceInfo;
import com.api.apicommon.model.entity.UserInterfaceInfo;
import com.api.backend.annotation.AuthCheck;
import com.api.backend.mapper.UserInterfaceInfoMapper;
import com.api.backend.model.vo.InterfaceInfoVO;
import com.api.backend.model.vo.UserInterfaceInfoAnalysisVO;
import com.api.backend.service.InterfaceInfoService;
import com.api.backend.service.UserInterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

//    @DubboReference
//    private InnerOrderService innerOrderService;


    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVO>> listTopInterfaceInfo(){
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        // todo 优化 防止空值报错
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(interfaceInfoVOList);

    }
    
    /**
    @GetMapping("/top/interface/invoke/excel")
    @AuthCheck(mustRole = "admin")
    public void topInvokeInterfaceInfoExcel(HttpServletResponse response) throws IOException {

        List<InterfaceInfoVO> interfaceInfoVOList = userInterfaceInfoService.interfaceInvokeTopAnalysis(100);
        List<InterfaceInfoInvokeExcel> collect = interfaceInfoVOList.stream().map(interfaceInfoVO -> {
            InterfaceInfoInvokeExcel interfaceInfoExcel = new InterfaceInfoInvokeExcel();
            BeanUtils.copyProperties(interfaceInfoVO, interfaceInfoExcel);
            return interfaceInfoExcel;
        }).sorted((a,b)-> b.getTotalNum() - a.getTotalNum()).collect(Collectors.toList());

        String fileName = "interface_invoke.xlsx";
        genExcel(response,fileName, InterfaceInfoInvokeExcel.class,collect);
    }



    @GetMapping("/top/interface/buy")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<OrderVO>> listTopBuyInterfaceInfo() {
        List<OrderVO> orderVOList = interfaceBuyTopAnalysis();
        return ResultUtils.success(orderVOList);
    }

    private List<OrderVO> interfaceBuyTopAnalysis() {
        List<Order> orderList = innerOrderService.listTopBuyInterfaceInfo(5);
        List<OrderVO> orderVOList = orderList.stream().map(order -> {
            Long interfaceId = order.getInterfaceId();
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceId);
            OrderVO orderVO = new OrderVO();
            orderVO.setInterfaceId(interfaceId);
            orderVO.setTotal(order.getCount().longValue());
            orderVO.setInterfaceName(interfaceInfo.getName());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            return orderVO;
        }).collect(Collectors.toList());
        return orderVOList;
    }

    @GetMapping("/top/interface/buy/excel")
    @AuthCheck(mustRole = "admin")
    public void topBuyInterfaceInfoExcel(HttpServletResponse response) throws IOException {
        List<OrderVO> orderVOList = interfaceBuyTopAnalysis();
        List<InterfaceInfoOrderExcel> collect = orderVOList.stream().map(orderVO -> {
            InterfaceInfoOrderExcel interfaceInfoOrderExcel = new InterfaceInfoOrderExcel();
            BeanUtils.copyProperties(orderVO, interfaceInfoOrderExcel);
            return interfaceInfoOrderExcel;
        }).sorted((a, b) -> (int) (b.getTotal() - a.getTotal())).collect(Collectors.toList());
        String fileName = "interface_buy.xlsx";

        genExcel(response,fileName,InterfaceInfoOrderExcel.class,collect);
    }



    private void genExcel(HttpServletResponse response,String fileName,Class entity, List collect) throws IOException {

        String sheetName = "analysis";
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        // 创建ExcelWriter对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), entity).build();
        // 创建工作表
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

        // 写入数据到工作表
        excelWriter.write(collect, writeSheet);

        // 关闭ExcelWriter对象
        excelWriter.finish();
    }
    */
}
