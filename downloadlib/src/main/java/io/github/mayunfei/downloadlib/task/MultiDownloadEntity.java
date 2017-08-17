package io.github.mayunfei.downloadlib.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mayunfei on 17-8-15.
 */

public class MultiDownloadEntity extends BaseDownloadEntity implements Serializable{

    List<SingleDownloadEntity> downloadEntities;

    public MultiDownloadEntity(String key, String name, String path) {
        super(key, name, path);
        downloadEntities = new ArrayList<>();
    }

    public void addAllEntity(Collection<SingleDownloadEntity> entities){
        downloadEntities.addAll(entities);
        totalSize = entities.size();
    }


}
