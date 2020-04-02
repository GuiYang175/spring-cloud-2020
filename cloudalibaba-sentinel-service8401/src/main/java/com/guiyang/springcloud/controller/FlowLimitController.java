package com.guiyang.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guiyang
 * @date 2020/4/2
 */
@RestController
@Slf4j
public class FlowLimitController {
    @GetMapping("/testA")
    public String testA(){
        //测试阈值类型为线程数的流控规则
//        try { TimeUnit.MILLISECONDS.sleep(800); } catch (InterruptedException e) { e.printStackTrace(); }
        return "------testA";
    }

    @GetMapping("/testB")
    public String testB(){
        log.info(Thread.currentThread().getName()+"\t .....testB");
        return "------testB";
    }
}
