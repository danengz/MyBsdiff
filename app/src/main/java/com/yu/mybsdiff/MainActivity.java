package com.yu.mybsdiff;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.sample_text);
        tv.setText(BuildConfig.VERSION_NAME);

        // 运行时权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String perms[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 1000);
            }
        }
    }



    public void update(View view) {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {

                String patch = new File(Environment.getExternalStorageDirectory(), "patch").getAbsolutePath();
                String oldApk = getApplicationInfo().sourceDir;
                String output = createNewApk().getAbsolutePath();

                if (!new File(patch).exists()) {
                    return null;
                }
                bsPatch(oldApk, patch, output);
                return new File(output);
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                //安装新apk
                if (file != null) {
                    if (!file.exists()) return;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Uri fileUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationInfo().packageName + ".fileprovider", file);
                        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                    } else {
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    }
                    MainActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "差分包不存在！", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }


    /**
     * 创建合成后的新版本apk文件
     *
     * @return
     */
    private File createNewApk() {
        File newApk = new File(Environment.getExternalStorageDirectory(), "bsdiff.apk");
        if (!newApk.exists()) {
            try {
                newApk.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newApk;
    }


    private native void bsPatch(String oldApk, String patch, String output);
}
