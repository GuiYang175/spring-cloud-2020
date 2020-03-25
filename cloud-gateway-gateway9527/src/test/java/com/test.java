package com;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

/**
 * @author guiyang
 * @date 2020/3/25
 */
public class test {

    @Test
    public void getTime(){
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println(now);
    }
}
