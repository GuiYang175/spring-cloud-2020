package com.guiyang.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author guiyang
 * @date 2020/3/31
 */
@EnableDiscoveryClient
@SpringBootApplication
public class NacosConfigMain3377 {
    public static void main(String[] args){
        SpringApplication.run(NacosConfigMain3377.class,args);
    }
}
