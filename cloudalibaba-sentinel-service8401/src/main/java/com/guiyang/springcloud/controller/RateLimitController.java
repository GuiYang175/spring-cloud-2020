package com.guiyang.springcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.guiyang.springcloud.entities.CommonResult;
import com.guiyang.springcloud.entities.Payment;
import com.guiyang.springcloud.myhandler.CustomerBlockHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guiyang
 * @date 2020/4/3
 */
@RestController
public class RateLimitController {

    /**
     * 按资源名限流
     * @return
     */
    @GetMapping("/byResource")
    @SentinelResource(value = "byResource",blockHandler = "handleException")
    public CommonResult byResource(){
        return new CommonResult(200,"按资源名称限流测试ok",new Payment(2020L,"serial001"));
    }

    public CommonResult handleException(BlockException exception){
        return new CommonResult(444,exception.getClass().getCanonicalName()+"\t 服务不可用");
    }

    /**
     * 按URL限流
     */
    @GetMapping("/rateLimit/byUrl")
    @SentinelResource(value="byUrl")
    public CommonResult byUrl(){
        return new CommonResult(200,"按url限流测试OK",new Payment(2020L,"serial002"));
    }

    /**
     * blockHandlerClass指定类
     * blockHandler指定类中的方法
     * @return
     */
    @GetMapping("/rateLimit/customerBlockHandler")
    @SentinelResource(value="customerBlockHandler",
            blockHandlerClass = CustomerBlockHandler.class,
            blockHandler = "handlerException2")
    public CommonResult customerBlockHandler(){
        return new CommonResult(200,"按客户自定义",new Payment(2020L,"serial003"));
    }
}
