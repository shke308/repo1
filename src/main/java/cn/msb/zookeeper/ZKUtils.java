package cn.msb.zookeeper;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

// zookeeper工具类
public class ZKUtils {
    private static ZooKeeper zk;
    private static String address;
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static final DefaultWatch watch = new DefaultWatch(latch);

    static {
        Properties prop = new Properties();
        try(InputStream is = ZKUtils.class.getClassLoader().getResourceAsStream("zkConf.properties")) {
            prop.load(is);
            address = prop.getProperty("zkNodes") + "/";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ZooKeeper getZk(String path) {
        address += path;
        try {
            zk = new ZooKeeper(address, 3000, watch);
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }

}
