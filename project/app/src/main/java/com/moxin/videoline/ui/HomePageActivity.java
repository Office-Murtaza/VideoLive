package com.moxin.videoline.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.moxin.videoline.R;
import com.moxin.videoline.adapter.CuckooEvaluateAdapter;
import com.moxin.videoline.adapter.recycler.RecycleUserHomeGiftAdapter;
import com.moxin.videoline.adapter.recycler.RecycleUserHomePhotoAdapter;
import com.moxin.videoline.adapter.recycler.RecycleUserHomeVideoAdapter;
import com.moxin.videoline.api.Api;
import com.moxin.videoline.base.BaseActivity;
import com.moxin.videoline.helper.SelectResHelper;
import com.moxin.videoline.inter.AdapterOnItemClick;
import com.moxin.videoline.inter.JsonCallback;
import com.moxin.videoline.json.JsonDoRequestGetEvaluateList;
import com.moxin.videoline.json.JsonRequest;
import com.moxin.videoline.json.JsonRequestBase;
import com.moxin.videoline.json.JsonRequestGetInviteCode;
import com.moxin.videoline.json.JsonRequestTarget;
import com.moxin.videoline.json.jsonmodle.TargetUserData;
import com.moxin.videoline.json.jsonmodle.VideoModel;
import com.moxin.videoline.manage.RequestConfig;
import com.moxin.videoline.manage.SaveData;
import com.moxin.videoline.modle.CuckooUserEvaluateListModel;
import com.moxin.videoline.ui.common.Common;
import com.moxin.videoline.utils.QRCodeUtil;
import com.moxin.videoline.utils.SimpleUtils;
import com.moxin.videoline.utils.StringUtils;
import com.moxin.videoline.utils.Utils;
import com.moxin.videoline.utils.im.IMHelp;
import com.moxin.videoline.widget.BGLevelTextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.sns.TIMFriendResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import okhttp3.Call;
import okhttp3.Response;

/**
 * player??????
 */

public class HomePageActivity extends BaseActivity implements View.OnClickListener, AdapterOnItemClick {

    @BindView(R.id.rv_evaluate)
    RecyclerView rv_evaluate;

    //??????
    private TextView shareUserMsg;//????????????
    private LinearLayout divideNumber;//????????????
    private ImageView userIsonLine;//??????????????????
    private SwipeRefreshLayout homePageRefreshLayout;//??????????????????
    private TextView videoRecommend;


    //??????
    private TextView maxLevelText;//??????????????????
    private TextView userNickname; //??????player??????
    private TextView userTimeText; //??????x??????
    private TextView userGoodText;//??????x%
    private TextView userLocationText;//??????player??????
    private TextView userIsonLineText;//??????player????????????
    private TextView loveNumber;//????????????
    private TextView fansNumber;//????????????
    private TextView listBarGiftText;//????????????
    private TextView listBarVideoText;//???????????????
    private TextView listBarPhotoText;//????????????
    private TextView tvVideoMoney;
    private TextView tv_id;

    private BGLevelTextView tv_level;

    private ImageView userIsattestation;//????????????

    private RecyclerView listBarGiftList;//????????????
    private RecyclerView listBarVideoList;//???????????????
    private RecyclerView listBarPhotoList;//????????????

    //??????
    private List<VideoModel> videos = new ArrayList<>();

    //?????????
    private ArrayList<String> rollPath = new ArrayList<>();

    //?????????????????????
    private ArrayList<TargetUserData.GiftBean> giftList = new ArrayList<>();
    //???????????????list
    private ArrayList<VideoModel> videoList = new ArrayList<>();
    //???????????????list
    private ArrayList<TargetUserData.PicturesBean> photoList = new ArrayList<>();

    private List<CuckooUserEvaluateListModel> listCuckooEvaluate = new ArrayList<>();

    //??????
    private TextView userLoveme;//????????????player

    //else
    private TargetUserData targetUserData;//????????????????????????

    private String targetUserId;//????????????id

    private Dialog dialog;

