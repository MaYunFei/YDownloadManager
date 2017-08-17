package io.github.mayunfei.simple;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.github.mayunfei.downloadlib.DownloadManager;
import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataWatcher;
import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadEntity;
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;

public class MainActivity extends AppCompatActivity {
    private Button downloadBtn;
    private ListView listView;
    private List<TestBean> datas;
    private MainAdapter mainAdapter;

    DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(final BaseDownloadEntity data) {

            for (int i = 0; i < datas.size(); i++) {
                TestBean testBean = datas.get(i);
                if (testBean.getKey().equals(data.getKey())){
                    testBean.setBaseDownloadEntity(data);
                    mainAdapter.notifyDataSetChanged();
                    break;
                }
            }
            Log.e("TAG ", "Thread = " + Thread.currentThread().getName() + "   " + data.toString());
//            switch (data.getStatus()) {
//                case DownloadEvent.CANCEL:
//                    setTextAndClickListener("START", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            DownloadManager.getInstance().add(data);
//                        }
//                    });
//                    break;
//                case DownloadEvent.DOWNLOADING:
//                    setTextAndClickListener("PAUSE", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            DownloadManager.getInstance().pause(data);
//                        }
//                    });
//                    break;
//                case DownloadEvent.ERROR:
//                    setTextAndClickListener("START", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            DownloadManager.getInstance().add(data);
//                        }
//                    });
//                    break;
//                case DownloadEvent.FINISH:
//                    break;
//                case DownloadEvent.PAUSE:
//                    setTextAndClickListener("START", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            DownloadManager.getInstance().add(data);
//                        }
//                    });
//                    break;
//                case DownloadEvent.WAIT:
//                    break;
//            }
        }
    };

    private void setTextAndClickListener(String text, View.OnClickListener listener) {
        downloadBtn.setText(text);
        downloadBtn.setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadBtn = (Button) findViewById(R.id.down_btn);
        listView = (ListView) findViewById(R.id.list_view);
        datas = new ArrayList<>();
        mainAdapter = new MainAdapter(datas);
        listView.setAdapter(mainAdapter);
        DownloadManager.init(this);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

//        SimpleDownload simpleDownload = DownloadManager.getInstance().simpleDownload("http://down.apps.sina.cn/sinasrc/f2/40/39e9780c0ab67c8494247515f6b540f2.apk", Environment.getExternalStorageDirectory().getAbsolutePath(), "helloworld.apk");
//        simpleDownload.start().subscribe(new Consumer<DownloadEvent>() {
//            @Override
//            public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
//                Log.e("111", downloadEvent.toString());
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(@NonNull Throwable throwable) throws Exception {
//                Log.e("222", throwable.toString());
//            }
//        });


//        for (int i = 0; i < 5; i++) {
//            String key = "single key " +i;
//            SingleDownloadEntity downloadEntity = new SingleDownloadEntity(
//                    key ,
//                    "http://down.apps.sina.cn/sinasrc/f2/40/39e9780c0ab67c8494247515f6b540f2.apk",
//                    "helloworld.apk", Environment.getExternalStorageDirectory().getAbsolutePath());
//            DownloadManager.getInstance().add(downloadEntity);
//            TestBean testBean = new TestBean();
//            testBean.setKey(key);
//            testBean.setBaseDownloadEntity(downloadEntity);
//            datas.add(testBean);
//        }




        for (int j = 0; j < 10; j++) {
            String key = "multi key " + j;
            MultiDownloadEntity multiDownloadEntity = new MultiDownloadEntity(key
                    , "helo", Environment.getExternalStorageDirectory().getAbsolutePath());
            List<SingleDownloadEntity> downloadEntities = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                SingleDownloadEntity entity = new SingleDownloadEntity("url " + i, "name", Environment.getExternalStorageDirectory().getAbsolutePath());
                downloadEntities.add(entity);
            }
            multiDownloadEntity.addAllEntity(downloadEntities);

            DownloadManager.getInstance().add(multiDownloadEntity);
            TestBean testBean = new TestBean();
            testBean.setKey(key);
            testBean.setBaseDownloadEntity(multiDownloadEntity);
            datas.add(testBean);
        }

        mainAdapter.notifyDataSetChanged();


        DownloadManager.getInstance().addObserver(watcher);

        //全局保存 downloadEntity 实现 重新下载

//        DownloadManager.getInstance().add(this, downloadEntity);
//        Disposable subscribe = DownloadManager.getInstance().getDownloadProcessor(downloadEntity.getKey())
//                .subscribe(new Consumer<DownloadEvent>() {
//                    @Override
//                    public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
//                        Log.e("YunFei", downloadEvent.toString());
//                    }
//                });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadManager.getInstance().delectObserver(watcher);
    }

    private static class MainAdapter extends BaseAdapter {
        public MainAdapter(List<TestBean> list) {
            this.list = list;
        }

        private List<TestBean> list;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item, null);
                viewHolder.itemBtn = (Button) convertView.findViewById(R.id.btn_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final TestBean testBean = list.get(position);
            switch (testBean.getBaseDownloadEntity().getStatus()){
                case DownloadEvent.CANCEL:
                    break;
                case DownloadEvent.DOWNLOADING:
                    viewHolder.itemBtn.setText("Downloading");
                    viewHolder.itemBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DownloadManager.getInstance().pause(testBean.getBaseDownloadEntity());
                        }
                    });
                    break;
                case DownloadEvent.ERROR:
                    break;
                case DownloadEvent.FINISH:
                    viewHolder.itemBtn.setText("Finish");
                    break;
                case DownloadEvent.PAUSE:
                    viewHolder.itemBtn.setText("Pause");
                    viewHolder.itemBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DownloadManager.getInstance().add(testBean.getBaseDownloadEntity());
                        }
                    });
                    break;
                case DownloadEvent.WAIT:
                    viewHolder.itemBtn.setText("wait");
                    break;
            }


            return convertView;
        }

        class ViewHolder {
            Button itemBtn;
        }

    }
}
