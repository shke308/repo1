package com.msb.zookeeper;


import com.msb.zookeeper.WatchCallback;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


/**
 * 客户端，用来从zookeeper中获取配置的类
 */
public class TestConfig {

    private ZooKeeper zk;

    // 获取连接
    @Before
    public void conn () {
        zk = ZKUtils.getZk();
    }

    // 关闭连接
    @After
    public void close() {
        if(zk != null ){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void getConf() {
        WatchCallback watch = new WatchCallback();
        MyConf myConf = new MyConf();
        watch.setZk(zk);
        watch.setMyConf(myConf);
        watch.aWait();
        /**
         * 可能情况
         *      1、节点不存在
         *          aWait（方法中latch.await()等待） -> processResult(因为stat是空，所以进去就出来) -> 直到触发节点NodeCreated事件
         *          -> getData() ->  processResult() -> 返回数据 调用 latch.countDown -> 线程继续往后走
         *      2、节点已存在
         *          aWait（方法中latch.await()等待） -> processResult -> getData() ->processResult
         *          -> 返回数据 调用 latch.countDown -> 线程继续往后走
         *      3、节点数据被改变
         *          触发watch中的process中的 NodeDataChanged事件 -> getData() -> processResult -> 返回数据 -> 线程继续往后走
         */
        while (true) {
            if(myConf.getConf().equals("")) {
                System.out.println("conf diu le...");
                watch.aWait();
            } else {
                System.out.println(myConf.getConf());
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
