package cn.msb.zookeeper_lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallback implements AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback, Watcher{

    private ZooKeeper zk;
    private CountDownLatch latch = new CountDownLatch(1);
    private String threadName;
    private String pathName;

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    // 加锁
    public void tryLock() {
        try {
            zk.create("/lock",
                    threadName.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL,
                    this, "ABC");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock() {
        try {
            zk.delete(pathName, -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * StringCallback 要重写的方法 ，执行完create()后，会回调该方法
     * @param rc
     * @param path
     * @param ctx
     * @param name
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println(threadName + " create node " + name);
        this.pathName = name;
        zk.getChildren("/", false, this, "ABC");
    }

    /**
     * Children2CallBack 要重写的方法，当执行完getChildren后，会回调该方法
     * @param rc
     * @param path
     * @param ctx
     * @param children
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        Collections.sort(children);
        int index = children.indexOf(pathName.substring(1));
        if(index == 0) {
            System.out.println(threadName + " is first.");
            try {
                zk.setData("/", threadName.getBytes(), -1);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();
        } else {
            zk.exists("/" + children.get(index - 1), this, this, "ABC");
        }
    }

    // StatCallback 需要重写方法，执行完 exists()方法后，会回调该方法
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        // 偷懒了没写
    }

    /**
     * Watcher需要重写的方法， 监听事件的发生，然后触发该方法
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren("/", false, this, "ABC");
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
}
