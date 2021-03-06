package com.moxin.videoline.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.moxin.video.videoupload.TXUGCPublish;
import com.moxin.video.videoupload.TXUGCPublishTypeDef;
import com.moxin.videoline.CuckooApplication;
import com.moxin.videoline.LiveConstant;
import com.moxin.videoline.R;
import com.moxin.videoline.api.Api;
import com.moxin.videoline.base.BaseActivity;
import com.moxin.videoline.event.CuckooPushVideoCommonEvent;
import com.moxin.videoline.json.JsonRequestAddShortVideo;
import com.moxin.videoline.json.JsonRequestBase;
import com.moxin.videoline.json.JsonRequestCheckVideoCoinRange;
import com.moxin.videoline.modle.ConfigModel;
import com.moxin.videoline.utils.CuckooQiniuFileUploadUtils;
import com.moxin.videoline.utils.StringUtils;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class PushShortVideoActivity extends BaseActivity implements TXUGCPublishTypeDef.ITXVideoPublishListener {

    private TXCloudVideoView mVideoView;
    public static final String VIDEO_PATH = "VIDEO_PATH";
    public static final String VIDEO_COVER_PATH = "VIDEO_COVER_PATH";

    private Button mBtnChangePay;
    private EditText mEtInputMoney;
    private Button mBtnPushVideo;
    private EditText mEtTitle;

    private String videoPath;
    private String coverPath;
    private boolean isPay = false;

    private String uploadFileVideoCoverThumbUrl;
    private String uploadFileVideoUrl;
    private TXVodPlayer txVodPlayer;
    private int state;
    private CuckooQiniuFileUploadUtils cuckooQiniuFileUploadUtils;

    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_push_short_video;
    }

    @Override
    protected void initView() {
        QMUIStatusBarHelper.translucent(this);
        mBtnChangePay = findViewById(R.id.btn_pay);
        mEtInputMoney = findViewById(R.id.et_money);
        mEtTitle = findViewById(R.id.et_title);
        mBtnPushVideo = findViewById(R.id.btn_push);
        mBtnChangePay.setOnClickListener(this);
        mBtnPushVideo.setOnClickListener(this);
        mVideoView = findViewById(R.id.video_view);

        findViewById(R.id.iv_back).setOnClickListener(this);

    }

    private void initLive() {

        txVodPlayer = new TXVodPlayer(getNowContext());
        txVodPlayer.setPlayerView(mVideoView);

        txVodPlayer.setPlayListener(new ITXLivePlayListener() {
            @Override
            public void onPlayEvent(int i, Bundle bundle) {
                if (i == TXLiveConstants.PLAY_EVT_PLAY_END) {
                    txVodPlayer.startPlay(videoPath);
                }
            }

            @Override
            public void onNetStatus(Bundle bundle) {

            }
        });
        txVodPlayer.startPlay(videoPath);

    }

    @Override
    protected void initSet() {
        videoPath = getIntent().getStringExtra(VIDEO_PATH);
        coverPath = getIntent().getStringExtra(VIDEO_COVER_PATH);

        initLive();
    }

    @Override
    protected void initData() {
        cuckooQiniuFileUploadUtils = new CuckooQiniuFileUploadUtils();

    }

    @Override
    protected void initPlayerDisplayData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

