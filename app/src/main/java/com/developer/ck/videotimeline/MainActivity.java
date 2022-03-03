package com.developer.ck.videotimeline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import bolts.Task;

public class MainActivity extends AppCompatActivity  implements ItemSelectListener {
    final static int SELECT_VIDEO = 101;
    private ArrayList<String> videos = new ArrayList<>();
    RecyclerView rv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (Android.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 0)) {
            load();
        }
    }

    public void onSelect(String path) {
        videos.add(path);
        open();
    }

    private void load() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Video"), SELECT_VIDEO);
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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_VIDEO) {
                Uri selectedMediaUri = data.getData();
                String path = FileUtils.getPath(this, selectedMediaUri);
                onSelect(path);
            }
        }
    }
}