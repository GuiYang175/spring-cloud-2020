# 前言

## 微服务模块基本公式

1.  建 Module
2.  改 pom
3.  写 yml
4.  主启动
5.  业务类

## 学习技术的4个维度

1. 是什么
2. 能干什么
3. 去哪下
4. 怎么玩

## 写在前面

* 很多服务的yml中都需要配置服务到注册中心里，默认是注册进Eureka。第一章的服务端yml里的向Eureka注册的配置基本都涵盖了，所以之后的yml与导入Eureka的pom依赖中都省略了。
* pom文件中基本上都需要加入web和actuator的依赖，所以都省略了（除了gateway使用的webflux不需要加）。

## 免责声明

​		以下内容均为本人学习[尚硅谷的spring cloud视频](https://www.bilibili.com/video/BV18E411x7eT)所学内容，整理了自己觉得重要的部分。仅供本人学习使用，侵删,联系方式1641074950@qq.com



# 一、Eureka（停更）

## 1、Eureka工作示意图



![Eureka工作示意图](https://github.com/guiyang175/spring-cloud-2020/raw/master/image/Eureka.png)
*Eureka Server功能 :*

* **服务注册**： 将服务信息注册进注册中心
* **服务发现**：从注册中心获取服务信息
* **实质**： 存key  服务名 ， 取value 调用地址



## 2、微服务RPC[^1]远程服务调用的核心

​		***高可用***  ：如果注册中心只有一个，他出故障后会导致整个微服务环境不可用。所以需要搭建Eureka注册中心集群，实现负载均衡加故障容错。

[^1]:全称Remote Procedure Call，:远程过程调用,它是一种通过网络从远程计算机程序上请求服务,而不需要了解底层网络技术的思想。



## 3、Eureka集群的注册原理

​		互相注册，相互守望

![注册原理示意图](https://github.com/guiyang175/spring-cloud-2020/raw/master/image/Eureka注册原理.png)

## 4、 Eureka工作流程（以项目demo举例）

1. 先启动Eureka注册中心
2. 启动服务提供者payment支付服务
3. 支付服务启动后会把自身信息（比如服务地址）以别名的方式t注册进Eureka
4. 消费者order服务在需要调用接口时，使用服务别名取注册中心获取实际的RPC远程调用地址
5. 消费者获得调用地址后，底层实际利用HttpClient技术实现远程调用
6. 消费者获得服务地址后，会缓存在本地jvm内存中，默认每间隔30秒更新一次服务调用地址



## 5、Eureka实操关键内容

### 	Ⅰ Eureka注册中心

#### 1）pom

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>		<!-- 可视化管理需要actuator ,一般web+actuator组合,但也有例外，比如含有webflux的依赖就不能导入web,以下都一样 就不重复写这个两个依赖了-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

####  			2）yml

```yaml
server:
  port: 7001

eureka:
  instance:
    hostname: eureka7001.com  #需要在C:\Windows\System32\driver\etc\hosts文件中做映射 127.0.0.1 eureka7001.com
    
  client:
    fetch-registry: false #false 表示自己端就是注册中心，职责就是维护服务实例，并不需要去检索服务
    register-with-eureka: false # false表示不向注册中心注册自己
    service-url:
      defaultZone: http://eureka7002.com:7002/eureka/     #设置与Eureka server交互的地址查询服务和注册服务都需要依赖这个地址(集群，若想再加入一个eureka，用逗号连接)

#  #配置eureka是否进入绝情模式(接受不到服务心跳时，立即删除)
#  server:
#    enable-self-preservation: false #禁用自我保护机制（自我保护:微服务掉线或网络堵塞时，也不会删除该服务信息（好死不如赖活着））
#    eviction-interval-timer-in-ms: 2000
```

#### 			3）主启动

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaMain7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7001.class, args);
    }
}
```

-----

### 	Ⅱ Provider提供服务方

#### 			1）pom

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
```

#### 			2）yml

```yaml
server:
  port: 8001
spring:
  application:
    name: cloud-payment-service
eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetch-registry: true
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/ #集群版
      #defaultZone: http://localhost:7001/eureka #单机版（单个eureka）
  instance:
    instance-id: payment8001 #服务名称
    prefer-ip-address: true #在eureka界面，鼠标悬停在该服务的status时，页面左下角显示ip
#    #eureka客户端向服务端发送心跳的时间间隔，单位为秒（默认30秒）
#    lease-renewal-interval-in-seconds: 1
#    #eureka服务端在收到最后一次心跳后等待时间上限，单位为秒（默认是90秒），超时将剔除服务
#    lease-expiration-duration-in-seconds: 2
```

#### 			3）主启动

```java
@SpringBootApplication
@EnableEurekaClient //@EnableEurekaClient 为Eureka注册中心
@EnableDiscoveryClient //@EnableDiscoveryClient 可以是其他注册中心
public class PaymentMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8001.class, args);
    }
}
```

#### 			4）业务类

```java
@RestController
@Slf4j
public class PaymentController {
    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private String servicePort;

    @PostMapping(value = "/payment/create")
    public CommonResult create(@RequestBody Payment payment) {
        int result = paymentService.create(payment);
        if (result > 0) {
            return new CommonResult(200, "插入数据库成功,端口号为：" + servicePort, result);
        } else {
            return new CommonResult(444, "插入数据库失败", null);
        }
    }

    @GetMapping(value = "/payment/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id) {
        Payment payment = paymentService.getPaymentById(id);
        if (payment != null) {
            return new CommonResult(200, "查询成功,端口号为：" + servicePort, payment);
        } else {
            return new CommonResult(444, "没有对应记录，查询ID：" + id, null);
        }
    }
}
```

