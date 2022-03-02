package com.developer.ck.videotimeline;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.material.slider.Slider;
import com.squareup.picasso.Picasso;
import com.video.timeline.ImageLoader;
import com.video.timeline.RetroInstance;
import com.video.timeline.VideoMetadata;
import com.video.timeline.VideoTimeLine;
import com.video.timeline.android.MediaRetrieverAdapter;
import com.video.timeline.render.TimelineGlSurfaceView;
import com.video.timeline.tools.MediaHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewActivity extends AppCompatActivity implements View.OnClickListener {
    private SimpleExoPlayer player;

    private VideoTimeLine fixedVideoTimeline;

    private ImageLoader picassoLoader = (file, view) -> {
        if (file == null || !file.exists()) {
            view.setImageDrawable(new ColorDrawable(Color.LTGRAY));
        } else {
            Picasso.get().load(Uri.fromFile(file))
                    .placeholder(new ColorDrawable(Color.LTGRAY)).into(view);
        }
    };
    private RecyclerView defaultListView;
    private RecyclerView retroListView;
    private RetroInstance retroInstance;
    long duration = 0;
    int frameDuration = 5000;
    int count;
    Slider slider;
    int changeType = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preview);

        PlayerView playerView = findViewById(R.id.playerView);

        List<String> videos = getIntent().getStringArrayListExtra("file_uri");
        if (videos.size() == 1) {
            String fileUri = videos.get(0);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory
                    (new DefaultDataSourceFactory(this, "geo"), new DefaultExtractorsFactory())
                    .createMediaSource(URLUtil.isNetworkUrl(fileUri) ? Uri.parse(fileUri) : Uri.fromFile(new File(fileUri)));

            player = new SimpleExoPlayer.Builder(this).build();
            player.prepare(mediaSource);
            playerView.setPlayer(player);
            player.addAnalyticsListener(new AnalyticsListener() {
                @Override
                public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

                }

                @Override
                public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {
                    if (MimeTypes.isVideo(format.sampleMimeType)) {

                        showBVariant();
                        //fixedVideoTimeline.start();

                    }
                }
            });

            TimelineGlSurfaceView glSurfaceView = findViewById(R.id.fixed_thumb_list);

            fixedVideoTimeline = VideoTimeLine.with(fileUri).into(glSurfaceView);
        }

        playerView.getVideoSurfaceView().setOnClickListener(v -> player.setPlayWhenReady(!player.getPlayWhenReady()));



        retroListView = findViewById(R.id.retro_list_view);
        retroListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        retroListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    long seekPos = (long) (recyclerView.computeHorizontalScrollOffset() * 1F /
//                            recyclerView.computeHorizontalScrollRange()
//                            * player.getDuration());
//                    player.seekTo(seekPos);

                } else {

                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(changeType == 0) {
                    int x = recyclerView.computeHorizontalScrollOffset();
                    float step = x / retroInstance.frameSize();
                    slider.setValue(step);
                }
                changeType = 0;

            }
        });

        slider = findViewById(R.id.slider);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                changeType = 1;
                retroListView.scrollToPosition((int) value);
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }

        if (fixedVideoTimeline != null) {
            fixedVideoTimeline.destroy();
        }

        if (retroInstance != null) {
            retroInstance.onDestroy();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void showBVariant() {
        List<String> videos = getIntent().getStringArrayListExtra("file_uri");

        List<VideoMetadata> mets = new ArrayList<>();

        for (String video: videos) {
            VideoMetadata videoMetadata = new VideoMetadata();
            MediaHelper.getVideoMets(this, video, videoMetadata);
            mets.add(videoMetadata);
        }

        if (retroInstance != null) {
            retroInstance.onDestroy();
        }

        retroInstance = new RetroInstance.Builder(this)
                .softwareDecoder(true)
                .setFrameSizeDp(180)
                .create();

        for (VideoMetadata info: mets) {
            Log.d("2_study", info.getDurationMs() + "");
            duration += info.getDurationMs();
        }
        count = (int) (duration / frameDuration);
        slider.setValue(0);
        slider.setValueTo(count - 1);
        slider.setStepSize(1);
        retroListView.setAdapter(
                new VideoFrameAdapter2(retroInstance, frameDuration, picassoLoader, videos, mets));
    }
}
