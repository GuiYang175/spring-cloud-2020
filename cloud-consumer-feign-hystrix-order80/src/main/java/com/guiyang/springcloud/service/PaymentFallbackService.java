package com.guiyang.springcloud.service;

import org.springframework.stereotype.Component;

/**
 * @author guiyang
 * @date 2020/3/24
 */
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