------

### Ⅲ Consumer客户端

#### 1）pom

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>		
```

#### 2）yml

```yaml
server:
  port: 80
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/ 
```

#### 3）主启动

```java
@SpringBootApplication
@EnableEurekaClient
public class OrderMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class, args);
    }
}
```

#### 4）业务类

* controller

  ```java
  @RestController
  @Slf4j
  public class OrderController {
      /**
       * 需要请求的服务的服务名
       */
      public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";
  
      /**
       * 基于Rest规范提供Http请求的工具，须在configure里配置
       */
      @Resource
      private RestTemplate restTemplate;
  
      @GetMapping("consumer/payment/create")
      public CommonResult<Payment> create(Payment payment){
          return restTemplate.postForObject(PAYMENT_URL+"/payment/create",payment,CommonResult.class);
      }
  
      @GetMapping("consumer/payment/get/{id}")
      public CommonResult<Payment> getPaymentById(@PathVariable("id")Long id){
          return restTemplate.getForObject(PAYMENT_URL+"/payment/get/"+id,CommonResult.class);
      }
  
      /**
       * 需要更多详细的信息时使用getForEntity
       *
       * @param id
       * @return
       */
      @GetMapping("/consumer/payment/getForEntity/{id}")
      public CommonResult<Payment> getPayment2(@PathVariable("id")Long id){
          ResponseEntity<CommonResult> entity = restTemplate.getForEntity(PAYMENT_URL+"/payment/get/"+id,CommonResult.class);
          if(entity.getStatusCode().is2xxSuccessful()){
              return entity.getBody();
          }else{
              return new CommonResult<>(444,"操作失败");
          }
      }
  }
  ```

* configure

  ```java
  @Configuration
  public class ApplicationContextConfig {
  
      @Bean
      @LoadBalanced
      public RestTemplate getRestTemplate(){
          //@LoadBalanced 负载均衡机制(默认轮询的负载机制)
          return new RestTemplate();
      }
  }
  ```

  

## 6、Eureka停更后的几个替换方案

### Ⅰ Zookeeper

#### 1）provider提供服务方

##### ① pom

```xml
<!--        自带了一个3.5.3版本的，但安装的是3.4.9，所以此处需要排除自带的zookeeper-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.apache.zookeeper</groupId>
                    <artifactId>zookeeper</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
<!--        此处添加3.4.9版本-->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.4.9</version>
        </dependency>
```

##### ② yml

```yaml
server:
  port: 8004
spring:
  application:
    name: cloud-provider-payment
  cloud:
    zookeeper:
      connect-string: localhost:2181 # zookeeper所在地址 ip:端口(默认2181)
```

##### ③ 主启动

```java
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentMain8004 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8004.class, args);
    }
}
```

##### ④ 业务类

```java
@RestController
@Slf4j
public class PaymentController {
    @Value("${server.port}")
    private String serverPort;

    @GetMapping(value = "/payment/zk")
    public String paymentZk(){
        return "SpringCloud with Zookeeper:" +serverPort+"\t"+ UUID.randomUUID().toString();
    }
}
```

#### 2）consumer客户端

​	业务类与eureka类似，pom、yml与、主启动与zookeeper的服务端一致，需要修改的为yml中的端口号与容器名

#### 3）附

* 在docker中安装的zookeeper，要启动zookeeper客户端， 用如下命令 ***docker exec -it 容器id zkCli.sh***
* zookeeper注册中心时单独运行的,暂不用配置（集群还没有配过）

------

### Ⅱ Consul

### 1）provider提供服务方

#### ①pom

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>
```

#### ②yml

```yaml
server:
  port: 8006
spring:
  application:
    name: consul-provider-payment
  cloud:
    consul:
      host: localhost
      port: 8500 #默认8500端口
      discovery:
        service-name: ${spring.application.name}
```

#### ③主启动

```java
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentMain8006 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8006.class,args);
    }
}
```

#### ④业务类

```java
@RestController
@Slf4j
public class PaymentController {
    @Value("${server.port}")
    private String serverPort;

    @GetMapping(value = "/payment/consul")
    public String paymentConsul(){
        return "springcloud with consul:"+serverPort+"\t"+ UUID.randomUUID().toString();
    }
}
```

### 2）Comsumer客户端

​	业务类与eureka类似，pom、yml与、主启动与consul的服务端一致，需要修改的为yml中的端口号与容器名

### 3）附

* consul安装在windows操作系统中，在官网下载后需要配置环境变量。下载的文件解压后只有一个文件，在此目录下打开cmd，输入consul agent -dev运行
* 通过<http://localhost:8500>访问客户端

### Ⅲ Nacos

----



# 二、Ribbon（维护模式） 负载均衡服务调用

## 1、简介

​		基于Netfix Ribbon实现的一套 ***客户端*** 负载均衡的工具。主要功能是提供客户端的软件负载均衡算法和服务调用。

​		主要是RestTemplate[^2]+负载均衡

