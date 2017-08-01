package io.github.mayunfei.downloadlib.observer;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

/**
 * Created by mayunfei on 17-8-1.
 */

public class DownloadProcessor {

    /**
     * 缓存
     */
    private Map<String, FlowableProcessor<DownloadEvent>> processorMap;

    private DownloadProcessor() {
        processorMap = new ConcurrentSkipListMap<>();
    }

    private static DownloadProcessor INSTANCE;

    public static synchronized DownloadProcessor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DownloadProcessor();
        }
        return INSTANCE;
    }

    public FlowableProcessor<DownloadEvent> getDownloadProcessor(String key) {
        if (processorMap.get(key) == null) {
            FlowableProcessor<DownloadEvent> processor =
                    BehaviorProcessor.<DownloadEvent>create().toSerialized();
            processorMap.put(key, processor);
        }
        return processorMap.get(key);
    }


}
