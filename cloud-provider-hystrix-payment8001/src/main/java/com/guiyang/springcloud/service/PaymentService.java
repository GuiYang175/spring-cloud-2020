package com.guiyang.springcloud.service;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

/**
 * @author guiyang
 * @date 2020/3/23
 */
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

    /******************************服务熔断*************************************/

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
     * ..........详情参照官方文档
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
        String serialNumber = IdUtil.simpleUUID();
        return Thread.currentThread().getName()+"\t 调用成功，流水号："+ serialNumber;
    }

    public String paymentCircuitBreakerFallback(@PathVariable("id")Integer id){
        return "id 不能为负数，请稍后再试，id"+id;
    }

}
