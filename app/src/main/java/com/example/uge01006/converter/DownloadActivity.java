package com.example.uge01006.converter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.uge01006.converter.Extractor.VideoMeta;
import com.example.uge01006.converter.Extractor.YouTubeExtractor;
import com.example.uge01006.converter.Extractor.YoutubeFragmentedVideo;
import com.example.uge01006.converter.Extractor.YtFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class DownloadActivity extends AppCompatActivity
{
    private RotateAnimation spinner = new RotateAnimation(360f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private static final int ITAG_FOR_AUDIO = 140;
    private List<YoutubeFragmentedVideo> formatsToShowList;
    private ImageView loadingDownload;
    private TextView TVtitleDownload;
    private Button BTaudio;
    private Button BTvideo360;
    private Button BTvideo480;
    private Button BTvideo720;
    private Button BTvideo1080;
    private Button BTvideo2160;
    private Button BTvideo4320;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TVtitleDownload = (TextView) this.findViewById(R.id.TVtitleDownload);
        BTaudio = (Button) this.findViewById(R.id.BTaudio);
        BTvideo360 = (Button) this.findViewById(R.id.BTvideo360);
        BTvideo480 = (Button) this.findViewById(R.id.BTvideo480);
        BTvideo720 = (Button) this.findViewById(R.id.BTvideo720);
        BTvideo1080 = (Button) this.findViewById(R.id.BTvideo1080);
        BTvideo2160 = (Button) this.findViewById(R.id.BTvideo2160);
        BTvideo4320 = (Button) this.findViewById(R.id.BTvideo4320);
        loadingDownload = (ImageView) this.findViewById(R.id.loadingDownload);
        spinImage();
        String link = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        getYoutubeVideoFileURL(link);

        BTaudio.setOnClickListener(view -> download(getFragment(-1)));
        BTvideo360.setOnClickListener(view -> download(getFragment(360)));
        BTvideo480.setOnClickListener(view -> download(getFragment(480)));
        BTvideo720.setOnClickListener(view -> download(getFragment(720)));
        BTvideo1080.setOnClickListener(view -> download(getFragment(1080)));
        BTvideo2160.setOnClickListener(view -> download(getFragment(2160)));
        BTvideo4320.setOnClickListener(view -> download(getFragment(4320)));
    }
    private YoutubeFragmentedVideo getFragment (int code)
    {
        for (YoutubeFragmentedVideo chosenFragment: formatsToShowList)
        {
            if (chosenFragment.height == code) {return chosenFragment;}
        }
        return null;
    }
    private void download(final YoutubeFragmentedVideo fragmentedVideo)
    {
        String videoTitle = fragmentedVideo.title;
        TVtitleDownload.setText(videoTitle);
        String filename;
        if (videoTitle.length() > 55) {filename = videoTitle.substring(0, 55);}
        else {filename = videoTitle;}
        filename = filename.replaceAll("\\\\|>|<|\"|\\||\\*|\\?|%|:|#|/", "");
        filename += (fragmentedVideo.height == -1) ? "" : "-" + fragmentedVideo.height + "p";
        String downloadIds = "";
        boolean hideAudioDownloadNotification = false;
        if (fragmentedVideo.videoFile != null)
        {
            downloadIds += downloadFromUrl(fragmentedVideo.videoFile.getUrl(), videoTitle, filename + "." + fragmentedVideo.videoFile.getFormat().getExt(), false);
            downloadIds += "-";
            hideAudioDownloadNotification = true;
        }
        if (fragmentedVideo.audioFile != null)
        {
            downloadIds += downloadFromUrl(fragmentedVideo.audioFile.getUrl(), videoTitle, filename + "." + fragmentedVideo.audioFile.getFormat().getExt(), hideAudioDownloadNotification);
        }
        if (fragmentedVideo.audioFile != null)
        {
            cacheDownloadIds(downloadIds);
        }
        finish();
    }
    private void getYoutubeVideoFileURL(String link)
    {
        new YouTubeExtractor(this)
        {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta)
            {
                loadingDownload.clearAnimation();
                TVtitleDownload.setText(vMeta.getTitle());
                formatsToShowList = new ArrayList<>();
                for (int i = 0, itag; i < ytFiles.size(); i++)
                {
                    itag = ytFiles.keyAt(i);
                    YtFile ytFile = ytFiles.get(itag);
                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360)
                    {
                        fillFormatsArray(ytFile, ytFiles);
                    }
                }
                Collections.sort(formatsToShowList, (lhs, rhs) -> lhs.height - rhs.height);
                for (YoutubeFragmentedVideo fragmentedVideo : formatsToShowList)
                {
                    fragmentedVideo.title = vMeta.getTitle();
                    if (fragmentedVideo.height == -1){BTaudio.setVisibility(View.VISIBLE);}
                    if (fragmentedVideo.height == 360){BTvideo360.setVisibility(View.VISIBLE);}
                    if (fragmentedVideo.height == 480){BTvideo480.setVisibility(View.VISIBLE);}
                    if (fragmentedVideo.height == 720){BTvideo720.setVisibility(View.VISIBLE);}
                    if (fragmentedVideo.height == 1080){BTvideo1080.setVisibility(View.VISIBLE);}
                    if (fragmentedVideo.height == 2160){BTvideo2160.setVisibility(View.VISIBLE);}
                    if (fragmentedVideo.height == 4320){BTvideo4320.setVisibility(View.VISIBLE);}
                }
            }
        }.extract(link, true, true);
    }
    private void fillFormatsArray(YtFile ytFile, SparseArray<YtFile> ytFiles)
    {
        int height = ytFile.getFormat().getHeight();
        if (height != -1)
        {
            for (YoutubeFragmentedVideo frVideo : formatsToShowList)
            {
                if (frVideo.height == height && (frVideo.videoFile == null || frVideo.videoFile.getFormat().getFps() == ytFile.getFormat().getFps())) {return;}
            }
        }
        YoutubeFragmentedVideo frVideo = new YoutubeFragmentedVideo();
        frVideo.height = height;
        if (ytFile.getFormat().isDashContainer())
        {
            if (height > 0)
            {
                frVideo.videoFile = ytFile;
                frVideo.audioFile = ytFiles.get(ITAG_FOR_AUDIO);
            }
            else {frVideo.audioFile = ytFile;}
        }
        else {frVideo.videoFile = ytFile;}
        formatsToShowList.add(frVideo);
    }
    private long downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName, boolean hide)
    {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);
        if (hide)
        {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);
        }
        else
        {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        }
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }
    private void cacheDownloadIds(String downloadIds)
    {
        File dlCacheFile = new File(this.getCacheDir().getAbsolutePath() + "/" + downloadIds);
        try {dlCacheFile.createNewFile();}
        catch (IOException e) {e.printStackTrace();}
    }
    private void spinImage()
    {
        spinner.setInterpolator(new LinearInterpolator());
        spinner.setDuration(1200);
        spinner.setRepeatCount(Animation.INFINITE);
        loadingDownload.setVisibility(View.VISIBLE);
        BTaudio.setVisibility(View.INVISIBLE);
        BTvideo360.setVisibility(View.INVISIBLE);
        BTvideo480.setVisibility(View.INVISIBLE);
        BTvideo720.setVisibility(View.INVISIBLE);
        BTvideo1080.setVisibility(View.INVISIBLE);
        BTvideo2160.setVisibility(View.INVISIBLE);
        BTvideo4320.setVisibility(View.INVISIBLE);
        loadingDownload.startAnimation(spinner);
    }
}
