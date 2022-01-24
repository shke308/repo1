package cn.msb.zookeeper;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

// 主测试类
public class TestConf {

    private ZooKeeper zk;


    @Before
    public void getConn() {
        zk = ZKUtils.getZk("TestConf");
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
        WatchCallback watch = new WatchCallback();
        MyConf conf = new MyConf();
        watch.setZk(zk);
        watch.setMyConf(conf);
        watch.aWait();
        while(true) {
            if(conf.getConf().equals("")) {
                System.out.println("配置丢了。。。");
                watch.aWait();
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


}
