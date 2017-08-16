package io.github.mayunfei.downloadlib.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mayunfei on 17-8-15.
 */

public class MultiDownloadEntity extends BaseEntity implements Serializable{

    List<DownloadEntity> downloadEntities;

    public MultiDownloadEntity(String key, String name, String path) {
        super(key, name, path);
        downloadEntities = new ArrayList<>();
    }

    public void addAllEntity(Collection<DownloadEntity> entities){
        downloadEntities.addAll(entities);
        totalSize = entities.size();
    }


}