[^2]:① getForObject: 返回对象为响应体中数据转化成的对象，基本上可理解为json ②getForEntity: 返回对象为ResponseEntity对象，包含了响应中的一些重要信息，比如响应头，响应状态码，响应体等

## 2、Ribbon 七大实现

*  com.netflix.loadbalance.***RoundRobinRule***   轮询
*  com.netflix.loadbalance.***RandomRule***  随机
*  com.netflix.loadbalance.***RetryRule***  先按随机，若获取失败则在指定时间内重试
* ***WeightedResponseTimeRule***  对轮询的扩展，响应速度越快的实例选择权重越大，越容易选择
* ***BestAvailableRule*** 会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务
* ***AraillabilityFilteringRule***  先过滤掉故障实例，再选择并发量小的实例
* ***ZoneAvoidanceRule*** 默认规则，符合判断server所在区域的性能和server的可用性选择服务器

## 3、Ribbon实操关键内容

### Ⅰ provider服务提供方

#### 1）pom

```xml
<!-- 含有ribbon的相关依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### 2）yml

​		不需要特别加入配置，默认eureka的配置即可

#### 3）主启动

```java
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class PaymentMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8001.class, args);
    }
}
```

#### 4）业务类

```java
/**
 * 验证负载均衡
 */
@GetMapping(value = "/payment/lb")
public String getPaymentLb(){
    return servicePort;
}
```

-----

### Ⅱ consumer客户端

#### 1）pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### 2）yml

​		不需要特别加入配置，默认eureka的配置即可

#### 3）主启动

```java
@SpringBootApplication
@EnableEurekaClient
//此处为替换默认的负载均衡策略
//@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration = MyselfRule.class)
public class OrderMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class, args);
    }
}
```

#### 4）业务类

* 替换自带的负载均衡策略，***一定注意，想用其它自带的负载均衡策略时，不能将MyselfRule这个文件所在的包放在@SpringBootApplication或@ComponentScan的包或下面的包，必须跳出取。并且注释掉config里的@loadBalance***

  ```java
  @Configuration
  public class MyselfRule {
      @Bean
      public IRule myRule() {
          return new RandomRule();
      }
  }
  ```

* 自定义负载均衡策略(放在平时的位置即可，不需要跳出扫描包)

  ```java
  public interface LoadBalancer {
      ServiceInstance instances(List<ServiceInstance> serviceInstances);
  }
  ```

  ```java
  @Component
  public class MyLb implements LoadBalancer {
  
      /**
       * 用于记录调用次数（AtomicInteger 原子操作类，防止高并发下i++出错）
       */
      private AtomicInteger atomicInteger = new AtomicInteger(0);
  
      /**
       * 获取rest接口第几次请求数并加一（每次服务重启后rest接口计数从1开始）
       *
       * @return
       */
      public final int getAndIncrement() {
          int current;
          int next;
          //判断当前时间的请求次数与期望的值是否相同，不同则被修改过，需要重新获取当前值。直至相同，才允许修改（自旋，防止多线程时自增出错。）
          do {
              //获取当前的值
              current = this.atomicInteger.get();
              //防止超过最大Integer的值
              next = current >= Integer.MAX_VALUE ? 0 : current + 1;
  
          } while (!this.atomicInteger.compareAndSet(current, next));
          System.out.println("******第几次访问，次数next:" + next);
          return next;
      }
  
      @Override
      public ServiceInstance instances(List<ServiceInstance> serviceInstances) {
          //rest接口第几次请求数 % 服务器集群总数 = 实际调用服务器位置下标。
          int index = getAndIncrement() % serviceInstances.size();
          return serviceInstances.get(index);
      }
  }
  ```

* controller层

  ```java
  @GetMapping(value = "/consumer/payment/lb")
  public String getPaymentUrlLB(){
      //获取注册中心里服务名为CLOUD-PAYMENT-SERVICE的所有服务信息
      List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
      if (CollectionUtils.isEmpty(instances)){
          return null;
      }
      //通过自定义算法获取提供服务的信息
      ServiceInstance serviceInstance = loadBalancer.instances(instances);
      URI uri = serviceInstance.getUri();
      return restTemplate.getForObject(uri+"/payment/lb",String.class);
  }
  ```



# 三、 Feign（实际为openFeign） 声明式WebService客户端

## 1、客户端关键内容

### Ⅰ pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### Ⅱ yml

```yml
server:
  port: 80
eureka:
  client:
    register-with-eureka: false
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/
#设置feign客户端超时时间(openFeign默认支持ribbon)
ribbon:
#指的是建立连接所用的时间，适用于网络状况正常的情况下，两端连接所用的时间
  ReadTimeout: 5000
#指的是建立连接后从服务器读取到可用资源所用到的时间
  ConnectTimeout: 5000

logging:
  level:
    # feign日志以什么级别监管哪个接口
    com.guiyang.springcloud.service.PaymentFeignService: debug
