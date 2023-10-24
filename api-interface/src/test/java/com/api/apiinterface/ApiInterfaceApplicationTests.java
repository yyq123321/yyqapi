package com.api.apiinterface;

import com.api.apiclientsdk.client.ApiClient;
import com.api.apiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@SpringBootTest
class ApiInterfaceApplicationTests {

    @Resource
    private ApiClient apiClient;

    @Test
    void contextLoads() {
        String api = apiClient.getNameBtGet("api");
        User user = new User();
        user.setUserName("api");
        String nameBtPost = apiClient.getUserNameByPost(user);
        System.out.println(nameBtPost);
    }

}
