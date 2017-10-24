package com.lxy.installapp;

import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.lxy.installapp.databinding.ActivityMainBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import zlc.season.rxdownload3.RxDownload;
import zlc.season.rxdownload3.core.Mission;
import zlc.season.rxdownload3.core.Status;

import static zlc.season.rxdownload3.helper.UtilsKt.dispose;

public class MainActivity extends AppCompatActivity {

    //private String mDownloadUrl = "http://shouji.360tpcdn.com/170918/93d1695d87df5a0c0002058afc0361f1/com.ss.android.article.news_636.apk";
    private String mDownloadUrl = "http://shouji.360tpcdn.com/170919/9f1c0f93a445d7d788519f38fdb3de77/com.UCMobile_704.apk";

    private ActivityMainBinding mBinding;
    private Disposable mDisposable;
    private Status mStatus = new Status();
    private RxPermissions mRxPersission;
    private Mission mMission;
    private boolean mIsFirst = true;
    private File mFile;
    private Context mContext;
    private MaterialDialog mUpLoadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mRxPersission = new RxPermissions(this);
        mContext = this;

        mBinding.btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this)
                        .canceledOnTouchOutside(false)
                        .autoDismiss(false)
                        .title("震撼更新")
                        .content("升级送女朋友")
                        .positiveText("升级")
                        .negativeText("取消");
                MaterialDialog dialog = builder.show();

                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        showUpdataDialog();
                    }
                });
                builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {

                        dialog.dismiss();
                    }
                });

            }
        });
    }

    public void showUpdataDialog(){
        mUpLoadDialog = new MaterialDialog.Builder(MainActivity.this)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .title("更新中……")
                .progress(false, 100, true)
                .negativeText("取消更新")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
        checkUpdate();
    }

    public void checkUpdate() {
        mRxPersission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {

                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (aBoolean) {

                            mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.APK_DOWNLOAD_DIR);
                            if (!mFile.exists()) {
                                Toast.makeText(MainActivity.this, "noExist", Toast.LENGTH_SHORT).show();
                                mFile.mkdirs();
                            }
                            mMission = new Mission(mDownloadUrl, "bft.apk", mFile.getPath());
                            startDownload();
                        } else {
                            Toast.makeText(MainActivity.this, "no", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void startDownload() {

        File file = new File(Environment.getExternalStorageDirectory() + Constant.APK_DOWNLOAD_DIR + "/bft.apk");

        boolean b = FileUtils.isFileExists(file);
        if (b) {
            Toast.makeText(MainActivity.this, "文件已存在", Toast.LENGTH_SHORT).show();
            System.out.println("path=======" + mFile.getPath());
           // install();
            return;
        }

        mDisposable = RxDownload.INSTANCE
                .create(mMission)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Status>() {
                    @Override
                    public void accept(@NonNull Status status) throws Exception {
                        mStatus = status;
                        setProgress(status);
                    }
                });

        //开始下载
        RxDownload.INSTANCE.start(mDownloadUrl).subscribe();
    }

    //设置进度条
    public void setProgress(Status status) {
       // mBinding.progressBar.setMax((int) status.getTotalSize());
       // mBinding.progressBar.setProgress((int) status.getDownloadSize());
       // mBinding.tvPercent.setText(status.percent());

       // int progress = (int) (status.getDownloadSize()*100/status.getTotalSize());
       // System.out.println("status===percent===" + progress);
       // mUpLoadDialog.incrementProgress(1);
       // mUpLoadDialog.setProgress();

        mUpLoadDialog.getProgressBar().setMax((int) status.getTotalSize());
        mUpLoadDialog.getProgressBar().setProgress((int) status.getDownloadSize());

        //mUpLoadDialog.setProgress((int) status.getDownloadSize()/(int) status.getTotalSize());

        if ("100.00%".equals(status.percent()) && mIsFirst) {
            mIsFirst = false;
            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
           // install();
        }

    }

    public void install() {
        AppUtils.installApp(mFile.getPath() + "/bft.apk", "com.lxy.installapp.fileprovider");
//        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
//        installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        String packageName = MainActivity.this.getPackageName();
//        Uri uri = FileProvider.getUriForFile(this,"com.lxy.installapp.fileprovider",mFile);
//        installIntent.setDataAndType(uri,"application/vnd.android.package-archive");
//        startActivity(installIntent);
    }

    //停止下载
    public void stopDownload() {
        RxDownload.INSTANCE.stop(mDownloadUrl).subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mDisposable != null && !mDisposable.isDisposed()) {
//            mDisposable.dispose();
//        }
        dispose(mDisposable);
    }
}
