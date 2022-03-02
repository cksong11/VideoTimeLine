package com.developer.ck.videotimeline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import bolts.Task;

public class MainActivity extends AppCompatActivity  implements ItemSelectListener {

    private ArrayList<String> videos = new ArrayList<>();
    RecyclerView rv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.video_list);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        if (Android.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 0)) {
            load();
        }
    }

    @Override
    public void onSelect(String path) {
        videos.add(path);
        open();
    }

    private void load() {
        Task<Object> objectTask = Android.queryRecentVideos(this, 100).continueWith((cont) -> {
            if (cont.getResult() != null) {
                VideoAdapter adapter = new VideoAdapter(MainActivity.this,
                        cont.getResult(), MainActivity.this);
                rv.setAdapter(adapter);
            }
            return null;
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void open() {
        Intent intent = new Intent(this, NewActivity.class);
        intent.putExtra("file_uri", videos);
        startActivity(intent);
        videos.clear();
    }

    @Override // android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == 0) {
            if (Android.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 0)) {
                load();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, paramArrayOfInt);
    }
}