```

### Ⅲ 主启动

```java
@SpringBootApplication
@EnableFeignClients
public class OrderFeignMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderFeignMain80.class,args);
    }
}
```

### Ⅳ 业务类

* service层内容与需要调用服务的服务端controller层相似

  ```java
  @Component
  @FeignClient("cloud-payment-service") //对应所要调用的服务名
  public interface PaymentFeignService {
  
      @GetMapping(value = "/payment/get/{id}")
      public CommonResult getPaymentById(@PathVariable("id") Long id);
  
      @GetMapping(value = "/payment/feign/timeout")
      public String paymentFeignTimeout();
  }
  ```

* controller

  ```java
  @RestController
  @Slf4j
  public class OrderFeignController {
      @Resource
      private PaymentFeignService paymentFeignService;
  
      @GetMapping(value = "/consumer/payment/get/{id}")
      public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id) {
          return paymentFeignService.getPaymentById(id);
      }
  
      @GetMapping(value = "/consumer/payment/feign/timeout")
      public String paymentFeignTimeout(){
          //默认响应时间超过一秒报错，不用担心，在配置文件中设置了5秒
          return paymentFeignService.paymentFeignTimeout();
      }
  }
  ```

* config

  ```java
  @Configuration
  public class FeignConfig {
      @Bean
      Logger.Level feignLoggerLevel(){
          return Logger.Level.FULL;
      }
  }
  ```

# 四、Hystrix 断路器

## 1、简介

​			用于处理分布式系统的 ***延迟*** 和 ***容错*** 的开源库。能保证在一个依赖出问题的情况下，<u>不会导致整个服务失败，避免级联故障，以提高分布式系统的弹性</u>



## 2、作用

* 服务降级 ： 友好提示
* 服务熔断 ： 直接拒绝访问并友好提示
* 接近事实的监控
* 限流
* ......

流程 :  服务降级 -----> 熔断 ------> **恢复调用链路**

## 3、服务降级

### Ⅰ provider服务端

#### 1）pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

#### 2）yml

```yaml
#为了服务监控而配置 访问监控服务地址时，需要加actuator，eg.localhost:8001/actuator/hystrix.stream
management:
  endpoints:
    web:
      exposure:
        include: hystrix.stream
```

#### 3）主启动

```java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class,args);
    }
}
```

#### 4）业务类

```java
@Service
public class PaymentService {
    /**
     * 正常访问，肯定ok
     *
     * @param id
     * @return
     */
    public String paymentInfo_OK(Integer id) {
        return "线程池： " + Thread.currentThread().getName() + "   paymentInfo_OK,id:  " + id + "\t";
    }

    /**
     * 延迟访问(fallbackMethod = "paymentInfo_TimeOutHandler"超时和出错都会降级处理 )
     *
     * @param id
     * @return
     */
    @HystrixCommand(fallbackMethod = "paymentInfo_TimeOutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")
    })
    public String paymentInfo_TimeOut(Integer id) {
//        int error = 10 / 0;
        try { TimeUnit.MILLISECONDS.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
        return "线程池： " + Thread.currentThread().getName() + "  id:  " + id + "\t 耗时";
    }

    public String paymentInfo_TimeOutHandler(Integer id) {

        return "线程池： " + Thread.currentThread().getName() + "   系统繁忙或运行报错，请稍后再试,id:  " + id;
    }
}
```

>  	@HystrixCommand(fallbackMethod = "方法名",commandProperties={@HystrixProperty(....),@HystrixProperty(....)}) : 服务方法失败并抛出错误信息后，会自动调用@HystrixCommand标注好的fallbackMethod调用类中指定的方法。

----

### Ⅱ consumer客户端

#### 1） pom

* 与openFeign整合

```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
```

#### 2） yml

```yaml
feign:
  hystrix:
    enabled: true