    //??????????????????????????????
    private RecycleUserHomeGiftAdapter recycleUserHomeGiftAdapter;
    //????????????????????????
    private RecycleUserHomeVideoAdapter recycleUserHomeVideoAdapter;
    //????????????????????????
    private RecycleUserHomePhotoAdapter recycleUserHomePhotoAdapter;

    private CuckooEvaluateAdapter cuckooEvaluateAdapter;
    //??????view
    private View shareView;
    private View headView;

    private int evaluatePage = 1;

    @Override
    protected void initPlayerDisplayData() {
    }

    @Override
    protected Context getNowContext() {
        return HomePageActivity.this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_player_page;
    }

    @Override
    protected void initView() {

        QMUIStatusBarHelper.translucent(this); // ??????????????????

        headView = LayoutInflater.from(this).inflate(R.layout.view_home_page_head, null);

        homePageRefreshLayout = findViewById(R.id.home_page_refresh_layout);


        shareUserMsg = headView.findViewById(R.id.share_user_msg);
        userIsonLine = headView.findViewById(R.id.userinfo_bar_isonLine);
        userIsattestation = headView.findViewById(R.id.userinfo_bar_isattestation);
        userNickname = headView.findViewById(R.id.userinfo_bar_userid);
        userTimeText = headView.findViewById(R.id.userinfo_bar_time_text);
        userGoodText = headView.findViewById(R.id.userinfo_bar_good_text);
        userLocationText = headView.findViewById(R.id.userinfo_bar_location_text);
        userIsonLineText = headView.findViewById(R.id.userinfo_bar_isonLine_text);
        loveNumber = headView.findViewById(R.id.love_number);
        fansNumber = headView.findViewById(R.id.fans_number);
        divideNumber = headView.findViewById(R.id.count_divide_layout);
        listBarGiftList = headView.findViewById(R.id.list_bar_gift_list);
        listBarVideoList = headView.findViewById(R.id.list_bar_video_list);
        listBarPhotoList = headView.findViewById(R.id.list_bar_photo_list);
        listBarGiftText = headView.findViewById(R.id.list_bar_gift_text);
        listBarVideoText = headView.findViewById(R.id.list_bar_video_text);
        listBarPhotoText = headView.findViewById(R.id.list_bar_photo_text);
        userLoveme = headView.findViewById(R.id.userinfo_bar_loveme);
//        homePageWallpaper = headView.findViewById(R.id.home_page_wallpaper);
        tv_level = headView.findViewById(R.id.tv_level);
        maxLevelText = headView.findViewById(R.id.userinfo_bar_max_number);
        tvVideoMoney = headView.findViewById(R.id.tv_video_money);
        tv_id = headView.findViewById(R.id.tv_id);

        setOnclickListener(headView, new int[]{R.id.userinfo_bar_loveme, R.id.float_back, R.id.float_share, R.id.float_meun, R.id.contribution_btn});

        //??????????????????
        concealView(divideNumber);

        rv_evaluate.setLayoutManager(new LinearLayoutManager(this));
        cuckooEvaluateAdapter = new CuckooEvaluateAdapter(this, listCuckooEvaluate);
        cuckooEvaluateAdapter.addHeaderView(headView);
        rv_evaluate.setAdapter(cuckooEvaluateAdapter);


    }

    @Override
    protected void initSet() {

        //????????????
        homePageRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        homePageRefreshLayout.setRefreshing(false);
                        showToastMsg(getString(R.string.refresh_success));
                    }
                }, 1000);
            }
        });

        //??????????????????
