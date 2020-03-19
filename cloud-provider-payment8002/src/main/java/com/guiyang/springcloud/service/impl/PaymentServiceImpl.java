package com.guiyang.springcloud.service.impl;

import com.guiyang.springcloud.dao.PaymentDao;
import com.guiyang.springcloud.entities.Payment;
import com.guiyang.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author guiyang
 * @date 2020/3/18
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Resource
    private PaymentDao paymentDao;

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }
}
