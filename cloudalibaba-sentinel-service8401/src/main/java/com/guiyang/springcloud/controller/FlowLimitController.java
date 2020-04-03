package com.guiyang.springcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        //测试阈值类型为线程数的流控规则；
//        try { TimeUnit.MILLISECONDS.sleep(800); } catch (InterruptedException e) { e.printStackTrace(); }
        //测试降级规则中的RT；
//        try { TimeUnit.MILLISECONDS.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
        //测试降级规则中的异常比例和异常数
        int error = 10/0;

        log.info(Thread.currentThread().getName()+"\t .....testA");
        return "------testA";
    }

    @GetMapping("/testB")
    public String testB(){
        log.info(Thread.currentThread().getName()+"\t .....testB");
        return "------testB";
    }

    @GetMapping("/testHotKey")
    @SentinelResource(value = "testHotKey",blockHandler = "deal_testHotKey")
    public String testHostKey(@RequestParam(value="p1",required = false)String p1,
                              @RequestParam(value="p2",required = false)String p2)
    {
        return "------testHotKey";
    }

    //兜底方法
    public String deal_testHotKey(String p1, String p2, BlockException exception){
        //sentinel系统默认的提示：Blocked by Sentinel(flow limiting)
        return "-----deal_testHotKey";
    }
}
