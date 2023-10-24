package com.api.apiinterface.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.api.apiclientsdk.model.SourceText;
import com.api.apiclientsdk.model.User;
import com.api.apiclientsdk.utils.SignUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */

@RestController
@RequestMapping("/invoke")
public class InvokeController {

    @GetMapping("/common/OneDayEnglish")
    public Map<String, Object> getOneDayEnglish(HttpServletRequest request) {
        HttpResponse response = HttpRequest.get("https://api.oioweb.cn/api/common/OneDayEnglish")
                .charset(StandardCharsets.UTF_8)
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
        try {
            resultMap = gson.fromJson(body, Map.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
        return (Map<String, Object>)resultMap.get("result");
    }

    @GetMapping("/ip/ipaddress")
    public Map<String, Object> getIpAdr(HttpServletRequest request, @RequestParam("ip") String ip) {
        HttpResponse response = HttpRequest.get("https://api.oioweb.cn/api/ip/ipaddress?ip=" + ip)
                .charset(StandardCharsets.UTF_8)
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
        try {
            resultMap = gson.fromJson(body, Map.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
        return (Map<String, Object>)resultMap.get("result");
    }

    @GetMapping("/common/history")
    public Object getHistory(HttpServletRequest request) {
        HttpResponse response = HttpRequest.get("https://api.oioweb.cn/api/common/history")
                .charset(StandardCharsets.UTF_8)
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());

        return responseMap.get("result");
    }

    @GetMapping("/bing")
    public Object getBingDailyPic(HttpServletRequest request) {
        HttpResponse response = HttpRequest.get("https://api.oioweb.cn/api/bing")
                .charset(StandardCharsets.UTF_8)
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());

        return responseMap.get("result");
    }

    @GetMapping("/common/teladress")
    public Map<String, Object> getTeladress(HttpServletRequest request, @RequestParam("mobile") String mobile) {
        HttpResponse response = HttpRequest.get("https://api.oioweb.cn/api/common/teladress?mobile=" + mobile)
                .charset(StandardCharsets.UTF_8)
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
        try {
            resultMap = gson.fromJson(body, Map.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
        return (Map<String, Object>)resultMap.get("result");
    }

    @GetMapping("/txt/QQFanyi")
    public Map<String, Object> QQTranslation(@RequestParam("sourceText") String sourceText, HttpServletRequest request) {
        HttpResponse response = HttpRequest.get("https://api.oioweb.cn/api/txt/QQFanyi?sourceText=" + sourceText)
                .charset(StandardCharsets.UTF_8)
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
        try {
            resultMap = gson.fromJson(body, Map.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
        return (Map<String, Object>)resultMap.get("result");
    }


}
