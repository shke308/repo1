package cn.msb.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

public class DefaultWatch implements Watcher {

    private final CountDownLatch latch;

    public DefaultWatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getState()) {
            case Unknown:
            case NoSyncConnected:
            case Disconnected:
            case AuthFailed:
            case ConnectedReadOnly:
            case SaslAuthenticated:
            case Expired:
            case Closed:
                break;
            case SyncConnected:
                System.out.println(event);
                latch.countDown();
                break;
        }
    }
}
