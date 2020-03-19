package com.guiyang.springcloud.service;

import com.guiyang.springcloud.entities.Payment;
import org.apache.ibatis.annotations.Param;

/**
 * @author guiyang
 * @date 2020/3/17
 */
public interface PaymentService {
    public int create(Payment payment);

    public Payment getPaymentById(@Param("id")Long id);
}