//        //?????????????????????
//        homePageWallpaper.setImageLoader(new GlideImageLoader());
//        //??????????????????
//        homePageWallpaper.setImages(rollPath);
//        //banner?????????????????????????????????????????????
//        homePageWallpaper.start();


        //???????????????????????????
        LinearLayoutManager listGiftLayoutManage = new LinearLayoutManager(this);
        listGiftLayoutManage.setOrientation(LinearLayoutManager.HORIZONTAL);
        listBarGiftList.setLayoutManager(listGiftLayoutManage);

        recycleUserHomeGiftAdapter = new RecycleUserHomeGiftAdapter(this, giftList);
        listBarGiftList.setAdapter(recycleUserHomeGiftAdapter);

        //??????????????????????????????
        LinearLayoutManager listVideoLayoutManage = new LinearLayoutManager(this);
        listVideoLayoutManage.setOrientation(LinearLayoutManager.HORIZONTAL);
        listBarVideoList.setLayoutManager(listVideoLayoutManage);

        recycleUserHomeVideoAdapter = new RecycleUserHomeVideoAdapter(this, videoList);
        listBarVideoList.setAdapter(recycleUserHomeVideoAdapter);
        recycleUserHomeVideoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //????????????
                VideoPlayerActivity.startVideoPlayerActivity(HomePageActivity.this, videoList.get(position));
            }
        });

        //???????????????????????????
        LinearLayoutManager listPhotoLayoutManage = new LinearLayoutManager(this);
        listPhotoLayoutManage.setOrientation(LinearLayoutManager.HORIZONTAL);
        listBarPhotoList.setLayoutManager(listPhotoLayoutManage);

        recycleUserHomePhotoAdapter = new RecycleUserHomePhotoAdapter(this, photoList);
        listBarPhotoList.setAdapter(recycleUserHomePhotoAdapter);
        recycleUserHomePhotoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                PrivatePhotoActivity.startPrivatePhotoActivity(HomePageActivity.this, targetUserId, "", 0);
            }
        });
    }

    @Override
    protected void doLogout() {
    }

    @Override
    protected void initData() {
        targetUserId = getIntent().getStringExtra("str");
        tv_id.setText("ID: " + targetUserId);
        requestTargetUserData();//????????????????????????
        requestGetEvaluate();//??????????????????
    }

    private void requestGetEvaluate() {

        Api.doRequestGetEvaluate(targetUserId, evaluatePage, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                JsonDoRequestGetEvaluateList data = (JsonDoRequestGetEvaluateList) JsonRequestBase.getJsonObj(s, JsonDoRequestGetEvaluateList.class);
                if (StringUtils.toInt(data.getCode()) != 0) {
                    listCuckooEvaluate.clear();
                    listCuckooEvaluate.addAll(data.getEvaluate_list());
                    cuckooEvaluateAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @OnClick({R.id.home_page_chat_layout, R.id.home_page_video_layout, R.id.home_page_gift_layout})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //????????????player
            case R.id.userinfo_bar_loveme:
                loveThisPlayer();
                break;
            //back button
            case R.id.float_back:
                finish();
                break;
            //??????????????????
            case R.id.float_share:
                shareThisVideo();
                break;
            //????????????
            case R.id.float_meun:
                showFloatMeun();
                break;
            //???????????????
            case R.id.contribution_btn:
                showContribution();
                break;
            //????????????
            case R.id.home_page_chat_layout:
                showChatPage(false);
                break;
            //???????????????
            case R.id.home_page_video_layout:
                callThisPlayer();
                break;
            //?????????
            case R.id.home_page_gift_layout:
                showChatPage(true);
                break;
            //?????????????????????
            case R.id.join_black_list:
                clickBlack();
                dialog.dismiss();
                break;
            //?????????????????????
            case R.id.report_this_user:
                clickReport();
                dialog.dismiss();
                break;
            //????????????
            case R.id.dialog_dis:
                dialog.dismiss();
                break;
            default:
                break;
        }
    }

    //??????
    private void clickReport() {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra(ReportActivity.REPORT_USER_ID, targetUserId);
        startActivity(intent);
    }

    //??????
    private void clickBlack() {

        showLoadingDialog(getResources().getString(R.string.loading_action));
        Api.doRequestBlackUser(uId, uToken, targetUserId, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                hideLoadingDialog();
                JsonRequestBase jsonObj = JsonRequestBase.getJsonObj(s, JsonRequestBase.class);
                if (jsonObj.getCode() == 1) {
                    showToastMsg(getResources().getString(R.string.action_success));
                    if (targetUserData.getIs_black() == 1) {
                        targetUserData.setIs_black(0);
                        IMHelp.deleteBlackUser(targetUserId, new TIMValueCallBack<List<TIMFriendResult>>() {
                            @Override
                            public void onError(int i, String s) {
                                LogUtils.i("????????????????????????:" + s);
                            }

                            @Override
                            public void onSuccess(List<TIMFriendResult> timFriendResults) {
                                LogUtils.i("????????????????????????");
                            }
                        });
                    } else {
                        targetUserData.setIs_black(1);
                        IMHelp.addBlackUser(targetUserId, new TIMValueCallBack<List<TIMFriendResult>>() {
                            @Override
                            public void onError(int i, String s) {
                                LogUtils.i("??????????????????:" + s);
                            }

                            @Override
                            public void onSuccess(List<TIMFriendResult> timFriendResults) {
                                LogUtils.i("??????????????????");
                            }
                        });
                    }
                } else {
                    showToastMsg(jsonObj.getMsg());
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                hideLoadingDialog();
            }
        });
    }

    @Override
    public void onItemClick(View view, AdapterOnItemClick.ViewName viewName, final int position) {
        //?????????????????????????????????viewName???????????????item??????????????????

        VideoPlayerActivity.startVideoPlayerActivity(this, videos.get(position));
    }

    /**
     * ??????????????????????????????
     */
    private void requestTargetUserData() {
        Api.getUserData(
                targetUserId,
                uId,
                uToken,
                new JsonCallback() {
                    @Override
                    public Context getContextToJson() {
                        return getNowContext();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        super.onSuccess(s, call, response);
                        JsonRequestTarget jsonTargetUser = JsonRequestTarget.getJsonObj(s);
                        if (jsonTargetUser.getCode() == 1) {
                            targetUserData = jsonTargetUser.getData();
                            //log("targetUserData::"+targetUserData);
                            initDisplayData();
                            //requestUserPateRoll();
                        } else {
                            //????????????
                            showToastMsg(jsonTargetUser.getMsg());
                        }
                    }
                }
        );
    }

    /**
     * ??????????????????
     */
