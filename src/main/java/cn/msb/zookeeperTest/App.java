package cn.msb.zookeeperTest;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        InputStream is = App.class.getClassLoader().getResourceAsStream("zkConf.properties");
        CountDownLatch latch = new CountDownLatch(1);
        Properties prop = new Properties();
        try {
            prop.load(is);
            String nodes = prop.getProperty("zkNodes");
            ZooKeeper zk = new ZooKeeper(nodes,
                    3000,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            // 连接状态
                            Event.KeeperState state = event.getState();
                            // 事件类型
                            Event.EventType type = event.getType();
                            // 基于哪个路径触发的事件
                            String path = event.getPath();
                            switch (state) {
                                case Unknown:
                                    break;
                                case Disconnected:
                                    break;
                                case NoSyncConnected:
                                    break;
                                case SyncConnected:
                                    System.out.println("请求连接成功！！！");
                                    System.out.println(event.toString());
                                    latch.countDown();
                                    break;
                                case AuthFailed:
                                    break;
                                case ConnectedReadOnly:
                                    break;
                                case SaslAuthenticated:
                                    break;
                                case Expired:
                                    break;
                                case Closed:
                                    break;
                            }
                            switch (type) {
                                case None:
                                    break;
                                case NodeCreated:
                                    break;
                                case NodeDeleted:
                                    break;
                                case NodeDataChanged:
                                    break;
                                case NodeChildrenChanged:
                                    break;
                                case DataWatchRemoved:
                                    break;
                                case ChildWatchRemoved:
                                    break;
                                case PersistentWatchRemoved:
                                    break;
                            }
                        }
                    });
            latch.await();
            // 阻塞方式
            System.out.println("----------------- zk Blocking start --------------------");
            String path = zk.create("/ooxx", "oldData".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            ZooKeeper.States state = zk.getState();
            switch (state) {
                case CONNECTING:
                    System.out.println("正在连接。。。");
                    break;
                case ASSOCIATING:
                    System.out.println("权限校验通过。。。");
                    break;
                case CONNECTED:
                    System.out.println("连接成功。。。");
                    break;
                case CONNECTEDREADONLY:
                    System.out.println("只读连接。。。");
                    break;
                case CLOSED:
                    System.out.println("连接关闭。。。");
                    break;
                case AUTH_FAILED:
                    System.out.println("权限校验失败。。。");
                    break;
                case NOT_CONNECTED:
                    System.out.println("没有连接。。。");
                    break;
            }
            Stat stat = new Stat();
            byte[] data = zk.getData(path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("getData watcher" + event.toString());
                    try {
                        zk.getData(path, this, stat);
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, stat);
            System.out.println(stat.getCzxid());
            System.out.println(new String(data));
            Stat newState1 = zk.setData(path, "newdata".getBytes(), 0);
            Stat newState2 = zk.setData(path, "newdata01".getBytes(), 1);
            System.out.println("----------------- zk Blocking end ----------------------");

            // 非阻塞方式
            System.out.println("----------------- zk Not Blocking start --------------------");
            zk.getData(path, false, new AsyncCallback.DataCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                    System.out.println(new String(data));
                    System.out.println(ctx.toString());
                }
            }, "abc");
            System.out.println("----------------- zk Not Blocking end ----------------------");

            TimeUnit.SECONDS.sleep(100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
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
}
