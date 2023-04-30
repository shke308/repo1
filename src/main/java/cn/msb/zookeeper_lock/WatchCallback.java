package cn.msb.zookeeper_lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {

    private ZooKeeper zk;

    private String threadName;

    private CountDownLatch latch = new CountDownLatch(1);

    private String pathName;

    public WatchCallback(ZooKeeper zk, String threadName) {
        this.zk = zk;
        this.threadName = threadName;
    }

    public void tryLock() {
        try {
            zk.create("/lock",
                    threadName.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL,
                    this,
                    "abc");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock() {
        try {
            zk.delete(pathName, -1);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    // Watch call back
    @Override
    public void process(WatchedEvent event) {
        Event.EventType type = event.getType();
        switch (type) {
            case None:
            case NodeCreated:
            case NodeDataChanged:
            case NodeChildrenChanged:
            case DataWatchRemoved:
            case ChildWatchRemoved:
            case PersistentWatchRemoved:
                break;
            case NodeDeleted:
                zk.getChildren("/", false, this, "anc");
                break;
        }
    }


    // String call back
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name != null) {
            System.out.println(threadName + ":" + name);
            pathName = name;
            zk.getChildren("/", false, this, "anc");
        }
    }

    // getChildren call back
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        // 一定能看到自己前面的节点
        Collections.sort(children);
        int index = children.indexOf(pathName.substring(1));
        // 是不是第一个
        if (index == 0) {
            // yes
            System.out.println(threadName + ": i am first!");
            try {
                // 这里的setData用于做可重入锁
                zk.setData("/", threadName.getBytes(), -1);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();
        } else {
            // no watch前一个节点
            zk.exists("/" + children.get(index - 1), this, this, "asf");
        }

//        System.out.println(threadName + ": look lock....");
        /*for (String child : children) {
            System.out.println(child);
        }*/
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    // stat call back
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        // 偷懒
    }
}
