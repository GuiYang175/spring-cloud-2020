package com.guiyang.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author guiyang
 * @date 2020/3/23
 */
@Component
public class MyLb implements LoadBalancer {

    /**
     * 用于记录调用次数
     */
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 获取rest接口第几次请求数并加一（每次服务重启后rest接口计数从1开始）
     *
     * @return
     */
    public final int getAndIncrement() {
        int current;
        int next;
        //判断当前时间的请求次数与期望的值是否相同，不同则被修改过，需要重新获取当前值。直至相同，才允许修改（自旋，防止多线程时自增出错。）
        do {
            //获取当前的值
            current = this.atomicInteger.get();
            //防止超过最大Integer的值
            next = current >= Integer.MAX_VALUE ? 0 : current + 1;

        } while (!this.atomicInteger.compareAndSet(current, next));
        System.out.println("******第几次访问，次数next:" + next);
        return next;
    }

    @Override
    public ServiceInstance instances(List<ServiceInstance> serviceInstances) {
        //rest接口第几次请求数 % 服务器集群总数 = 实际调用服务器位置下标。
        int index = getAndIncrement() % serviceInstances.size();
        return serviceInstances.get(index);
    }
}
