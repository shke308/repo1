package com.mashibing.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );

        // zk是有session概念的，没有连接池的概念
        // zk里面，watch分为两类
        // watch的注册只发生在读类型的调用，get、exites
        // 第一列：new zk时传递的watch，这个watch是session级别的，跟path、node没有关系
        final CountDownLatch latch = new CountDownLatch(1);
        final ZooKeeper zk = new ZooKeeper("192.168.245.3:2181,192.168.245.4:2181,192.168.245.5:2181,192.168.245.6:2181",
                3000,
                new Watcher() {
                    // Watch回调方法
                    @Override
                    public void process(WatchedEvent event) {
                        // 事件状态
                        Event.KeeperState state = event.getState();
                        // 事件类型
                        Event.EventType type = event.getType();
                        // 事件基于哪个path
                        String path = event.getPath();
                        System.out.println(event.toString());
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
                System.out.println("ing......");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed......");
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

        // 同步阻塞的方式
        String pathName = zk.create("/ooxx", "olddate".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        final Stat stat = new Stat();
        byte[] nodeData = zk.getData(pathName, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData watch：" + event.toString());
                try {
                    // true default Watch 被重新注册 new zookeeper是的时候传递的watch
                    zk.getData("/ooxx", true, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);
        System.out.println(new String(nodeData));
        Stat newState1 = zk.setData(pathName, "newdata".getBytes(), 0);
        Stat newState2 = zk.setData(pathName, "newdata01".getBytes(), 1);

        // 异步回调方式
        System.out.println("-----------------async start-----------------");
        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("-----------------async call back-----------------");
                System.out.println(ctx.toString());
                System.out.println(new String(data));
            }
        }, "abc");

        System.out.println("-----------------async over-----------------");
        Thread.sleep(2222222);

    }
}
