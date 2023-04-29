package cn.msb.zookeeper;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.common.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

// 主测试类
public class TestConf {

    private ZooKeeper zk;


    @Before
    public void getConn() {
        zk = ZKUtils.getZk("testConf");
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
        MyConf conf = new MyConf();
        WatchCallback watch = new WatchCallback(zk, conf);
        watch.aWait("/AppConf");
        while(true) {
            if(conf.getConf().equals("")) {
                System.out.println("配置丢了。。。");
                watch.aWait("/AppConf");
            } else {
                System.out.println(conf.getConf());
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void getConf() throws InterruptedException {
        MyConf myConf = new MyConf();
        WatchCallback watchCallback = new WatchCallback(zk, myConf);

        watchCallback.aWait("/AppConf");
        while (true) {
            if(StringUtils.isBlank(myConf.getConf())) {
                System.out.println("配置丢了。。。");
                watchCallback.aWait("/AppConf");
            } else {
                Thread.sleep(1000);
                System.out.println(myConf.getConf());
            }
        }
    }

}
