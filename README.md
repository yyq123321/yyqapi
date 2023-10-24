# YYQ-API接口

> 一个丰富的API开放调用平台，为开发者提供便捷、实用、安全的API调用；全栈项目，包括网站前台+后台
>
>
> 
>
> 在线体验地址：[YYQ-API接口](http://yyq-api.yyq-personal-code.cn/)




## 项目背景

&emsp;&emsp;我的初衷是尽可能帮助和服务更多的用户和开发者，让他们更加方便快捷的获取他们想要的信息和功能。
接口平台可以帮助开发者快速接入一些常用的服务



## 技术栈

### 前端技术栈

- 开发框架：React、Umi
- 脚手架：Ant Design Pro
- 组件库：Ant Design、Ant Design Components
- 语法扩展：TypeScript、Less
- 打包工具：Webpack
- 代码规范：ESLint、StyleLint、Prettier



### 后端技术栈

- 主语言：Java
- 框架：SpringBoot 2.7.0、Mybatis-plus、Spring Cloud
- 数据库：Mysql8.0、Redis
- 中间件：RabbitMq
- 注册中心：Nacos
- 服务调用：Dubbo
- 网关：Spring Cloud Gateway
- 负载均衡：Spring cloud Loadbalancer



## 项目模块

- api-frontend ：为项目前端，前端项目启动具体看readme.md文档
- api-common ：为公共封装类（如公共实体、公共常量，统一响应实体，统一异常处理）
- api-backend ：为接口管理平台，主要包括用户、接口相关的功能
- api-gateway ：为网关服务，**涉及到网关限流，统一鉴权，统一日志处理，接口统计，接口数据一致性处理**
- api-global-gateway ：为全局网关服务，**涉及到网关限流，请求转发**
- api-order ：为订单服务，主要涉及到货币的购买等
- api-interface：为接口服务，提供可供调用的接口
- api-sdk：提供给开发者的SDK



## 欢迎贡献
限于技术水平和服务器瓶颈，许多idea和实现中的模块还未上线。
项目需要大家的支持，期待更多小伙伴的贡献，你可以：

- 对于项目中的Bug和建议，能够在Issues区提出建议，我会积极响应
- 对于熟悉CICD并且实现过SDK自动化构建、测试及集成的小伙伴，欢迎联系我，感激不尽


## 快速上手

### 后端

1. 将各模块配置修改成你自己本地的端口、账号、密码
2. 启动Nacos、Mysql、Redis、RabbitMq
3. 将公共服务 api-common 以及客户端 SDK 安装到本地仓库
4. 按顺序启动服务

服务启动顺序参考：
1. api-backend
2. api-order
3. api-gateway
4. api-interface
5. api-global-gateway


### 前端

环境要求：Node.js >= 16

安装依赖:

```
yarn
```

启动:

```
npm run start:dev
```

### 后端
nacos注册中心:

```
启动命令(standalone代表着单机模式运行，非集群模式):

startup.cmd -m standalone

linux:
sh startup.sh -m standalone
```

rabbitmq:

```
rabbitmqctl status	//查看当前状态
rabbitmq-plugins enable rabbitmq_management	//开启Web插件
rabbitmq-server start	//启动服务
rabbitmq-server stop	//停止服务
rabbitmq-server restart	//重启服务
```

**注意：如果想要体验订单和支付业务并且没有个人云服务器的，需要配置内网穿透才能体验(非必要)**


后台运行java

```
nohup java -jar babyshark-0.0.1-SNAPSHOT.jar  > log.file  2>&1 &
```









