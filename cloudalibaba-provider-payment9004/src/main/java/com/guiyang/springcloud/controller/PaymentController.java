package com.guiyang.springcloud.controller;

import com.guiyang.springcloud.entities.CommonResult;
import com.guiyang.springcloud.entities.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author guiyang
 * @date 2020/4/3
 */
@RestController
public class PaymentController {
    @Value("${server.port}")
    private String serverPort;

    public static HashMap<Long, Payment> hashMap = new HashMap<>(3);
    static {
        hashMap.put(1L,new Payment(1L,"dadsdadasdadada"));
        hashMap.put(2L,new Payment(2L,"vbvbvbvbvbvvbvb"));
        hashMap.put(3L,new Payment(3L,"hghghghghghghgh"));
    }

    @GetMapping(value="/paymentSQL/{id}")
    public CommonResult<Payment> paymentSQL(@PathVariable("id") Long id){
        Payment payment =hashMap.get(id);
        return new CommonResult(200,"from mysql.serverPort: "+serverPort,payment);
    }
}
