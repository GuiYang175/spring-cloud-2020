package com.guiyang.springcloud.service.impl;

import com.guiyang.springcloud.service.IMessageProvider;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author guiyang
 * @date 2020/3/26
 *
 * 此处不再使用@Service，因为是与rabbitmq打交道的
 * 基于Stream，然后做output指定通道，开启绑定器
 *
 * EnableBinding可以理解为消息生产者的发送管道
 * EnableBinding(Source.class)定义消息的推送管道
 */
@EnableBinding(Source.class)
public class MessageProviderImpl implements IMessageProvider {

    /**
     * 消息发送管道
     */
    @Resource
    private MessageChannel output;

    @Override
    public String send() {
        String serial = UUID.randomUUID().toString();
        //withPayload设置消息内容
        output.send(MessageBuilder.withPayload(serial).build());
        System.out.println("*******serial"+serial );
        return null;
    }
}
