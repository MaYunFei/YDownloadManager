package io.github.mayunfei.downloadlib.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.utils.FileUtils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Created by mayunfei on 17-7-26.
 */

public class SimpleDownload {

    private String url;
    private DownloadApi api;
    private String path;
    private String name;

    private SimpleDownload(Builder builder) {
        url = builder.url;
        api = builder.api;
        path = builder.path;
        name = builder.name;
    }

    private FlowableProcessor<DownloadEvent> start() {
        final BehaviorProcessor<DownloadEvent> downloadProcessor = BehaviorProcessor.create();
        api.download(url).subscribe(new Consumer<Response<ResponseBody>>() {
            @Override
            public void accept(@NonNull Response<ResponseBody> response) throws Exception {
                if (!response.isSuccessful()) {
                    downloadProcessor.onError(new HttpException(response));
                    return;
                }

                ResponseBody body = response.body();
                DownloadEvent event = new DownloadEvent();

                InputStream input = response.body().byteStream();
                File file = new File(path, name);
                if (file.exists()) {
                    file.delete();
                } else {
                    file.mkdir();
                }
                OutputStream output = new FileOutputStream(file);

                FileUtils.save(input, output);

            }
        });

        return downloadProcessor;
    }


    private void cancel() {

    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        private String url;
        private DownloadApi api;
        private String path;
        private String name;

        private Builder() {
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder api(DownloadApi val) {
            api = val;
            return this;
        }

        public Builder path(String val) {
            path = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public SimpleDownload build() {
            return new SimpleDownload(this);
        }
    }
}
