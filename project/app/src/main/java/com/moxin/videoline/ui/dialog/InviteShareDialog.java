package com.moxin.videoline.ui.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.moxin.videoline.R;
import com.moxin.videoline.api.Api;
import com.moxin.videoline.manage.SaveData;
import com.moxin.videoline.modle.ConfigModel;
import com.moxin.videoline.modle.SharePosterBean;
import com.moxin.videoline.utils.ImageUtils;
import com.moxin.videoline.utils.QRCodeUtil;
import com.moxin.videoline.utils.Utils;
import com.moxin.videoline.widget.CuckooShareDialogView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.lzy.okgo.callback.StringCallback;
import com.stx.xhb.xbanner.XBanner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import okhttp3.Call;
import okhttp3.Response;

public class InviteShareDialog extends BaseDialog implements CuckooShareDialogView.CuckooShareDialogViewCallback {

    private int selectPosi;
    private Bitmap selectBitMap;
    private String shareUrl;

    public InviteShareDialog(Context context) {
        super(context);
    }

    @BindView(R.id.show_l)
    View tab_left;
    @BindView(R.id.show_r)
    View tab_right;
    @BindView(R.id.url_icon)
    ImageView icon;
    @BindView(R.id.url_title)
    TextView title;
    @BindView(R.id.url_content)
    TextView content;

    @BindView(R.id.share_card)
    ImageView share_card;

    @BindView(R.id.mSharePagerView)
    XBanner xBanner;

    @BindView(R.id.share_url)
    View share_url;
    @BindView(R.id.share_view)
    InviteShareView share_view;


    List<Bitmap> bitmapList = new ArrayList<>();
    private List<SharePosterBean.DataBean> posterList;

    @Override
    public int setRes() {
        return R.layout.invite_share_dialog;
    }


    @Override
    public void init() {
        super.init();
        setTrans();
//        getShareInfo();
        setImg(SaveData.getInstance().getUserInfo().getAvatar());
        setShareUrl(Utils.getShareExtensionUrl());
        title.setText(ConfigModel.getInitData().getShare_title());
        content.setText(ConfigModel.getInitData().getShare_content());

        s_title = ConfigModel.getInitData().getShare_title();
        s_content = ConfigModel.getInitData().getShare_content();

        Utils.loadUserIcon(img, icon);
        share_view.setCallback(InviteShareDialog.this);


        getSharePosterData();
    }


