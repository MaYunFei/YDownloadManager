package io.github.mayunfei.simple;

import android.os.StrictMode;
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
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadEntity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private Button downloadBtn;
    private Button btnDao;
    private ListView listView;
    private List<TestBean> datas;
    private MainAdapter mainAdapter;
    private TestBean daoTestBean;

    DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(final BaseDownloadEntity data) {

            if (daoTestBean != null) {
                if (data.equals(daoTestBean.getBaseDownloadEntity())) {
                    switch (data.getStatus()) {
                        case DownloadEvent.CANCEL:
                            break;
                        case DownloadEvent.DOWNLOADING:
                            btnDao.setText("DOWNLOADING");
                            btnDao.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DownloadManager.getInstance().pause(data);
                                }
                            });
                            break;
                        case DownloadEvent.ERROR:
                            break;
                        case DownloadEvent.FINISH:
                            break;
                        case DownloadEvent.PAUSE:
                            btnDao.setText("PAUSE");
                            btnDao.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DownloadManager.getInstance().add(data);
                                }
                            });
                            break;
                    }
                }
            }


            for (int i = 0; i < datas.size(); i++) {
                TestBean testBean = datas.get(i);
                if (testBean.getKey().equals(data.getKey())) {
                    if (data.getStatus() == DownloadEvent.CANCEL) {
                        datas.remove(i);
                        mainAdapter.notifyDataSetChanged();
                        break;
                    }
                    testBean.setBaseDownloadEntity(data);
                    mainAdapter.notifyDataSetChanged();
                    break;
                }
            }
            float speed = data.getSpeed();
            float v = speed * 1000 / 1024;
            Log.i(TAG, "speed = " + speed);
            Log.e(TAG, "Thread = " + Thread.currentThread().getName() + "   " + data.toString());
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

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());


        super.onCreate(savedInstanceState);
        DownloadManager.init(this);
        setContentView(R.layout.activity_main);
        downloadBtn = (Button) findViewById(R.id.down_btn);
        btnDao = (Button) findViewById(R.id.btn_dao);
        listView = (ListView) findViewById(R.id.list_view);
        datas = new ArrayList<>();
        mainAdapter = new MainAdapter(datas);
        listView.setAdapter(mainAdapter);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SingleDownloadEntity singleDownloadEntity = new SingleDownloadEntity("key", "http://down.apps.sina.cn/sinasrc/f2/40/39e9780c0ab67c8494247515f6b540f2.apk");
//                DownloadManager.getInstance().add(singleDownloadEntity);
                addTest("https://md.dongaocloud.com/2b50/2b53/4ac/cc3/01c3e61cb43e03d9cf2f9428141d7c8d/01c3e61cb43e03d9cf2f9428141d7c8d-", 500, "yunfei" + 1);
                addTest("https://md.dongaocloud.com/2b50/2b53/227/736/a1f52ebfa24a75d8cbd0acd290ad2858/a1f52ebfa24a75d8cbd0acd290ad2858-", 100, "yunfei" + 2);
                addTest("https://md.dongaocloud.com/2b50/2b53/e8a/d9b/bcefb3996739c9a6f0293d0d9599d4d9/bcefb3996739c9a6f0293d0d9599d4d9-", 226, "yunfei" + 3);
                addTest("https://md.dongaocloud.com/2b50/2b53/6dd/2a1/413b8fcf04c48ff475a6e86f958b5e78/413b8fcf04c48ff475a6e86f958b5e78-", 229, "yunfei" + 4);
                addTest("https://md.dongaocloud.com/2b50/2b53/38d/4a1/a9e6cefb02189e049a260119a7a8d2a7/a9e6cefb02189e049a260119a7a8d2a7-", 278, "yunfei" + 5);
                addTest("https://md.dongaocloud.com/2b50/2b53/d0b/356/9c2bf190837bf9e0fc0f4d914a17e8c9/9c2bf190837bf9e0fc0f4d914a17e8c9-", 241, "yunfei" + 6);
                addTest("https://md.dongaocloud.com/2b50/2b53/6b0/66e/5756b3c6f899844923a666ffd9d76eae/5756b3c6f899844923a666ffd9d76eae-", 254, "yunfei" + 7);
            }
        });

        BaseDownloadEntity entity = DownloadManager.getInstance().queryDownloadEntity("yunfei" + 1);
        if (entity != null) {
            btnDao.setVisibility(View.VISIBLE);
            daoTestBean = new TestBean();
            daoTestBean.setKey(entity.getKey());
            daoTestBean.setBaseDownloadEntity(entity);
            btnDao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadManager.getInstance().add(daoTestBean.getBaseDownloadEntity());
                }
            });
            Log.i(TAG, entity.toString());
        } else {
            btnDao.setVisibility(View.GONE);
        }


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


//        for (int j = 0; j < 20; j++) {
//            String key = "multi key " + j;
//            MultiDownloadEntity multiDownloadEntity = new MultiDownloadEntity(key
//            );
//            List<SingleDownloadEntity> downloadEntities = new ArrayList<>();
//            for (int i = 0; i < 5; i++) {
//                SingleDownloadEntity entity = new SingleDownloadEntity("url" + j + i, "url" + i);
//                downloadEntities.add(entity);
//            }
//            multiDownloadEntity.addAllEntity(downloadEntities);
//
//            DownloadManager.getInstance().add(multiDownloadEntity);
//            TestBean testBean = new TestBean();
//            testBean.setKey(key);
//            testBean.setBaseDownloadEntity(multiDownloadEntity);
//            datas.add(testBean);
//        }
//
//        mainAdapter.notifyDataSetChanged();


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

    private void addTest(String url_pr, int length, String key) {
        MultiDownloadEntity multiDownloadEntity = new MultiDownloadEntity(key
        );
        List<SingleDownloadEntity> downloadEntities = new ArrayList<>();
        for (int i = 0; i <= length; i++) {
            String temp = "";
            if (i < 10) {
                temp = "00" + i;
            }
            if (i >= 10 && i < 100) {
                temp = "0" + i;
            }
            if (i >= 100) {
                temp = "" + i;
            }
            String url = url_pr + temp + ".ts";
            SingleDownloadEntity entity = new SingleDownloadEntity(url, url);
            downloadEntities.add(entity);
        }
        multiDownloadEntity.addAllEntity(downloadEntities);
        TestBean testBean = new TestBean();
        testBean.setKey(key);
        testBean.setBaseDownloadEntity(multiDownloadEntity);
        datas.add(testBean);
        DownloadManager.getInstance().add(multiDownloadEntity);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadManager.getInstance().deleteObserver(watcher);
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
            switch (testBean.getBaseDownloadEntity().getStatus()) {
                case DownloadEvent.CANCEL:
                    break;
                case DownloadEvent.DOWNLOADING:
                    viewHolder.itemBtn.setText("Downloading");
                    viewHolder.itemBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DownloadManager.getInstance().cancel(testBean.getBaseDownloadEntity());
                        }
                    });
                    break;
                case DownloadEvent.ERROR:
                    viewHolder.itemBtn.setText("ERROR");
                    viewHolder.itemBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DownloadManager.getInstance().pause(testBean.getBaseDownloadEntity());
                        }
                    });
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
