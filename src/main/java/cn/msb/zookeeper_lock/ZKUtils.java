package cn.msb.zookeeper_lock;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ZKUtils {
    private static ZooKeeper zk;
    private static String address;
    private static CountDownLatch latch = new CountDownLatch(1);

    static {
        Properties prop = new Properties();
        InputStream is = ZKUtils.class.getClassLoader().getResourceAsStream("zkConf.properties");
        try {
            prop.load(is);
            address = prop.getProperty("zkNodes") + "/";
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

    public static ZooKeeper getZk(String path) {
        address += path;
        DefaultWatch watch = new DefaultWatch();
        watch.setLatch(latch);
        try {
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