//    private void requestRecommendVideo() {
//        Api.getVideoPageList(
//                uId,
//                uToken,
//                ApiUtils.VideoType.reference,
//                CuckooApplication.getInstances().getLocation().get("lat"),
//                CuckooApplication.getInstances().getLocation().get("lng"),
//                new JsonCallback() {
//                    @Override
//                    public Context getContextToJson() {
//                        return getNowContext();
//                    }
//                    @Override
//                    public void onSuccess(String s, Call call, Response response) {
//                        JsonRequestsVideo requestObj = JsonRequestsVideo.getJsonObj(s);
//                        if (requestObj.getCode() == 1){
//                            videos = requestObj.getData();
//                            //log(videos.toString());//????????????????????????
//                        }else
//                            showToastMsg(requestObj.getMsg());
//                    }
//
//                    @Override
//                    public String returnMsg() {
//                        return "??????????????????";
//                    }
//                }
//        );
//    }

    /**
     * ???????????????????????????
     */
    private void initDisplayData() {
        //???????????????????????????
        maxLevelText.setText(targetUserData.getMax_level());
        tv_level.setLevelInfo(targetUserData.getSex(), targetUserData.getLevel());
        //????????????????????????
        userLoveme.setVisibility(StringUtils.toInt(targetUserData.getAttention()) == 1 ? View.GONE : View.VISIBLE);

        userNickname.setText(targetUserData.getUser_nickname());
        userTimeText.setText(getString(R.string.home_page_call) + targetUserData.getCall());
        userGoodText.setText(getString(R.string.home_page_good) + targetUserData.getEvaluation());
        userLocationText.setText(targetUserData.getAddress());
        tvVideoMoney.setText(targetUserData.getVideo_deduction() + RequestConfig.getConfigObj().getCurrency() + "/??????");
        userIsattestation.setImageResource(SelectResHelper.getAttestationRes(StringUtils.toInt(targetUserData.getUser_status())));
        loveNumber.setText(targetUserData.getAttention_all());
        fansNumber.setText(targetUserData.getAttention_fans());

        //????????????
        userIsonLineText.setText(StringUtils.toInt(targetUserData.getIs_online()) == 1 ? "??????" : "??????");
        userIsonLine.setBackgroundResource(SelectResHelper.getOnLineRes(StringUtils.toInt(targetUserData.getIs_online())));

        //??????--??????--??????--????????????
        listBarVideoText.setText(getString(R.string.home_page_small_video) + "(" + objToString(targetUserData.getVideo_count()) + ")");
        listBarPhotoText.setText(getString(R.string.home_page_private_photo) + "(" + objToString(targetUserData.getPictures_count()) + ")");

        if (targetUserId.equals(SaveData.getInstance().getId())) {
            //????????????
            userLoveme.setVisibility(View.GONE);
            listBarGiftText.setText(getString(R.string.home_page_send_gift) + "(" + objToString(targetUserData.getGift_count()) + ")");
        } else {
            listBarGiftText.setText(getString(R.string.home_page_received) + "(" + objToString(targetUserData.getGift_count()) + ")");
        }

        if (targetUserData.getImg() != null) {
            for (TargetUserData.ImgBean img : targetUserData.getImg()) {
                rollPath.add(Utils.getCompleteImgUrl(img.getImg()));
            }
            if (targetUserData.getImg().size() == 0) {
                rollPath.add(Utils.getCompleteImgUrl(targetUserData.getAvatar()));
            }
        }

        //???????????????????????????
        if (targetUserData.getGift() != null) {
            giftList.addAll(targetUserData.getGift());
            recycleUserHomeGiftAdapter.notifyDataSetChanged();
        }

        //???????????????????????????
        if (targetUserData.getVideo() != null) {
            videoList.addAll(targetUserData.getVideo());
            recycleUserHomePhotoAdapter.notifyDataSetChanged();
        }
        //???????????????????????????
        if (targetUserData.getPictures() != null) {
            photoList.addAll(targetUserData.getPictures());
            recycleUserHomeVideoAdapter.notifyDataSetChanged();
        }
        refreshRollView();

    }

    /**
     * ???????????????
     */
    private void refreshRollView() {
        //log("????????????");
//        homePageWallpaper.setImages(rollPath);
//        homePageWallpaper.start();
    }

    //?????????player?????????
    private void callThisPlayer() {
        showToastMsg(getString(R.string.home_page_loading_call));

        Common.callVideo(this, targetUserId,0);
    }

    //??????????????????
    private void showChatPage(boolean isShowGift) {

        Common.startPrivatePage(this, targetUserId);

    }

    //???????????????
    private void showContribution() {
        Intent intent = new Intent(this, UserContribuionRankActivity.class);
        intent.putExtra(UserContribuionRankActivity.TO_USER_ID, targetUserId);
        startActivity(intent);
    }

    //????????????
    private void showFloatMeun() {
        int[] a = {R.id.join_black_list, R.id.report_this_user, R.id.dialog_dis};
        dialog = showButtomDialog(R.layout.dialog_float_meun, a, 20);
        TextView tv = dialog.findViewById(R.id.join_black_list);
        if (targetUserData.getIs_black() == 1) {
            tv.setText(getResources().getString(R.string.relieve_black));
        }
        dialog.show();
    }

    //??????????????????
    private void shareThisVideo() {
        concealView(shareUserMsg);
        showSimpleBottomSheetGrid();
    }

    //????????????player
    private void loveThisPlayer() {
        Api.doLoveTheUser(
                targetUserId,
                uId,
                uToken,
                new JsonCallback() {
                    @Override
                    public Context getContextToJson() {
                        return getNowContext();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        super.onSuccess(s, call, response);
                        JsonRequest requestObj = JsonRequest.getJsonObj(s);
                        if (requestObj.getCode() == 1) {
                            concealView(userLoveme);//??????????????????
                            showToastMsg(getResources().getString(R.string.action_success));
                        } else {
                            log("????????????player:" + requestObj.getMsg());
                        }
                    }
                }
        );
    }

    /**
     * ?????????????????????
     */
    private void showSimpleBottomSheetGrid() {
        final int TAG_SHARE_WECHAT_FRIEND = 0;
        final int TAG_SHARE_WECHAT_MOMENT = 1;
        QMUIBottomSheet.BottomGridSheetBuilder builder = new QMUIBottomSheet.BottomGridSheetBuilder(HomePageActivity.this);
        builder.addItem(R.drawable.icon_weixin_friend, getString(R.string.share_wechat), TAG_SHARE_WECHAT_FRIEND, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.drawable.icon_weixin_moment, getString(R.string.share_pyq), TAG_SHARE_WECHAT_MOMENT, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomGridSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView) {
                        dialog.dismiss();
                        int tag = (int) itemView.getTag();
                        switch (tag) {
                            case TAG_SHARE_WECHAT_FRIEND:
                                showToastMsg(getString(R.string.share_wechat));
                                createImageText(Wechat.NAME);
                                break;
                            case TAG_SHARE_WECHAT_MOMENT:
                                showToastMsg(getString(R.string.share_pyq));
                                createImageText(WechatMoments.NAME);
                                break;
                        }
                    }
                }).build().show();
    }

    //??????????????????
    private void createImageText(final String platform) {

        Api.doGetInviteCode(SaveData.getInstance().getId(), SaveData.getInstance().getToken(), new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                JsonRequestGetInviteCode data = (JsonRequestGetInviteCode) JsonRequestBase.getJsonObj(s, JsonRequestGetInviteCode.class);
                if (data.getCode() == 1) {
                    String inviteCode = data.getInvite_code();
                    createImageText2(platform, inviteCode);
                }
            }
        });

    }

    private void createImageText2(final String platform, String inviteCode) {
        shareView = View.inflate(this, R.layout.view_share, null);

        Bitmap qrcodeBitmap = QRCodeUtil.createQRCodeBitmap(RequestConfig.getConfigObj().getInviteShareRegUrl() + "?invite_code=" + inviteCode, ConvertUtils.dp2px(150), ConvertUtils.dp2px(150));
        ImageView qrCodeImg = shareView.findViewById(R.id.iv_code);
        qrCodeImg.setImageBitmap(qrcodeBitmap);

        final TextView name = shareView.findViewById(R.id.tv_name);
        name.setText(targetUserData.getUser_nickname());
        ImageView avatar = (ImageView) shareView.findViewById(R.id.iv_avatar);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setLayoutParams(new RelativeLayout.LayoutParams(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenWidth()));
        Utils.loadHttpImg(Utils.getCompleteImgUrl(targetUserData.getAvatar()), avatar);

        // ???????????????????????????view?????????????????????????????????????????????????????????
        SimpleUtils.layoutView(shareView, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());


//        ImageView imageView = new ImageView(this);
//        imageView.setImageBitmap(cacheBitmapFromView);
//        Dialog dialog = new Dialog(this);
//        dialog.setContentView(imageView);
//        dialog.show();


        tvVideoMoney.postDelayed(new Runnable() {
            @Override
            public void run() {
                // View????????????
                Bitmap cacheBitmapFromView = SimpleUtils.getCacheBitmapFromView(shareView);

                File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    cacheBitmapFromView.compress(Bitmap.CompressFormat.JPEG, 30, out);
                    //System.out.println("___________?????????__sd___???_______________________");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showShare(file.getPath(), platform);
            }
        }, 3000);
    }


    private void showShare(String file, String platform) {
        final OnekeyShare oks = new OnekeyShare();
        //????????????????????????????????????????????????????????????????????????????????????
        if (platform != null) {
            oks.setPlatform(platform);
        }
        //??????sso??????
        oks.disableSSOWhenAuthorize();
        // title???????????????????????????????????????????????????????????????QQ????????????
        //oks.setTitle("??????");
        // titleUrl?????????????????????????????????Linked-in,QQ???QQ????????????
        //oks.setTitleUrl("http://sharesdk.cn");
        // text???????????????????????????????????????????????????
        //oks.setText("??????????????????");
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        // imagePath???????????????????????????Linked-In?????????????????????????????????
        oks.setImagePath(file);//??????SDcard????????????????????????
        // url???????????????????????????????????????????????????
        //oks.setUrl("http://sharesdk.cn");
        // comment???????????????????????????????????????????????????QQ????????????
        //oks.setComment("????????????????????????");
        // site??????????????????????????????????????????QQ????????????
        //oks.setSite("ShareSDK");
        // siteUrl??????????????????????????????????????????QQ????????????
        //oks.setSiteUrl("http://sharesdk.cn");

        //????????????
        oks.show(this);
    }
}