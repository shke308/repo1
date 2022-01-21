package com.msb.zookeeper_lock;

import com.msb.zookeeper.*;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestLock {

    ZooKeeper zk;
    ZKConf zkConf;
    DefaultWatch defaultWatch;
    MyConf confMsg = new MyConf();

    @Before
    public void conn(){
        zkConf = new ZKConf();
        zkConf.setAddress("192.168.245.3:2181,192.168.245.4:2181,192.168.245.5:2181,192.168.245.6:2181/testLock");
        zkConf.setSessionTime(1000);
        defaultWatch = new DefaultWatch();
        ZKUtils.setConf(zkConf);
        ZKUtils.setWatch(defaultWatch);
        zk = ZKUtils.getZK();
    }

    @After
    public void close(){
        ZKUtils.closeZK();
    }

    @Test
    public void getConfigFromZK(){

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WatchCallBack watchCallBack = new WatchCallBack();
                    watchCallBack.setZk(zk);
                    String threadName = Thread.currentThread().getName();
                    watchCallBack.setThreadName(threadName);
                    // 每一个线程
                    // 抢锁
                    watchCallBack.tryLock();
                    // 干活
                    System.out.println("ganhuo...");

                    // 释放锁
                    watchCallBack.unLock();
                }
            }).start();
        }
        while (true) {
        }
    }


}
