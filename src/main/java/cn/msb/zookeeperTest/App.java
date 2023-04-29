package cn.msb.zookeeperTest;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        final CountDownLatch latch = new CountDownLatch(1);
        // watch 注册只发生在读类型调用，如get(),exists
        ZooKeeper zk = new ZooKeeper("192.168.245.101:2181,192.168.245.102:2181,192.168.245.103:2181,192.168.245.104:2181",
                3000,
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        Event.KeeperState state = event.getState();
                        Event.EventType type = event.getType();
                        String path = event.getPath();
                        System.out.println("new zk watch: " + event);
                        switch (state) {
                            case Unknown:
                                break;
                            case Disconnected:
                                break;
                            case NoSyncConnected:
                                break;
                            case SyncConnected:
                                System.out.println("connected");
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
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("ing....");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed....");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }
        String pathName = zk.create("/ooxx", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        System.out.println(pathName);
        Stat stat = new Stat();
        byte[] node = zk.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData watch: " + event.toString());
                try {
                    // 继续注册watch 这里为true ，注册的是默认的watch
                    // 默认的watch是我们new Zk的时候注册的watch
//                    zk.getData("/ooxx", true, stat);
                    zk.getData("/ooxx", this, stat);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);

        System.out.println(new String(node));
        // 触发getData 的watch
        Stat stat1 = zk.setData("/ooxx", "newData".getBytes(), 0);
        // 不会触发getData的watch了， watch 只会触发一次
        Stat stat2 = zk.setData("/ooxx", "newData01".getBytes(), stat1.getVersion());

        System.out.println("-------------------async start--------------------");

        // 异步getDate，获取数据后，在AsyncCallback.DataCallback.processResult(int rc, String path, Object ctx, byte[] data, Stat stat)中处理
        // 其中 ctx是我们传进去的数据， data是key中的数据，rc是状态码，path是key
        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("--------------async callback -----------------");
                System.out.println(ctx.toString());
                System.out.println(new String(data));
            }
        }, "abc");
        System.out.println("-------------------async over--------------------");

        Thread.sleep(22222222);
    }
}
