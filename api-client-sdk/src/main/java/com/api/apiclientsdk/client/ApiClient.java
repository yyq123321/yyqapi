package com.api.apiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.api.apiclientsdk.model.IpAdr;
import com.api.apiclientsdk.model.Mobile;
import com.api.apiclientsdk.model.SourceText;
import com.api.apiclientsdk.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.api.apiclientsdk.utils.SignUtil.getSign;


/**
 * @description: 调用第三方SDK
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public class ApiClient {

    private static final String GATEWAY_HOST = "http://127.0.0.1:8090";

    private String accessKey;
    private String secretKey;

    public ApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    private Map<String, String> getHeaderMap(String body) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accessKey", accessKey);
        //一定不能直接发送
//        hashMap.put("secretKey", secretKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(4));
        hashMap.put("body", body);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        hashMap.put("sign", getSign(body, secretKey));
        return hashMap;
    }


    public String getOneDayEnglish() {
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/interfaceInvoke/invoke/common/OneDayEnglish")
                .charset(StandardCharsets.UTF_8)
                .addHeaders(getHeaderMap("user"))
                .execute();
        System.out.println(response.getStatus());
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> resultMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());

        String resultString = gson.toJson(resultMap);

        String result = resultString.toString();
        return result;
    }

    public String getIpAddress(IpAdr ip) {
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/interfaceInvoke/invoke/ip/ipaddress?ip=" + ip.getIp())
                .charset(StandardCharsets.UTF_8)
                .addHeaders(getHeaderMap("user"))
                .execute();
        System.out.println(response.getStatus());
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> resultMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());


        String resultString = gson.toJson(resultMap);

        String result = resultString.toString();
        return result;
    }


    public String getHistory() {
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/interfaceInvoke/invoke/common/history")
                .charset(StandardCharsets.UTF_8)
                .addHeaders(getHeaderMap("user"))
                .execute();
        System.out.println(response.getStatus());
        String body = response.body();
        Gson gson = new Gson();
        List<Map<String, Object>> resultMap = gson.fromJson(body, new TypeToken<List<Map<String, Object>>>() {}.getType());

        String resultString = gson.toJson(resultMap);

        String result = resultString.toString();
        return result;
    }

    public String getBingDailyPic() {
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/interfaceInvoke/invoke/bing")
                .charset(StandardCharsets.UTF_8)
                .addHeaders(getHeaderMap("user"))
                .execute();
        System.out.println(response.getStatus());
        String body = response.body();
        Gson gson = new Gson();
        List<Map<String, Object>> resultMap = gson.fromJson(body, new TypeToken<List<Map<String, Object>>>() {}.getType());

        String resultString = gson.toJson(resultMap);

        String result = resultString.toString();
        return result;
    }

    public String getTelAddress(Mobile mobile) {
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/interfaceInvoke/invoke/common/teladress?mobile=" + mobile.getMobile())
                .charset(StandardCharsets.UTF_8)
                .addHeaders(getHeaderMap("user"))
                .execute();
        System.out.println(response.getStatus());
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> resultMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());


        String resultString = gson.toJson(resultMap);

        String result = resultString.toString();
        return result;
    }

    public String QQTranslation(SourceText sourceText) {
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/interfaceInvoke/invoke/txt/QQFanyi?sourceText=" +
                         sourceText.getSourceText())
                .charset(StandardCharsets.UTF_8)
                .addHeaders(getHeaderMap("user"))
                .execute();
        System.out.println(response.getStatus());
        String body = response.body();
        Gson gson = new Gson();
        Map<String, Object> resultMap = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());

        String resultString = gson.toJson(resultMap);

        String result = resultString.toString();
        return result;
    }





}
