package com.msb.zookeeper_lock;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * zookeeper实现分布式锁的类
 */
public class TestLock {

    private ZooKeeper zk;

    // 获取zk连接
    @Before
    public void conn() {
        zk = ZKUtils.getZk();
    }


    // 关闭zk连接
    @After
    public void close() {
        if(zk != null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void lock() {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                WatchCallback watchCallback = new WatchCallback();
                watchCallback.setZk(zk);
                String threadName = Thread.currentThread().getName();
                watchCallback.setThreadName(threadName);
                // 抢锁
                watchCallback.tryLock();
                // 干活
                System.out.println("working....");

                // 释放锁
                watchCallback.unLock();
            }).start();

        }

        while (true) {}
    }

}
