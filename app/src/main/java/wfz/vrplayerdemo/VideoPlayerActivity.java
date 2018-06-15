package wfz.vrplayerdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asha.vrlib.MDDirectorCamUpdate;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.plugins.MDWidgetPlugin;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class VideoPlayerActivity extends AppCompatActivity {
    private LinearLayout settingPanel;
    private LinearLayout ll;
    private Button up, down, reSetYBtn, shortBtn, longBtn, reSetXBtn, farBtn, nearBtn, reSetZBtn;
    private GLSurfaceView glSurfaceView;
    private SelectPoint pointL, pointR;
    private long lastSelectTime;
    private TextView settingTv;
    private ImageView rePosIv;
    private Spinner spinner;
    private MDWidgetPlugin rePosPlugin;
    private MDWidgetPlugin playPlugin, pausePlugin, ffPlugin, rewPlugin;
    //private MDAbsView controlPanelView;
    private boolean isRePosIconShow, isControlIconsShow/*, isControlPanelViewShow*/;
    private long videoDuration;
    private long videoDurationSec;
    private long videoCurrentPosition = -1;
    private long lastWantSeekSec = -1;
    private String videoDurationStr;
    private MDVRLibrary mVRLibrary;
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private MDVRLibrary.IImageLoadProvider mImageLoadProvider;
    private static final long checkNeedTime = 2_000;
    private static final int FAST_X = 5;
    private static final long lvlUpTime = 5_000;
    private int fastXX = 2;
    private static final int UP_DOWN_TOP_MARGIN = 6;
    private SharedPreferences sp;
    private static final String SETTING_TOP = "top";
    private static final String SETTING_LONG = "long";
    private static final String SETTING_EYE_Z = "eyeZ";

    private static final SparseArray<String> sProjectionMode = new SparseArray<>();

    static {
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_SPHERE, "球面");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180, "半球180°");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230, "半球230°");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180_UPPER, "半球180°upper");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230_UPPER, "半球230°upper");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL, "立体球面左右");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL, "立体球面上下");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FIT, "平面 FIT");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_CROP, "平面 CROP");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FULL, "平面 FULL");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL, "鱼眼左右");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL, "鱼眼上下");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initMediaPlayer();
        initVRLibrary();
        preparePlay();
        initSettingPanel();
        initSetting();
    }

    private void preparePlay() {
        Uri uri = getUri();
        System.out.println("播放" + uri.toString());
        if (uri != null) {
            mMediaPlayerWrapper.openRemoteFile(uri.toString());
            mMediaPlayerWrapper.prepare();
        }
    }

    private void initSettingPanel() {

        ll = findViewById(R.id.ll);
        glSurfaceView = findViewById(R.id.gl_view);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        reSetYBtn = findViewById(R.id.reSetYBtn);
        shortBtn = findViewById(R.id.shortBtn);
        longBtn = findViewById(R.id.longBtn);
        reSetXBtn = findViewById(R.id.reSetXBtn);
        farBtn = findViewById(R.id.farBtn);
        nearBtn = findViewById(R.id.nearBtn);
        reSetZBtn = findViewById(R.id.reSetZBtn);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
                if (lp.topMargin > UP_DOWN_TOP_MARGIN){
                    lp.topMargin -= UP_DOWN_TOP_MARGIN;
                    glSurfaceView.setLayoutParams(lp);
                    ll.setLayoutParams(lp);
                    sp.edit().putInt(SETTING_TOP, lp.topMargin).apply();
                } else if (lp.topMargin != 0){
                    lp.topMargin = 0;
                    glSurfaceView.setLayoutParams(lp);
                    ll.setLayoutParams(lp);
                    sp.edit().putInt(SETTING_TOP, lp.topMargin).apply();
                }
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
                lp.topMargin += UP_DOWN_TOP_MARGIN;
                glSurfaceView.setLayoutParams(lp);
                ll.setLayoutParams(lp);
                sp.edit().putInt(SETTING_TOP, lp.topMargin).apply();
            }
        });
        reSetYBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
                lp.topMargin = 0;
                glSurfaceView.setLayoutParams(lp);
                ll.setLayoutParams(lp);
                sp.edit().putInt(SETTING_TOP, lp.topMargin).apply();
            }
        });

        shortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
                lp.leftMargin += UP_DOWN_TOP_MARGIN;
                lp.rightMargin += UP_DOWN_TOP_MARGIN;
                glSurfaceView.setLayoutParams(lp);
                ll.setLayoutParams(lp);
                sp.edit().putInt(SETTING_LONG, lp.leftMargin).apply();
            }
        });
        longBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
                if (lp.leftMargin > UP_DOWN_TOP_MARGIN){
                    lp.leftMargin -= UP_DOWN_TOP_MARGIN;
                    lp.rightMargin = lp.leftMargin;
                    glSurfaceView.setLayoutParams(lp);
                    ll.setLayoutParams(lp);
                    sp.edit().putInt(SETTING_LONG, lp.leftMargin).apply();
                } else if (lp.leftMargin != 0){
                    lp.leftMargin = 0;
                    lp.rightMargin = lp.leftMargin;
                    glSurfaceView.setLayoutParams(lp);
                    ll.setLayoutParams(lp);
                    sp.edit().putInt(SETTING_LONG, lp.leftMargin).apply();
                }
            }
        });
        reSetXBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
                lp.leftMargin = 0;
                lp.rightMargin = lp.leftMargin;
                glSurfaceView.setLayoutParams(lp);
                ll.setLayoutParams(lp);
                sp.edit().putInt(SETTING_LONG, lp.leftMargin).apply();
            }
        });

        farBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
                System.out.println("cameraUpdate-->EyeZ:"+ cameraUpdate.getEyeZ() + "+1");
                cameraUpdate.setEyeZ(cameraUpdate.getEyeZ()+1);
                sp.edit().putFloat(SETTING_EYE_Z, cameraUpdate.getEyeZ()).apply();
            }
        });
        nearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
                float z = cameraUpdate.getEyeZ();
                if (z > 1){
                    System.out.println("cameraUpdate-->EyeZ:"+ z + "-1");
                    cameraUpdate.setEyeZ(z-1);
                    sp.edit().putFloat(SETTING_EYE_Z, cameraUpdate.getEyeZ()).apply();
                } else if (z != 0) {
                    System.out.println("cameraUpdate-->EyeZ:"+ cameraUpdate.getEyeZ() + "-> 0");
                    cameraUpdate.setEyeZ(0);
                    sp.edit().putFloat(SETTING_EYE_Z, cameraUpdate.getEyeZ()).apply();
                }

            }
        });
        reSetZBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
                System.out.println("cameraUpdate-->EyeZ:"+ cameraUpdate.getEyeZ() + "-> 0");
                cameraUpdate.setEyeZ(0);
                sp.edit().putFloat(SETTING_EYE_Z, cameraUpdate.getEyeZ()).apply();
            }
        });

        settingTv = findViewById(R.id.settingTv);
        rePosIv = findViewById(R.id.rePosIv);
        settingPanel = findViewById(R.id.setting_panel);

        rePosIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rePosition();
            }
        });

        settingTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (View.VISIBLE == settingPanel.getVisibility()) {
                    settingPanel.setVisibility(View.GONE);
                    settingTv.setText("设置");
                } else {
                    settingPanel.setVisibility(View.VISIBLE);
                    settingTv.setText(" X ");
                    int index = sProjectionMode.indexOfKey(mVRLibrary.getProjectionMode());
                    index = index == -1 ? 0 : index;
                    spinner.setSelection(index);
                }
            }
        });

        spinner = SpinnerHelper.with(this)
                .setData(sProjectionMode)
                .setDefault(mVRLibrary.getProjectionMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        if (mVRLibrary.getProjectionMode() != key) {
                            System.out.println("显示模式改为" + value);
                            mVRLibrary.switchProjectionMode(VideoPlayerActivity.this, key);
                        } else {
                            System.out.println("显示模式已经是" + value + "了");
                        }
                    }
                })
                .init(R.id.spinner_projection);
    }

    private void initSetting() {
        sp = getSharedPreferences("VrSetting", 0);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
        lp.topMargin = sp.getInt(SETTING_TOP, 0);
        lp.leftMargin = sp.getInt(SETTING_LONG, 0);
        lp.rightMargin = lp.leftMargin;
        glSurfaceView.setLayoutParams(lp);
        ll.setLayoutParams(lp);
    }

    private void initEyeZ(){
        MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
        float z = sp.getFloat(SETTING_EYE_Z, 0);
        System.out.println("cameraUpdate-->EyeZ:"+ cameraUpdate.getEyeZ() + "-> z");
        cameraUpdate.setEyeZ(z);
    }

    protected Uri getUri() {
        Intent i = getIntent();
        System.out.println("包名："+i.toString());
        if (i == null || i.getData() == null) {
            return null;
        }
        return i.getData();
    }

    private void initMediaPlayer() {
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                //cancelBusy();
                videoDuration = mMediaPlayerWrapper.mPlayer.getDuration();
                videoDurationSec = videoDuration/1000;
                videoDurationStr = formatTime(videoDurationSec);
                if (getVRLibrary() != null) {
                    int w = mMediaPlayerWrapper.mPlayer.getVideoWidth();
                    int h = mMediaPlayerWrapper.mPlayer.getVideoHeight();
                    System.out.println("视频准备好，分辨率："+ w + "x" + h);
                    if (h >= w) {
                        mVRLibrary.switchProjectionMode(VideoPlayerActivity.this, MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL);
                    } else if (w > 2*h){
                        mVRLibrary.switchProjectionMode(VideoPlayerActivity.this, MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL);
                    }
                    getVRLibrary().notifyPlayerChanged();
                }
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d", what, extra);
                Toast.makeText(VideoPlayerActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                System.out.println("OnVideoSizeChanged，分辨率："+ width + "x" + height);
                getVRLibrary().onTextureResize(width, height);
            }
        });
    }

    private void initVRLibrary() {
        pointL = findViewById(R.id.hotspot_point1);
        pointR = findViewById(R.id.hotspot_point2);
        // new instance
        mVRLibrary = MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_GLASS)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        // IjkMediaPlayer or MediaPlayer
                        getPlayer().setSurface(surface);
                        initEyeZ();
                    }
                })
                .listenGesture(new MDVRLibrary.IGestureListener() {
                    @Override
                    public void onClick(MotionEvent e) {
                        System.out.println(getVRLibrary().getDirectorBrief().toString());
                        if (View.VISIBLE == settingPanel.getVisibility()) {
                            settingPanel.setVisibility(View.GONE);
                            settingTv.setText("设置");
                        }
                    }
                })
                .build(findViewById(R.id.gl_view));
        mVRLibrary.setAntiDistortionEnabled(true);
        mVRLibrary.setEyePickChangedListener(new MDVRLibrary.IEyePickListener2() {
            @Override
            public void onHotspotHit(MDHitEvent hitEvent) {
                IMDHotspot hotspot = hitEvent.getHotspot();
                long hitTimestamp = hitEvent.getTimestamp();
                if (lastSelectTime != hitTimestamp){
                    pointL.startRate(checkNeedTime);
                    pointR.startRate(checkNeedTime);
                    lastSelectTime = hitTimestamp;
                }
                long nowTimestamp = System.currentTimeMillis();
                if (hotspot != null) {
                    //System.out.println("选中-->"+hotspot);
                    if (nowTimestamp - hitTimestamp > checkNeedTime) {
                        if (hotspot == rePosPlugin){
                            rePosition();
                            removePlugins();
                            getVRLibrary().resetEyePick();
                        } else if (hotspot == playPlugin){
                            getPlayer().mPlayer.start();
                            getVRLibrary().resetEyePick();
                        } else if (hotspot == pausePlugin){
                            getPlayer().mPlayer.pause();
                            getVRLibrary().resetEyePick();
                            pointL.cancelRate();
                            pointR.cancelRate();
                        } else if (hotspot == ffPlugin){
                            calSeekSec(nowTimestamp, hitTimestamp, true);
                        } else if (hotspot == rewPlugin){
                            calSeekSec(nowTimestamp, hitTimestamp, false);
                        }
                    }

                } else {
                    pointL.cancelRate();
                    pointR.cancelRate();
                    float pitch = getVRLibrary().getDirectorBrief().getPitch();
                    if (pitch < -55) {
                        showRePosIcon();
                    } else if (pitch > 30) {
                        showControlIcons();
                        //showControlPanel();
                    } else {
                        removePlugins();
                    }

                    if (lastWantSeekSec > -1) {
                        System.out.println("seekTo-->"+lastWantSeekSec*1000+"/"+ videoDuration);
                        getPlayer().mPlayer.seekTo(lastWantSeekSec*1000);
                        lastWantSeekSec = -1;
                        videoCurrentPosition = -1;
                    }
                }
            }
        });

        mImageLoadProvider = new ImageLoadProvider(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
        mMediaPlayerWrapper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
        mMediaPlayerWrapper.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVRLibrary.onDestroy();
        mMediaPlayerWrapper.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }

    public MDVRLibrary getVRLibrary() {
        return mVRLibrary;
    }

    public MediaPlayerWrapper getPlayer() {
        return mMediaPlayerWrapper;
    }

    private void rePosition() {
        float yaw = getVRLibrary().getDirectorBrief().getYaw();
        MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
        cameraUpdate.setPitch(cameraUpdate.getPitch() - (90f - yaw));
        System.out.println("onClick-->updateCamera: pitch=" + cameraUpdate.getPitch()
                + ", yaw=" + cameraUpdate.getYaw()
                + ", roll=" + cameraUpdate.getRoll());
    }

    /*private void showControlPanel() {
        if (isControlPanelViewShow){
            return;
        }
        removePlugins(true);
        if (controlPanelView == null) {
            View view = getLayoutInflater().inflate(R.layout.control_panel, null,false);
            MDViewBuilder builder = MDViewBuilder.create()
                    .provider(view, 160*3*//*view width*//*, 50*3*//*view height*//*)
                    .size(3.2f, 1)
                    .position(MDPosition.newInstance().setZ(-8.0f))
                    .title("ControlPanel view")
                    .tag("controlPanelView");

            controlPanelView = new MDView(builder);
        }

        getVRLibrary().addPlugin(controlPanelView);
        isControlPanelViewShow = true;
    }*/

    long lastSec;
    int skipTime = 4;
    int time;
    private void showControlIcons() {
        if (isControlIconsShow) {
            time++;
            if (time > skipTime){
                long sec = getPlayer().mPlayer.getCurrentPosition() / 1000;
                if (lastSec != sec){
                    String curP = formatTime(sec);
                    //c += (nowTimestamp - hitTimestamp)
                    pointL.showText(curP + "/" + videoDurationStr);
                    pointR.showText(curP + "/" + videoDurationStr);
                    lastSec = sec;
                }
                time = 0;
            }
            return;
        }
        removePlugins();
        if (playPlugin == null) {
            MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                    .size(1f, 1f)
                    .provider(this, R.mipmap.ic_media_play)
                    .title("play logo")
                    .tag("play")
                    .position(MDPosition.newInstance().setZ(-12.0f).setY(1.0f));
            playPlugin = new MDWidgetPlugin(builder);
        }

        if (pausePlugin == null) {
            MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                    .size(1f, 1f)
                    .provider(this, R.mipmap.ic_media_pause)
                    .title("pause logo")
                    .tag("pause")
                    .position(MDPosition.newInstance().setZ(-12.0f).setY(1.0f));
            pausePlugin = new MDWidgetPlugin(builder);
        }

        if (getPlayer().mPlayer.isPlaying()){
            pausePlugin.rotateToCamera();
            getVRLibrary().addPlugin(pausePlugin);
        } else {
            playPlugin.rotateToCamera();
            getVRLibrary().addPlugin(playPlugin);
        }

        if (ffPlugin == null){
            MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                    .size(1f, 1f)
                    .provider(this, R.mipmap.ic_media_ff)
                    .title("ff logo")
                    .tag("ff")
                    .position(MDPosition.newInstance().setZ(-12.0f).setY(1.0f).setX(1.0f));
            ffPlugin = new MDWidgetPlugin(builder);
        }
        ffPlugin.rotateToCamera();
        getVRLibrary().addPlugin(ffPlugin);

        if (rewPlugin == null){
            MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                    .size(1f, 1f)
                    .provider(this, R.mipmap.ic_media_rew)
                    .title("rew logo")
                    .tag("rew")
                    .position(MDPosition.newInstance().setZ(-12.0f).setY(1.0f).setX(-1.0f));
            rewPlugin = new MDWidgetPlugin(builder);
        }
        rewPlugin.rotateToCamera();
        getVRLibrary().addPlugin(rewPlugin);

        isControlIconsShow = true;
    }

    private void showRePosIcon() {
        if (isRePosIconShow) {
            return;
        }
        removePlugins();
        if (rePosPlugin == null) {
            MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                    .size(1f, 1f)
                    .provider(0, this, R.mipmap.icon_location_0)
                    .provider(1, this, R.mipmap.icon_location)
                    .title("rePos logo")
                    .tag("rePos")
                    .status(0, 1)
                    .position(MDPosition.newInstance().setZ(-12.0f).setY(-1.0f));
            rePosPlugin = new MDWidgetPlugin(builder);
        }

        rePosPlugin.rotateToCamera();
        getVRLibrary().addPlugin(rePosPlugin);
        isRePosIconShow = true;
    }

    private void removePlugins() {
        if (isRePosIconShow || isControlIconsShow) {
            getVRLibrary().removePlugins();
            pointL.reset();
            pointR.reset();
            isRePosIconShow = false;
            isControlIconsShow = false;
        }
    }

    private void calSeekSec(long nowTimestamp, long hitTimestamp, boolean isFF){
        if (videoCurrentPosition == -1){
            videoCurrentPosition = getPlayer().mPlayer.getCurrentPosition();
        }
        long change = nowTimestamp - hitTimestamp - checkNeedTime;
        long temp;
        if (change < lvlUpTime){
            change = change * FAST_X;
        } else if ((temp = change - lvlUpTime) < lvlUpTime) {
            change = lvlUpTime * FAST_X + temp * FAST_X * fastXX;
        } else {
            temp = temp - lvlUpTime;
            change = lvlUpTime * FAST_X * (1 + fastXX) +  temp * FAST_X * fastXX * fastXX;;
        }

        if (!isFF){
            change = -change;
        }
        long sec = (videoCurrentPosition + change) / 1000;
        if (sec < 0) {
            sec = 0;
        } else if (sec > videoDurationSec) {
            sec = videoDurationSec;
        }
        if (sec != lastWantSeekSec){
            String curP = formatTime(sec);
            //c += (nowTimestamp - hitTimestamp)
            pointL.showText(curP + "/" + videoDurationStr);
            pointR.showText(curP + "/" + videoDurationStr);
            lastWantSeekSec = sec;
        } else {
            System.out.println("seek秒数一样，不用刷新");
        }
    }

    private String formatTime(long sec){
        String s = "0:00";
        if (sec > 0) {
            long t = sec % 60;
            s = (sec / 60) + ":" + (t > 9 ? t : ("0"+t));
        }
        return s;
    }
}
