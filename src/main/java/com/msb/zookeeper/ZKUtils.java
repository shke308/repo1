package com.msb.zookeeper;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * 连接zookeeper的工具类
 */
public class ZKUtils {

    private static ZooKeeper zk;

    // zk服务器ip:端口号/根路径
    private static String address;

    private static DefaultWatch watch = new DefaultWatch();

    private static CountDownLatch latch = new CountDownLatch(1);


    // 读取配置文件
    static {
        InputStream is = ZKUtils.class.getClassLoader().getResourceAsStream("zkConf.properties");
        Properties prop = new Properties();
        try {
            prop.load(is);
            address = prop.getProperty("zkNodes") + "/testConf";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // 获取zk连接
    public static ZooKeeper getZk() {
        try {
            watch.setLatch(latch);
            zk = new ZooKeeper(address, 3000, watch);
            latch.await();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return zk;
    }
}