    private void getSharePosterData() {
        Api.getShareInfo(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                Log.e("getShareInfos", s);
                SharePosterBean bean = new Gson().fromJson(s, SharePosterBean.class);
                if (bean.getCode() == 1) {
                    posterList = bean.getData();
                    initBitmap();

                } else {
                    ToastUtils.showShort(bean.getMsg());
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
            }
        });
    }

    private void initBitmap() {
        for (int i = 0; i < posterList.size(); i++) {
            creatQr(posterList.get(i));
        }
    }


    @OnClick({R.id.click_l, R.id.click_r})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.click_l:
                tab_left.setVisibility(View.VISIBLE);
                tab_right.setVisibility(View.GONE);
                xBanner.setVisibility(View.GONE);
                share_url.setVisibility(View.VISIBLE);
                share_view.showSaveBt(false);
                isImg = false;
                break;

            case R.id.click_r:
                tab_left.setVisibility(View.GONE);
                tab_right.setVisibility(View.VISIBLE);
                xBanner.setVisibility(View.VISIBLE);
                share_url.setVisibility(View.GONE);
                share_view.showSaveBt(true);
                isImg = true;
                //viewConversionBitmap();
                break;

        }
    }

    private void creatQr(final SharePosterBean.DataBean bannerImgBean) {

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(context)
                .asBitmap()
                .load(Utils.getCompleteImgUrl(bannerImgBean.getImg()))
                .apply(options)
                .into(new SimpleTarget<Bitmap>(480, 800) {
                    @Override
                    public void onResourceReady(Bitmap bg, Transition<? super Bitmap> transition) {
                        Bitmap bm_qr = QRCodeUtil.createQRCodeBitmap(shareUrl, 190, 190);
                        Bitmap newBitmap = null;
                        newBitmap = Bitmap.createBitmap(bg);
                        Canvas canvas = new Canvas(newBitmap);
                        int w = bm_qr.getWidth();
                        int h = bm_qr.getHeight();
                        int w_2 = bg.getWidth();
                        int h_2 = bg.getHeight();
                        Paint paint = new Paint();
                        canvas.drawBitmap(bm_qr, Math.abs(w - w_2) / 2,
                                Math.abs(h - h_2) / 2 + 220, paint);
                        //canvas.save(Canvas.ALL_SAVE_FLAG);//2020.09.02.zad
                        canvas.save();
                        // ????????????????????????
                        canvas.restore();
                        Log.e("getShareInfo_img", shareUrl + "????????????");
                        Log.e("getShareInfo_img_url: ", Utils.getCompleteImgUrl(bannerImgBean.getImg()) );
                        bitmapList.add(newBitmap);

                        if (bitmapList.size() == posterList.size()) {
                            initXbanner();
                        }
                    }
                });


//        Utils.loadHttpImg(bannerImgBean.getImg(), tvContent);
    }


    private void initXbanner() {
        selectBitMap = bitmapList.get(0);
        //?????????????????????
        xBanner.loadImage(new XBanner.XBannerAdapter() {
            @Override
            public void loadBanner(XBanner banner, Object model, View view, int position) {
                ImageView tvContent = (ImageView) view.findViewById(R.id.custom_imageview_layout);
                SharePosterBean.DataBean bannerImgBean = (SharePosterBean.DataBean) model;
                Log.e("getShareInfo", bannerImgBean.getImg());

                tvContent.setImageBitmap(bitmapList.get(position));

            }
        });

        xBanner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectPosi = position;
//                ToastUtils.showShort(selectPosi+"");
                selectBitMap = bitmapList.get(selectPosi);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        xBanner.setBannerData(R.layout.invite_custom_layout, posterList);
        xBanner.startAutoPlay();

    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }


    @Override
    public void onClickWeChat() {
        showShare(Wechat.NAME);
    }

    @Override
    public void onClickPyq() {

        showShare(WechatMoments.NAME);
    }

    @Override
    public void copyUrl() {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // ??????????????????????????????????????????
        cm.setText(shareUrl);
        ToastUtils.showLong("??????????????????????????????????????????");
    }


    @Override
    public void onClickQQ() {
        showShare(QQ.NAME);
    }

    @Override
    public void onClickQrcode() {
        ImageUtils.saveImageToGallery(context, selectBitMap);
        //MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "???????????????", "description");
        ToastUtils.showLong("???????????????");
    }


    String img;
    boolean isImg = false;

    private void showShare(String platform) {
        OnekeyShare oks = new OnekeyShare();
        //????????????????????????????????????????????????????????????????????????????????????
        if (platform != null) {
            oks.setPlatform(platform);
        }
        //??????sso??????
        oks.disableSSOWhenAuthorize();
        if (!isImg) {
            // title???????????????????????????????????????????????????????????????QQ????????????
            oks.setTitle(s_title);
            // titleUrl?????????????????????????????????Linked-in,QQ???QQ????????????
            oks.setTitleUrl(shareUrl);
            // text???????????????????????????????????????????????????
            oks.setText(s_content);
            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            //oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
            // imagePath???????????????????????????Linked-In?????????????????????????????????
            //oks.setImagePath(img);//??????SDcard????????????????????????
            oks.setImageUrl(img);
            // url???????????????????????????????????????????????????
            oks.setUrl(shareUrl);
            // comment???????????????????????????????????????????????????QQ????????????
            //oks.setComment("????????????????????????");
            // site??????????????????????????????????????????QQ????????????
            //oks.setSite("ShareSDK");
            // siteUrl??????????????????????????????????????????QQ????????????
            //oks.setSiteUrl("http://sharesdk.cn");
        } else{
            oks.setImagePath(ImageUtils.saveUserQr(context, selectBitMap));
        }

        //????????????
        oks.show(context);
    }


    public void setImg(String img) {
        this.img = img;
    }

    String s_title;
    String s_content;

    public void setVideo(String title, String img, String url) {
        s_title = title;
        this.img = img;
        shareUrl = url;
        this.title.setText(s_title);
        Utils.loadUserIcon(img, icon);
    }
}
