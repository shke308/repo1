package com.msb.zookeeper_lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {
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

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void tryLock() {
        try {

            zk.create("/lock", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "ABC");
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
     * watch中要重写的方法，在触发某些事件时被回调
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


    /**
     * StringCallback 要重写的方法，在节点创建完后触发的回调
     * @param rc    状态码
     * @param path  节点路径
     * @param ctx   create方法传进来的参数
     * @param name  节点名称 如果为null表示创建失败
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if(name != null) {
            System.out.println(threadName + "create Node: " + name);
            this.pathName = name;
            zk.getChildren("/", false, this, "ABC");
        }
    }

    /**
     * Children2Callback 要重写方法，执行完getChildren后执行的回调
     * @param rc
     * @param path
     * @param ctx           getChildren方法传进来的参数
     * @param children     每个线程至少能看到的自己以及自己前面的所有节点
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        Collections.sort(children);
        int i = children.indexOf(pathName.substring(1));
        if(i == 0) {
            // 第一个线程countDown()，让自己的线程去干活
            System.out.println(threadName + " i am first...");
            try {
                // 主要为了实现锁重入
                // 记录当前是哪条线程获取的锁
                // 这条线程还想要获取锁时，可以直接重入
                zk.setData("/", threadName.getBytes(), -1);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();

        } else {
            // 其他线程去监控它前面的节点是否存在
            zk.exists("/" + children.get(i - 1), this, this, "ABC");

        }
    }

    /**
     * StatCallback 要重写的方法
     * @param rc
     * @param path
     * @param ctx
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        // 偷懒不写了
    }
}
