package cn.msb.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {
    private final ZooKeeper zk;
    private final MyConf myConf;
    private CountDownLatch latch;

    public WatchCallback(ZooKeeper zk, MyConf myConf) {
        this.zk = zk;
        this.myConf = myConf;
    }

    /**
     * StatCallback 需要重写的方法，调用exists()方法后，回调该方法
     * @param rc
     * @param path
     * @param ctx
     * @param stat  状态，可以用它是否为空，判断节点是否存在
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if(stat != null) {
            zk.getData(path, this, this, "ABC");
        }
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
            case NodeChildrenChanged:
            case DataWatchRemoved:
            case ChildWatchRemoved:
            case PersistentWatchRemoved:
                break;
            case NodeCreated:
            case NodeDataChanged:
                zk.getData(event.getPath(), this, this, "ABC");
                break;
            case NodeDeleted:
                myConf.setConf("");
                break;
        }
    }

    /**
     * DataCallback 需要重写的方法，调用getData()方法后，回调该方法
     * @param rc
     * @param path
     * @param ctx
     * @param data
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if(data != null) {
            myConf.setConf(new String(data));
            latch.countDown();
        }
    }

    public void aWait(String path) {
        latch = new CountDownLatch(1);
        zk.exists(path, this, this, "ABC");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
