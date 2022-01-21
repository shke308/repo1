package com.msb.zookeeper_lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallBack implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {

    ZooKeeper zk;
    String threadName;
    CountDownLatch latch = new CountDownLatch(1);
    String pathName;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    // 加锁
    public void tryLock() {
        try {
            zk.create("/lock", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "abc");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // 解锁
    public void unLock() {
        try {
            zk.delete(pathName, -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    // create watch
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren("/", false, this, "abc");
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

    // create callback
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if(name != null) {
            System.out.println(threadName + "createNode：" + name);
            pathName = name;
            zk.getChildren("/", false, this, "abc");
        }
    }


    // getChildren CallBack
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        // 一定能看到自己前面。。。
        Collections.sort(children);
        int index = children.indexOf(pathName.substring(1));
        if(index == 0) {
            // 是第一个
            System.out.println(threadName + "i am first");
            try {
                zk.setData("/", threadName.getBytes(), -1);
                latch.countDown();
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // 不是第一个要监控我前面的
            zk.exists("/" + children.get(index - 1), this, this, "abc");
        }

    }

    // exists Callback
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
