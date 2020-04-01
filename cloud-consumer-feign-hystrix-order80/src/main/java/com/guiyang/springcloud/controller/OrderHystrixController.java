package com.guiyang.springcloud.controller;

import com.guiyang.springcloud.service.PaymentHystrixService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author guiyang
 * @date 2020/3/24
 */
@RestController
@Slf4j
@DefaultProperties(defaultFallback = "paymentGlobalFallbackMethod")
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
        //此处必定出错，检验运行时错误是否会触发兜底
        int error = 10 / 0;
        return paymentHystrixService.paymentInfo_TimeOut(id);
    }

    //兜底函数
    public String paymentTimeOutFallbackMethod(@PathVariable("id")Integer id){
        return "我是消费者80，对方支付系统繁忙，请十秒钟后再试或者自己运行出错请检查自己!";
    }

    //下面是全局fallback方法
    public String paymentGlobalFallbackMethod(){
        return "Global异常处理信息，请稍后再试！";
    }
}
