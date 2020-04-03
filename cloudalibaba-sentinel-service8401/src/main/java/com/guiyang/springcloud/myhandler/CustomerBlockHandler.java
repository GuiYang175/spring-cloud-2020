package com.guiyang.springcloud.myhandler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.guiyang.springcloud.entities.CommonResult;

/**
 * @author guiyang
 * @date 2020/4/3
 */
public class CustomerBlockHandler {
    public static CommonResult handlerException1(BlockException exception){
        return new CommonResult(444,"按客户自定义，global handlerException---1");
    }

    public static CommonResult handlerException2(BlockException exception){
        return new CommonResult(444,"按客户自定义，global handlerException---2");
    }
}
