package com.moxin.videoline.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moxin.videoline.CuckooApplication;
import com.moxin.videoline.R;
import com.moxin.videoline.audiorecord.IRecordAudioListener;
import com.moxin.videoline.audiorecord.entity.EnterRecordAudioEntity;
import com.moxin.videoline.audiorecord.util.Cons;
import com.moxin.videoline.audiorecord.util.PaoPaoTips;
import com.moxin.videoline.audiorecord.util.PermissionUtil;
import com.moxin.videoline.audiorecord.util.StringUtil;
import com.moxin.videoline.audiorecord.view.LineWaveVoiceView;
import com.moxin.videoline.audiorecord.view.RecordAudioView;
import com.moxin.videoline.event.EventBusConfig;
import com.moxin.videoline.event.VoiceRecordEvent;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * 录制语音feed页面
 */
public class AudioRecordActivity extends FragmentActivity implements
        IRecordAudioListener, View.OnClickListener, RecordAudioView.IRecordAudioListener {

    private static final String TAG = "AudioRecordActivity";
    public static final String KEY_ENTER_RECORD_AUDIO_ENTITY = "enter_record_audio";
    public static final String KEY_AUDIO_BUNDLE = "audio_bundle";
    public static final long DEFAULT_MAX_RECORD_TIME = 600000;
    public static final long DEFAULT_MIN_RECORD_TIME = 2000;
    protected static final int DEFAULT_MIN_TIME_UPDATE_TIME = 1000;

    private RecordAudioView recordAudioView;
    private String audioFileName;
    private TextView ivClose;
    private TextView tvRecordTips;
    private LinearLayout layoutCancelView;
    private String[] recordStatusDescription;
    private long maxRecordTime = DEFAULT_MAX_RECORD_TIME;
    private long minRecordTime = DEFAULT_MIN_RECORD_TIME;
    private Timer timer;
    private TimerTask timerTask;
    private Handler mainHandler;
    private long recordTotalTime;
    private EnterRecordAudioEntity entity;
    private View contentView;
    private View recordAudioContent;
    private TextView tvReplyName;
    private LineWaveVoiceView mHorVoiceView;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.pp_bottom_in, R.anim.pp_bottom_out);
        setContentView(R.layout.activity_sound_feed);

        QMUIStatusBarHelper.translucent(this); // 沉浸式状态栏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        recordAudioView = (RecordAudioView) findViewById(R.id.iv_recording);
        recordAudioView.setRecordAudioListener(this);
        ivClose = (TextView) findViewById(R.id.close_record);
        ivClose.setOnClickListener(this);
        tvRecordTips = (TextView) findViewById(R.id.record_tips);
        layoutCancelView = (LinearLayout) findViewById(R.id.pp_layout_cancel);
        contentView = findViewById(R.id.record_content);
        recordAudioContent = findViewById(R.id.layout_record_audio);
        mHorVoiceView = (LineWaveVoiceView) findViewById(R.id.horvoiceview);
        emptyView = findViewById(R.id.audio_empty_layout);
        emptyView.setOnClickListener(this);
        recordStatusDescription = new String[]{
                "按住录音",
                "上滑取消"
        };
        mainHandler = new Handler();
        Bundle bundle = getIntent().getBundleExtra(KEY_AUDIO_BUNDLE);
        entity = (EnterRecordAudioEntity) bundle.getSerializable(KEY_ENTER_RECORD_AUDIO_ENTITY);
    }

    @Override
    public boolean onRecordPrepare() {
        //检查录音权限
        if (!PermissionUtil.hasSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
            String[] pp = new String[]{
                    Manifest.permission.RECORD_AUDIO
            };
            ActivityCompat.requestPermissions(this, pp, Cons.PERMISSIONS_REQUEST_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public String onRecordStart() {
        recordTotalTime = 0;
        initTimer();
        timer.schedule(timerTask, 0, DEFAULT_MIN_TIME_UPDATE_TIME);
        audioFileName = CuckooApplication.getInstances().getExternalCacheDir() + File.separator + createAudioName();
        mHorVoiceView.startRecord();
        return audioFileName;
    }

    @Override
    public boolean onRecordStop() {
        if (recordTotalTime >= minRecordTime) {
            timer.cancel();
            onBackPressed();
            //录制完成发送EventBus通知
            switch (entity.getSourceType()) {
                case AUDIO_FEED:
                    VoiceRecordEvent event1 = new VoiceRecordEvent(EventBusConfig.SOUND_FEED_RECORD_FINISH, audioFileName);
                    EventBus.getDefault().post(event1);
                    break;
                default:
                    break;
            }
        } else {
            onRecordCancel();
        }
        return false;
    }

    @Override
    public boolean onRecordCancel() {
        if (timer != null) {
            timer.cancel();
        }
        updateCancelUi();
        return false;
    }

    private void updateCancelUi() {
        mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        layoutCancelView.setVisibility(View.INVISIBLE);
        tvRecordTips.setText(recordStatusDescription[0]);
        mHorVoiceView.stopRecord();
        deleteTempFile();
    }

    private void deleteTempFile() {
        //取消录制后删除文件
        if (audioFileName != null) {
            File tempFile = new File(audioFileName);
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 上划取消
     */
    @Override
    public void onSlideTop() {
        mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.INVISIBLE);
        layoutCancelView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFingerPress() {
        mHorVoiceView.setVisibility(View.VISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        tvRecordTips.setText(recordStatusDescription[1]);
        layoutCancelView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length <= 0
                || grantResults == null || grantResults.length <= 0) {
            return;
        }
        boolean isGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (isGranted) {
            //暂时先弹出提示,用户需要再次按下语音键
            PaoPaoTips.showDefault(this, "请再次按下语音键");
        } else {
            PaoPaoTips.showDefault(this, "您拒绝了录音权限,请在设置中打开");
        }
        updateCancelUi();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_record) {
            onBackPressed();
        } else if (v.getId() == R.id.audio_empty_layout) {
            onBackPressed();
        }
    }

    /**
     * 初始化计时器用来更新倒计时
     */
    private void initTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //每隔1000毫秒更新一次ui
                        recordTotalTime += 1000;
                        updateTimerUI();
                    }
                });
            }
        };
    }

    private void updateTimerUI() {
        if (recordTotalTime >= maxRecordTime) {
            recordAudioView.invokeStop();
        } else {
            String string = String.format(" 倒计时 %s ", StringUtil.formatRecordTime(recordTotalTime, maxRecordTime));
            mHorVoiceView.setText(string);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.pp_bottom_in, R.anim.pp_bottom_out);
    }

    public String createAudioName() {
        long time = System.currentTimeMillis();
        String fileName = UUID.randomUUID().toString() + time + ".aac";
        return fileName;
    }
}