```

#### 3） 主启动

```java
@SpringBootApplication
@EnableFeignClients
@EnableHystrix
public class OrderHystrixMain80 {
    public static void main(String[] args){
        SpringApplication.run(OrderHystrixMain80.class,args);
    }
}
```

#### 4） 业务类

* service

  ```java
  @Component
  @FeignClient(value = "CLOUD-PROVIDER-HYSTRIX-PAYMENT",fallback = PaymentFallbackService.class)
  public interface PaymentHystrixService {
      @GetMapping(value = "/payment/hystrix/ok/{id}")
      public String paymentInfo_OK(@PathVariable("id")Integer id);
  
      @GetMapping(value = "/payment/hystrix/timeout/{id}")
      public String paymentInfo_TimeOut(@PathVariable("id")Integer id);
  }
  ```

* impl 当服务端宕机或出现延迟无法得到消息时，返回impl里的内容，这样就能保证两方都要兜底方案。同时可实现解耦

  ```java
  @Component
  public class PaymentFallbackService implements PaymentHystrixService{
      @Override
      public String paymentInfo_OK(Integer id) {
          return "----------PaymentFallbackService fallback paymentInfo_OK";
      }
  
      @Override
      public String paymentInfo_TimeOut(Integer id) {
          return "----------PaymentFallbackService fallback paymentInfo_TimeOut";
      }
  }
  ```

* controller

  ```java
  @RestController
  @Slf4j
  @DefaultProperties(defaultFallback = "paymentGlobalFallbackMethod")//全局兜底方案，可以不用一个方法写一个兜底方案了。只需要指定全局兜底的函数，配合@HystrixCommand（不指定参数出错时调用全局兜底方案）即可完成。也解决了代码膨胀的问题
  public class OrderHystrixController {
      @Resource
      private PaymentHystrixService paymentHystrixService;
  
      @GetMapping(value = "/consumer/payment/hystrix/ok/{id}")
      public String paymentInfo_OK(@PathVariable("id")Integer id){
          return paymentHystrixService.paymentInfo_OK(id);
      }
  
      @GetMapping(value = "/consumer/payment/hystrix/timeout/{id}")
  //    @HystrixCommand(fallbackMethod = "paymentTimeOutFallbackMethod",commandProperties = {
  //            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
  //    })
      @HystrixCommand
      public String paymentInfo_TimeOut(@PathVariable("id")Integer id){
          //此行必定报错，用于测试运行时错误是否会使用兜底
          int error = 10 / 0;
          return paymentHystrixService.paymentInfo_TimeOut(id);
      }
  
      public String paymentTimeOutFallbackMethod(@PathVariable("id")Integer id){
          return "我是消费者80，对方支付系统繁忙，请十秒钟后再试或者自己运行出错请检查自己!";
      }
  
      //下面是全局fallback方法
      public String paymentGlobalFallbackMethod(){
          return "Global异常处理信息，请稍后再试！";
      }
  ```

### Ⅲ 作用范围

​		可在客户端，也可在服务端。一般放在服务端。

----

### Ⅳ 哪些情况触发服务降级

* 程序运行异常

* 超时

* 服务熔断触发服务降级

* 线程池/信号量打满也会导致服务降级

  

## 4、服务熔断

### Ⅰ 简介

* 应对雪崩效应的一种微服务保护机制。当扇出链路的某个微服务出错不可用或者响应时间太长时，会进行服务降级。进而熔断该节点微服务的调用，快速返回错误的响应信息。
* ***当检查到该节点微服务调用响应正常后，恢复调用链路***
* 在spring框架中，熔断机制通过hystrix实现。hystrix会监控微服务间调用的优化，当失败的调用达到一定的阈值（缺省是5秒内20次调用失败），就会启动熔断机制。

-----

### Ⅱ 熔断机制示意图

![熔断示意图](https://github.com/guiyang175/spring-cloud-2020/raw/master/image/熔断示意图.png)

### Ⅲ 熔断类型

* 打开：请求不再进行当前服务调用，当打开时长达到所设时钟进入半开
* 关闭：不会对服务进行熔断
* 半开：部分请求根据规则调用当前服务，如果请求成功且符合规则，则认为当前服务恢复正常，关闭熔断

----

### Ⅳ provider服务端

#### 1）pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
<!--        工具包-->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.1.0</version>
</dependency>
```

#### 2）yml

```yml
#为了服务监控而配置 访问监控服务地址时，需要加actuator，eg.localhost:8001/actuator/hystrix.stream
management:
  endpoints:
    web:
      exposure:
        include: hystrix.stream
```

#### 3）主启动

```java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class,args);
    }
}
```

#### 4）业务类

```java
/**
 * @HystrixProperty额外的参数:
 * execution.isolation.strategy THREAD 设置隔离策略，THREAD表示线程池 SEMAPHORE:信号池隔离
 * execution.isolation.semaphore.maxConcurrentRequests 10 当隔离策略选择信号池隔离的时候，用来设置信号池的大小(最大并发数)
 * execution.isolation.thread.timeoutInMilliseconds 10 配置命令执行的超时时间
 * execution.timeout.enabled true 是否启用超时时间
 * execution.isolation.thread.interruptOnTimeout true 执行超时的时候是否中断
 * execution.isolation.thread.interruptOnCancel true 执行被取消的时候是否中断
 * fallback.isolation.semaphore.maxConcurrentRequests 10 允许回调方法执行的最大并发数
 * fallback.enabled true 服务降级是否启用，是否执行回调函数
 * circuitBreaker.enabled true 是否启用断路器
 * ..........更多详情参照官方文档
 * @param id
 * @return
 */
@HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback",commandProperties = {
        @HystrixProperty(name="circuitBreaker.enabled",value = "true"), //是否开启断路器
        @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "10"), //请求次数
        @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "10000"), //时间窗口期（时间范围// ）
        @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "60") //失败率达到多少后跳闸
})
public String paymentCircuitBreaker(@PathVariable("id")Integer id){
    if(id<0){
        throw new RuntimeException("********id 不能为负数");
    }
    //hutool工具包中的函数，自动生成UUID，且去掉中间的-
    String serialNumber = IdUtil.simpleUUID();
    return Thread.currentThread().getName()+"\t 调用成功，流水号："+ serialNumber;
}

public String paymentCircuitBreakerFallback(@PathVariable("id")Integer id){
    return "id 不能为负数，请稍后再试，id"+id;
}
```

---



# 五、Gateway 新一代网关

## 1、简介

​		Gateway使用Webflux中的**reactor-netty**响应式编程组件，底层使用了Netty通讯框架。同时，Gateway 是基于异步非阻塞模型上进行开发的



## 2、特性

* 动态路由 ： 能够匹配任何请求属性
* 可以对路由指定的Predicate（断言）和Filter（过滤器）
* 集成hystrix的断路器功能
* 集成SpringCloud服务发现功能
* 使用易于编写的Predicate和Filter
* 请求限流功能
* 支持路径重写



## 3、三大核心概念

* **路由**（route） ： 路由是构建网关的基本模块，它由ID，目标URI，一系列的断言和过滤器组成。
* **断言**（predicate）： 开发人员可以匹配HTTP请求中的所有内容（eg. 请求头或请求参数）
* **过滤**： 指的是Spring框架中GatewayFilter的实例，使用过滤器，可以在请求路由前后对其进行修改



## 4、服务端关键内容

### Ⅰ pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```



### Ⅱ yml（重点）

```yml
server:
  port: 9527
spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true # 开启从注册中心动态创建路由的功能，利用微服务名进行路由
      routes:
          #路由的ID，没有固定规则但要求唯一，建议配合服务名
        - id: payment_routh
#          #匹配后提供服务的路由地址
#          uri: http://localhost:8001
		  # 使用所需访问的微服务名进行负载均衡访问
          uri: lb://cloud-payment-service
          #断言，路径相匹配的进行路由
          predicates:
            - Path=/payment/get/**

          #路由的ID，没有固定规则但要求唯一，建议配合服务名
        - id: payment_routh2
#          #匹配后提供服务的路由地址
#          uri: http://localhost:8001
          uri: lb://cloud-payment-service
          #断言，路径相匹配的进行路由
          predicates:
            - Path=/payment/lb/**
#            - After=2020-03-25T09:56:16.931+08:00[Asia/Shanghai] #在这个时间之后访问才会有效
#            - Cookie=username,guiyang
#            - Header=X-Request-Id, \d+ #请求头要有X-Request-Id属性并且值为整数的正则表达式
#            - Query=username,\d+ # 要有参数名username并且值还是整数才能路由 eg.http://localhost:9527/payment/lb?username=31
#            - Host=**.guiyang.com #-H "Host:guiyang.com"
#            - Method=Get

#          filters: #一般用自定义全局GlobalFilter，主要是实现GlobalFilter，Ordered接口
#            - AddRequestParameter=X-Request-Id,1024 #过滤器工厂会在匹配的请求头上加上一对请求头，名称为X-Request-Id的值为1024

# 此处指定的是eureka注册中心，若是zookeeper或consul需要改变配置
#eureka:
#  instance:
#    hostname: cloud-gateway-service
#  client:
#    service-url:
#      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/
#    register-with-eureka: true
#    fetch-registry: true
```



### Ⅲ 主启动

```java
@SpringBootApplication
@EnableEurekaClient
public class GateWayMain9527 {
    public static void main(String[] args){
        SpringApplication.run(GateWayMain9527.class,args);
    }
}
```



### Ⅳ 业务类

* config

  ```java
  /** 
   * 可以使用配置类配置，也可以直接使用yml配置（推荐）
   */
  @Configuration
  public class GateWayConfig {
      /**
       * 配置了一个id为route-name的路由规则
       * 当访问地址 http://localhost:9527/guonei时，会自动转发到地址 http://news.baidu.com/guonei
       *
       * @param routeLocatorBuilder
       * @return
       */
      @Bean
      public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
          RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();
  
          routes.route("path_route_guiyang",
                  r -> r.path("/guonei")
                          .uri("http://news.baidu.com/guonei")).build();
          return routes.build();
      }
  
      /**
       * 访问百度新闻的国际板块
       *
       * @param routeLocatorBuilder
       * @return
       */
      @Bean
      public RouteLocator customRouteLocator2(RouteLocatorBuilder routeLocatorBuilder){
          RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();
  
          routes.route("path_route_guoji",
                  r->r.path("/guoji")
                          .uri("http://news.baidu.com/guoji")).build();
          return routes.build();
      }
  }
  ```

* filter（一般用自定义全局GlobalFilter，主要是实现GlobalFilter，Ordered接口）

  ```java
  @Component
  @Slf4j
  public class MyLogGateWayFilter implements GlobalFilter, Ordered {
      @Override
      public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
          log.info("**************come in MyLogGateWayFilter:     "+new Date());
          String uname = exchange.getRequest().getQueryParams().getFirst("uname");
          //判断用户是否合法
          if(uname == null){
              log.info("*******用户名为null,非法用户。");
              //回应，设置http状态码
              exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
              return exchange.getResponse().setComplete();
          }
          //过滤链，将此次判断通过的exchange续传下去
          return chain.filter(exchange);
      }
  
      /**
       * 加载过滤器顺序，一般数字越小，优先级越高
       *
       * @return
       */
      @Override
      public int getOrder() {
          return 0;
      }
  }
  ```



# 七、Config 分布式配置中心

## 1、简介

​		一套集中式的，动态的配置管理设施。即为微服务架构中的微服务提供集中化的外部配置支持。**配置服务器为各个不同微服务应用的所有环境提供了一个中心化的外部配置**

​		公有的抽出来便于修改和管理



## 2、Config的服务端与客户端

### Ⅰ 服务端

#### 1）概念

​		服务端也称为 <u>分布式配置中心，是一个独立的微服务应用</u>。用来连接服务器并为客户端提供获取配置信息，加密/解密信息等访问接口

#### 2）pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

#### 3）yml

```yml
server:
  port: 3344
spring:
  application:
    name: cloud-config-center
  cloud:
    config:
      server:
        git:
          uri: https://github.com/github用户名/springcloud-config.git #连接远程仓库
          search-paths: # 搜索目录
            - springcloud-config
      # 读取分支
      label: master
```

#### 4）主启动

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigCenterMain3344 {
    public static void main(String[] args){
        SpringApplication.run(ConfigCenterMain3344.class,args);
    }
}
```

#### 5）配置规则

* /{label}/{application}-{profile}.yml（推荐这种）

  * master分支

    http://localhost:3344/master/config-dev.yml

  * dev分支

    http://localhost:3344/dev/config-dev.yml

* /{application}-{profile}.yml

  http://localhost:3344/config-dev.yml

* /{application}/{profile}[/{label}]

  http://localhost:3344/config/dev/master



### Ⅱ 客户端

#### 1）概念

​		通过指定的配置中心来管理应用资源以及与业务相关的配置内容。在启动的时候从配置中心获取和加载配置信息。

#### 2）pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

#### 3）yml

此处为bootstrop.yml，优先级大于application.yml

修改下面的label、name、profile获取配置中心对应的文件

config:
  label: master #分支名称
  name: config # 配置文件名称
  profile: dev # 读取后缀名称

```
server:
  port: 3355
spring:
  application:
    name: config-client
  cloud:
    config:
      label: master #分支名称
      name: config # 配置文件名称
      profile: dev # 读取后缀名称
      uri: http://localhost:3344 # 配置中心地址
```

#### 4）主启动

```java
@SpringBootApplication
@EnableEurekaClient
public class ConfigClientMain3355 {
    public static void main(String[] args){
        SpringApplication.run(ConfigClientMain3355.class,args);
    }
}
```

#### 5）业务类

```java
@RestController
@Slf4j
public class ConfigController {
    @Value("${config.info}")
    private String configInfo;

    @GetMapping("/configInfo")
    public String getConfigInfo(){
        return configInfo;
    }
}
```

#### 6）动态刷新之手动刷新

​		不加入动态刷新，配置中心的文件内容更改后，客户端必须重启服务才能获得更改后的文件信息。所以需要配置动态刷新，通过发送特殊的post请求，从而完成动态刷新。(以下配置都是在上面客户端的基础上增加的)

* pom

  引入图形化监控

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  ```

* yml

  ```yml
  # 在此处配置暴露监控端点及在controller层添加refreshScope实现实时刷新配置
  # 暴露监控端点
  management:
    endpoints:
      web:
        exposure:
          include: "*"
  ```

* controller

  @RefreshScope //获悉刷新的内容

* 配置完成后，调出cmd命令，输入curl -X POST "http://localhost:3355/actuator/refresh"

* 产生的问题：

  一个微服务就要发送一次，微服务过多效率就会变得很低并且繁琐

* 后两章给出了解决办法和改进（广播型的自动版的动态刷新）



# 八、Bus 消息总线

## 1、简介

* Bus是用来将分布式系统的节点与轻量级消息系统连接起来的框架，它整合了java的事件处理机制和消息中间件的功能。目前支持RabbitMQ和Kafka

* Bus能管理和传播分布式系统间的消息，就像一个分布式执行器，可用于广播状态更改、事件推送等，也可以当作微服务间的通信通道。

* 总线 ：在微服务架构中，通常会使用轻量级的消息代理来构建一个共用的消息主题，并让系统中所有微服务实例都连接上来。由于该主题中产生的消息会被所有实例监听和消费，所以称它为消息总线。

* ***实现动态刷新的基本原理***

  ​		**ConfigClient实例都监听MQ中同一个topic（默认是SpringCloud Bus)。当一个服务刷新数据的时候，他会把这个消息放入到topic中，这样其它监听同一Topic的服务就能得到通知，然后去更新自身的配置。**

