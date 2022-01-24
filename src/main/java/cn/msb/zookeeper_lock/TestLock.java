package cn.msb.zookeeper_lock;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLock {

    private ZooKeeper zk;

    @Before
    public void getConn() {
        zk = ZKUtils.getZk("TestLock");
    }

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
    public void test() {
        for (int i = 0; i < 10; i++) {
            WatchCallback watchCallback = new WatchCallback();
            new Thread(() -> {
                String threadName = Thread.currentThread().getName();
                watchCallback.setZk(zk);
                watchCallback.setThreadName(threadName);
                // 争抢锁
                watchCallback.tryLock();
                // 干活
                System.out.println(threadName + " 抢到锁了，正在干活...");
                // 释放锁
                watchCallback.unLock();
            }).start();
        }

        while (true) {}
    }
}
