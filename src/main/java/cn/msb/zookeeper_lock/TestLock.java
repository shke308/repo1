package cn.msb.zookeeper_lock;

import cn.msb.zookeeper.ZKUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestLock {

    private ZooKeeper zk;

    @Before
    public void getConn() {
        zk = ZKUtils.getZk("testLock");
    }

    @After
    public void close() {
        if (zk != null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testLock() {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                String name = Thread.currentThread().getName();
                WatchCallback watch = new WatchCallback(zk, name);
                // 每一个线程
                // 抢锁
                watch.tryLock();
                // 干活
                System.out.println(name + ": " + "get lock，working。。。");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 释放锁
                watch.unLock();
            }, "thread:" + i).start();
        }

        while (true) {}
    }
}
