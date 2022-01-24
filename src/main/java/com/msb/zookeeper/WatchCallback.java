package cn.msb.zookeeper;

import com.msb.zookeeper.MyConf;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {
    private ZooKeeper zk;

    private MyConf myConf;

    private CountDownLatch latch = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setMyConf(MyConf myConf) {
        this.myConf = myConf;
    }

    /**
     * StatCallback exists()方法的异步形式需要传递的一个Callback，
     * 在查询完zookeeper后，会调用该方法
     * @param rc        状态码
     * @param path      节点路径
     * @param ctx       exists方法传入的参数
     * @param stat      节点状态，如果当前节点不存在返回null
     *                  可以通过stat是否为空判断当前节点是否存在
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if(stat != null) {
            zk.getData("/AppConf", this, this, ctx);
        }
    }

    /**
     * Watch 当节点发生某些事件（如修改，删除等等）时触发的回调方法
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        Event.EventType type = event.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                zk.getData("/AppConf", this, this, "ABC");
                break;
            case NodeDeleted:
                // 根据需要而定
                myConf.setConf("");
                latch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                zk.getData("/AppConf", this, this, "ABC");
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
     * DataCallback getData()方法的异步形式需要传递的一个Callback，
     * 在查询完zookeeper后，会调用该方法
     * 在这里主要是把获取出来的配置信息，存放到我们存储配置信息的类中
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

    /**
     * 获取数据的
     * 想通过exists()判断节点是否存在
     *      如果存在通过StatCallback去掉用getData获取数据
     *      如果不存在，等着节点状态发生改变，触发某些事件后，去调用watch中的方法
     */
    public void aWait() {
        zk.exists("/AppConf", this, this, "ABC");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