//            case R.id.btn_pay:
//                isPay = !isPay;
//
//                if (isPay) {
//                    mEtInputMoney.setVisibility(View.VISIBLE);
//                    mBtnChangePay.setTextColor(getResources().getColor(R.color.admin_color));
//                    mBtnChangePay.setBackgroundResource(R.drawable.btn_push_short_video_pay_select);
//                } else {
//                    mEtInputMoney.setVisibility(View.GONE);
//                    mBtnChangePay.setTextColor(getResources().getColor(R.color.white));
//                    mBtnChangePay.setBackgroundResource(R.drawable.btn_push_short_video_pay);
//                }
//                break;
            case R.id.btn_push:

                clickPushVideo();
                break;
            case R.id.iv_back:

                finish();
                break;
        }
    }

    private void clickPushVideo() {

        showLoadingDialog(getString(R.string.test_text));
        String money = mEtInputMoney.getText().toString();
        Api.doCheckVideoCoinRange(money, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                hideLoadingDialog();
                JsonRequestCheckVideoCoinRange data = (JsonRequestCheckVideoCoinRange) JsonRequestBase.getJsonObj(s, JsonRequestCheckVideoCoinRange.class);
                if (data.getCode() == 1) {
                    pushVideo();
                } else {
                    ToastUtils.showShort(data.getMsg());
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                hideLoadingDialog();
            }
        });
    }

    private void pushVideo() {

        String money = mEtInputMoney.getText().toString();
        String title = mEtTitle.getText().toString();

        if (TextUtils.isEmpty(title)) {
            showToastMsg(getString(R.string.please_full_video_title));
            return;
        }
        if (isPay && StringUtils.toInt(money) == 0) {
            showToastMsg(getString(R.string.please_full_video_money));
            return;
        }

        try {
            MediaPlayer mediaPalyer = new MediaPlayer();
            mediaPalyer.setDataSource(videoPath);
            mediaPalyer.prepare();
            long time = mediaPalyer.getDuration();
            if (time / 1000 > StringUtils.toInt(ConfigModel.getInitData().getUpload_short_video_time_limit())) {
                ToastUtils.showLong("??????????????????" + ConfigModel.getInitData().getUpload_short_video_time_limit() + "???");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        showLoadingDialog(getString(R.string.loading_now_upload_video));
        uploadVideoThumb();

    }

    //??????????????????
    private void uploadVideoThumb() {

        File file = new File(coverPath);
        //????????????????????????key
        //final String upkey = LiveConstant.VIDEO_COVER_IMG_DIR + MD5.toMD5(System.currentTimeMillis() + "_" + file.getName()) + ".mp4";

        List<File> thumbList = new ArrayList<>();
        thumbList.add(file);
        cuckooQiniuFileUploadUtils.uploadFile(LiveConstant.VIDEO_COVER_IMG_DIR, thumbList, new CuckooQiniuFileUploadUtils.CuckooQiniuFileUploadCallback() {
            @Override
            public void onUploadFileSuccess(List<String> fileUrlList) {
                if (fileUrlList.size() > 0) {
                    uploadFileVideoCoverThumbUrl = fileUrlList.get(0);
                    uploadVideo();
                } else {
                    ToastUtils.showLong("?????????????????????");
                    hideLoadingDialog();
                }
            }
        });
    }

    //????????????
    private void uploadVideo() {

        File file = new File(videoPath);
        //????????????????????????key
        //final String upkey = LiveConstant.VIDEO_DIR + System.currentTimeMillis() + "_" + file.getName();

        List<File> videoList = new ArrayList<>();
        videoList.add(file);
        cuckooQiniuFileUploadUtils.uploadFile(LiveConstant.VIDEO_DIR, videoList, new CuckooQiniuFileUploadUtils.CuckooQiniuFileUploadCallback() {
            @Override
            public void onUploadFileSuccess(List<String> fileUrlList) {
                hideLoadingDialog();
                if (fileUrlList.size() > 0) {
                    uploadFileVideoUrl = fileUrlList.get(0);
                    requestAddShortVideo();
                } else {
                    ToastUtils.showLong("?????????????????????");
                }
            }
        });
    }

    /**
     * ??????????????????
     */
    private void requestAddShortVideo() {

        String money = mEtInputMoney.getText().toString();
        String title = mEtTitle.getText().toString();

        HashMap<String, String> local = CuckooApplication.getInstances().getLocation();

        String lat = "", lng = "";
        if (local.get("lat") != null) {
            lat = local.get("lat");
        }
        if (local.get("lng") != null) {
            lng = local.get("lng");
        }

        if (StringUtils.toInt(money) > 0) {
            state = 2;
        } else {
            state = 1;
        }
        showLoadingDialog(getString(R.string.loading_now_upload_video));
        Api.doUploadShortVideo(uId, uToken, state, money, title, "1111111", uploadFileVideoUrl, uploadFileVideoCoverThumbUrl, lat, lng, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                hideLoadingDialog();
                JsonRequestAddShortVideo jsonObj = (JsonRequestAddShortVideo) JsonRequestAddShortVideo.getJsonObj(s, JsonRequestAddShortVideo.class);
                if (jsonObj.getCode() == 1) {
                    showToastMsg(getString(R.string.upload_success));
                    finish();
                } else {
                    showToastMsg(jsonObj.getMsg());
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                hideLoadingDialog();
            }
        });
    }

    /**
     * ????????????????????????
     */
    private void doUploadFile(String sign) {

        showLoadingDialog(getString(R.string.loading_now_upload_video));

        TXUGCPublish mVideoPublish = new TXUGCPublish(PushShortVideoActivity.this.getApplicationContext());
        // ???????????????????????????????????????
        TXUGCPublishTypeDef.TXPublishParam param = new TXUGCPublishTypeDef.TXPublishParam();
        param.signature = sign;                       // ?????????????????????????????????????????????
        // ?????????????????????????????????, ITXVideoRecordListener ??? onRecordComplete ?????????????????????
        param.videoPath = videoPath;
        // ???????????????????????????????????????ITXVideoRecordListener ??? onRecordComplete ?????????????????????
        param.coverPath = coverPath;
        mVideoPublish.publishVideo(param);
        mVideoPublish.setListener(this);
    }

    @Override
    public void onPublishProgress(long uploadBytes, long totalBytes) {

        LogUtils.i("????????????:" + uploadBytes + "?????????:" + totalBytes);

    }

    @Override
    public void onPublishComplete(TXUGCPublishTypeDef.TXPublishResult result) {

        switch (result.retCode) {
            case TXUGCPublishTypeDef.PUBLISH_RESULT_OK:

                //requestAddShortVideo(result.videoURL, result.coverURL, result.videoId);
                break;

            default:
                showToastMsg(result.descMsg);
                break;
        }

        CuckooPushVideoCommonEvent event = new CuckooPushVideoCommonEvent();
        EventBus.getDefault().post(event);

        LogUtils.i("????????????:code" + result.retCode + "msg:" + result.descMsg);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            txVodPlayer.stopPlay(false);
            mVideoView.onDestroy();
        }
    }
}
