package com.moxin.videoline.ui;

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
import com.moxin.videoline.base.BaseActivity;
import com.moxin.videoline.modle.SharePosterBean;
import com.moxin.videoline.ui.dialog.InviteShareView;
import com.moxin.videoline.utils.ImageUtils;
import com.moxin.videoline.utils.QRCodeUtil;
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

public class PosterActivity extends BaseActivity implements CuckooShareDialogView.CuckooShareDialogViewCallback {

    @BindView(R.id.mSharePagerView)
    XBanner viewPager;

    @BindView(R.id.share_view)
    InviteShareView shareView;

    @BindView(R.id.share_poster_btn)
    TextView shareBtn;
    private int selectPosi = 0;
    private Bitmap selectBitMap;

    private List<SharePosterBean.DataBean> posterList;


    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_poster;
    }

    @Override
    protected boolean hasTopBar() {
        return true;
    }

    @Override
    protected void initView() {
        getTopBar().setTitle("????????????");

        shareView.setCallback(PosterActivity.this);
        shareView.showSaveBt(true);

    }

    List<Bitmap> bitmapList = new ArrayList<>();

    /**
     * @param bannerImgBean
     */
    private void creatQr(final SharePosterBean.DataBean bannerImgBean) {

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(this)
                .asBitmap()
                .load(bannerImgBean.getImg())
                .apply(options)
                .into(new SimpleTarget<Bitmap>(480, 800) {
                    @Override
                    public void onResourceReady(Bitmap bg, Transition<? super Bitmap> transition) {
                        Bitmap bm_qr = QRCodeUtil.createQRCodeBitmap(bannerImgBean.getImg(), 150, 150);
                        Bitmap newBitmap = null;
                        newBitmap = Bitmap.createBitmap(bg);
                        Canvas canvas = new Canvas(newBitmap);
                        int w = bm_qr.getWidth();
                        int h = bm_qr.getHeight();
                        int w_2 = bg.getWidth();
                        int h_2 = bg.getHeight();
                        Paint paint = new Paint();
                        canvas.drawBitmap(bm_qr, Math.abs(w - w_2) / 2,
                                Math.abs(h - h_2) / 2 + 230, paint);
                        //canvas.save(Canvas.ALL_SAVE_FLAG);//2020.09.02.zad
                        canvas.save();
                        // ????????????????????????
                        canvas.restore();
                        Log.e("getShareInfo_img", bannerImgBean.getImg() + "????????????");
                        bitmapList.add(newBitmap);

                        if (bitmapList.size() == posterList.size()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initXbanner();
                                }
                            });
                        }
                    }
                });


//        Utils.loadHttpImg(bannerImgBean.getImg(), tvContent);
    }


    @Override
    protected void initSet() {

    }

    @Override
    protected void initData() {
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

    private void initXbanner() {

        //?????????????????????
        viewPager.loadImage(new XBanner.XBannerAdapter() {
            @Override
            public void loadBanner(XBanner banner, Object model, View view, int position) {
                ImageView tvContent = (ImageView) view.findViewById(R.id.custom_imageview_layout);
                SharePosterBean.DataBean bannerImgBean = (SharePosterBean.DataBean) model;
                Log.e("getShareInfo", bannerImgBean.getImg());

                tvContent.setImageBitmap(bitmapList.get(position));

            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectPosi = position;
//                ToastUtils.showShort(selectPosi+"");
                selectBitMap = bitmapList.get(selectPosi);
                shareView.setVisibility(View.GONE);
                shareBtn.setVisibility(View.VISIBLE);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        viewPager.setBannerData(R.layout.banner_custom_layout, posterList);
        viewPager.startAutoPlay();

    }

    @Override
    public void onBackPressed() {
        if (shareView.getVisibility() == View.VISIBLE) {
            shareView.setVisibility(View.GONE);
            shareBtn.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }

    }

    @OnClick({R.id.share_poster_rl})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_poster_rl:
                shareView.setVisibility(View.VISIBLE);
                shareBtn.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        viewPager.stopAutoPlay();
    }

    @Override
    protected void initPlayerDisplayData() {

    }


    /**
     * ????????????
     */
    @Override
    public void onClickWeChat() {
        showShare(Wechat.NAME);
    }

    @Override
    public void onClickQQ() {

        showShare(QQ.NAME);
    }

    @Override
    public void onClickQrcode() {
        ImageUtils.saveImageToGallery(this, selectBitMap);
        ToastUtils.showLong("???????????????");
    }

    @Override
    public void onClickPyq() {
        showShare(WechatMoments.NAME);
    }

    @Override
    public void copyUrl() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // ??????????????????????????????????????????

        cm.setText(posterList.get(selectPosi).getInvite_url());
        ToastUtils.showLong("??????????????????????????????????????????");
    }


    private void showShare(String platform) {
        OnekeyShare oks = new OnekeyShare();
        //????????????????????????????????????????????????????????????????????????????????????
        if (platform != null) {
            oks.setPlatform(platform);
        }
        //??????sso??????
        oks.disableSSOWhenAuthorize();
//        if (!isImg) {
//            // title???????????????????????????????????????????????????????????????QQ????????????
//            oks.setTitle(s_title);
//            // titleUrl?????????????????????????????????Linked-in,QQ???QQ????????????
//            oks.setTitleUrl(shareUrl);
//            // text???????????????????????????????????????????????????
//            oks.setText(s_content);
//            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//            //oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
//            // imagePath???????????????????????????Linked-In?????????????????????????????????
//            //oks.setImagePath(img);//??????SDcard????????????????????????
//            oks.setImageUrl(img);
//            // url???????????????????????????????????????????????????
//            oks.setUrl(shareUrl);
//            // comment???????????????????????????????????????????????????QQ????????????
//            //oks.setComment("????????????????????????");
//            // site??????????????????????????????????????????QQ????????????
//            //oks.setSite("ShareSDK");
//            // siteUrl??????????????????????????????????????????QQ????????????
//            //oks.setSiteUrl("http://sharesdk.cn");
//        } else

        oks.setImagePath(ImageUtils.saveUserQr(this, selectBitMap));

        //????????????
        oks.show(this);
    }

}