## 2、Bus 动态刷新全局广播

pom文件与上一章一致,yml为新增内容

* 客户端(client)

  ```yml
  spring:
    #rabbitmq相关配置
    rabbitmq:
      host: localhost
      port: 5672
      username: guest
      password: guest
  
  # 在此处配置暴露监控端点及在controller层添加refreshScope实现实时刷新配置
  # 暴露监控端点
  management:
    endpoints:
      web:
        exposure:
          include: "*"
  ```

* 服务端(center）

  ```yml
  spring:
    #rabbitmq相关配置
    rabbitmq:
      host: 172.16.2.66
      port: 5672
      username: guest
      password: guest
  
  #暴露bus刷新配置的端点
  management:
    endpoints:
      web:
        exposure:
          include: 'bus-refresh'
  ```

* 配置完成后，使用命令crul -X POST "http://localhost:3344/actuator/bus-refresh"，发送给服务端，让其影响所有的客户端。（一次发送，处处生效）

## 3、Bus 动态刷新定点通知

* 公式 ：http://localhost:配置中心端口号/actuator/bus-refresh/***{destination}***[^3]
* 作用 ：/bus/refresh请求不再发送到具体的服务实例上，而是发给config server并通过destination参数类指定需要更新配置的服务或实例
* 举例：
  * 当前有服务端3344 两个客户端3355 3366，只想通知3355
  * crul -X POST "http://localhost:3344/actuator/bus-refresh/config-client:3355"

[^3]: 微服务名称加端口号

# 九、Stream 消息驱动

## 1、简介

* SpringCloud Stream 屏蔽底层消息中间件的差异，降低切换成本，统一消息的编程模型。
* 主要通过与Binder交互，通过定义绑定器Binder作为中间层，实现了应用程序与消息中间件细节之间的隔离。
* 目前仅支持RabbitMQ、kafka

## 2、Stream工作示意图

![Stream工作示意图](https://github.com/guiyang175/spring-cloud-2020/raw/master/image/Stream示意图.png)

* Binder ：很方便的连接中间价，屏蔽差异
* Channel ：通道，是队列Queue的一种抽象，在消息通讯系统中就是实现存储和转发的媒介，通过Channel对队列进行配置
* Source 和 Sink : 简单的可理解为参照对象SpringCloud Stream 自身，从Stream发布消息就是输出，接受消息就是输入

## 3、Stream实操关键内容

### Ⅰ provider服务端

#### 1）pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

#### 2）yml

```yml
server:
  port: 8801
spring:
  application:
    name: cloud-stream-provider
  cloud:
    stream:
      binders: # 在此处配置要绑定的rabbitmq的服务信息
        defaultRabbit: # 表示定义的名称，用于与binding整合
          type: rabbit # 消息组件类型
          environment: # 设置rabbitmq的相关的环境配置
            spring:
              rabbitmq:
                host: localhost
                port: 5672
                username: guest
                password: guest
      bindings: # 服务的整合处理
        output: # 表示是消息的生产者发送
          destination: studyExchange # 表示要使用的Exchange名称定义,消费者与生产者使用的通道
          content-type: application/json # 设置消息类型，本次为json。文本则设置为"text/plain"
          binder: defaultRabbit # 设定要绑定的消息服务具体设置

eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/ #集群版
  instance:
    lease-expiration-duration-in-seconds: 5 # 如果现在超时了5秒的间隔(默认是90秒)
    lease-renewal-interval-in-seconds: 2 # 设置心跳的时间间隔（默认是30秒）
    instance-id: send-8801.com # 在信息列表时显示主机名称
    prefer-ip-address: true # 访问的路径变为IP地址
```

#### 3）主启动

```java
@SpringBootApplication
public class StreamMqMain8801 {
    public static void main(String[] args){
        SpringApplication.run(StreamMqMain8801.class,args);
    }
}
```

#### 4）业务类

```java
/**
 * 此处不再使用@Service，因为是与rabbitmq打交道的
 * 基于Stream，然后做output指定通道，开启绑定器
 *
 * EnableBinding可以理解为消息生产者的发送管道
 * EnableBinding(Source.class)定义消息的推送管道
 */
@EnableBinding(Source.class)
public class MessageProviderImpl implements IMessageProvider {

    /**
     * 消息发送管道
     */
    @Resource
    private MessageChannel output;

    @Override
    public String send() {
        String serial = UUID.randomUUID().toString();
        //withPayload设置消息内容
        output.send(MessageBuilder.withPayload(serial).build());
        System.out.println("*******serial"+serial );
        return null;
    }
}
```

### Ⅱ consumer客户端

#### 1）pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

#### 2）yml

```yml
server:
  port: 8802
spring:
  application:
    name: cloud-stream-consument
  cloud:
    stream:
      binders:
        defaultRabbit:
          type: rabbit
          environment:
            spring:
              rabbitmq:
                host: localhost
                port: 5672
                username: guest
                password: guest
      bindings:
        input:
          destination: studyExchange
          content-type: application/json
          binder: defaultRabbit #此处可能报红，不用管
#          group: consumerA #除了有分组的作用，还有持久化作用。没有group的重启后不会消费停机期间的信息
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/ #集群版
  instance:
    lease-expiration-duration-in-seconds: 5 # 如果现在超时了5秒的间隔(默认是90秒)
    lease-renewal-interval-in-seconds: 2 # 设置心跳的时间间隔（默认是30秒）
    instance-id: receive-8802.com # 在信息列表时显示主机名称
    prefer-ip-address: true # 访问的路径变为IP地址
```

#### 3）主启动

```java
@SpringBootApplication
public class StreamMqMain8802 {
    public static void main(String[] args){
        SpringApplication.run(StreamMqMain8802.class,args);
    }
}
```

#### 4）业务类

```java
@Component
@EnableBinding(Sink.class)
public class ReceiveMessageListenerController {

    @Value("${server.port}")
    private String serverPort;

    @StreamListener(Sink.INPUT)
    public void input(Message<String> message){
        System.out.println("消费者1号，------->接收到的消息："+message.getPayload()+"\t port:"+serverPort);
    }
}
```

## 4、Stream分组消费与持久化

在消费端yml配置

```yaml
cloud:
  stream:
    bindings:
      input:
        group: consumerA #除了有分组的作用，还有持久化作用。没有group的重启后不会消费停机期间的信息
```



# 十、Sleuth 分布式请求链路跟踪

## 1、简介

* 产生背景 ：在微服务架构中，一个由客户端发起的请求在后端系统中会经过多个不同的服务节点调用来协同产生最后的请求结果，每一个前端请求都会形成一条复杂的分布式服务调用链路，链路中的任何一环出现高延迟时或错误都会引起整个请求最后的失败
* Spring Cloud Sleuth提供了一套完整的解决方案，在分布式系统中提供解决方案并且兼容支持了zipkin（一个管监控，一个管呈现）
* Spring Cloud F版起，就不用自己构建Zipkin Service了，只需调用jar包即可

## 2、启动zipkin

​		cmd中输入java -jar zipkin-server-2.12.9.exec.jar

​		默认访问网址，localhost:9411

## 3、实操关键内容

provider服务端与consumer客户端所需修改一致

* pom

```xml
<!--        链路监控,包含了sleuth+zipkin-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zipkin</artifactId>
        </dependency>
```

* yml

```yml
spring:
#链路监控
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    sampler:
      probability: 1 # 采样率值介于0到1之间，1则表示全部采集,一般为0.5
```
