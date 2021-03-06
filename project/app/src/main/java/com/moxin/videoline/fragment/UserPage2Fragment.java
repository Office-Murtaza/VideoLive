package com.moxin.videoline.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.moxin.videoline.ApiConstantDefine;
import com.moxin.videoline.R;
import com.moxin.videoline.api.Api;
import com.moxin.videoline.api.ApiUtils;
import com.moxin.videoline.base.BaseFragment;
import com.moxin.videoline.helper.SelectResHelper;
import com.moxin.videoline.json.JsonRequestUserCenterInfo;
import com.moxin.videoline.json.jsonmodle.UserCenterData;
import com.moxin.videoline.manage.RequestConfig;
import com.moxin.videoline.manage.SaveData;
import com.moxin.videoline.modle.ConfigModel;
import com.moxin.videoline.modle.UserModel;
import com.moxin.videoline.msg.ui.AboutFansActivity;
import com.moxin.videoline.ui.CuckooAuthFormActivity;
import com.moxin.videoline.ui.EditActivity;
import com.moxin.videoline.ui.HomePageActivity;
import com.moxin.videoline.ui.PrivatePhotoActivity;
import com.moxin.videoline.ui.RechargeActivity;
import com.moxin.videoline.ui.SettingActivity;
import com.moxin.videoline.ui.ShortVideoActivity;
import com.moxin.videoline.ui.ToJoinActivity;
import com.moxin.videoline.ui.WealthActivity;
import com.moxin.videoline.ui.WebViewActivity;
import com.moxin.videoline.ui.common.LoginUtils;
import com.moxin.videoline.utils.StringUtils;
import com.moxin.videoline.utils.Utils;
import com.moxin.videoline.widget.ForScrollViewGridView;
import com.moxin.videoline.widget.GradeShowLayout;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

/**
 * ??????
 */
public class UserPage2Fragment extends BaseFragment {
    private QMUITopBar mTopBar;
    private FrameLayout buyOne, buyTwo, buyThree;
    private RelativeLayout userpageMyuserpage, userpageMoneyBtn;
    private ForScrollViewGridView buyGrid;
    private SimpleAdapter simpleAdapter;
    private String[] titles = {
            "??????",
            "?????????",
            "??????",
            "????????????",
            "????????????",
            "????????????",
            "??????/????????????",
            "??????",
    };
    private int[] imgRess = {
            R.drawable.icon_video_verify,
            R.drawable.icon_small_video,
            R.drawable.icon_private_photo,
            R.drawable.icon_invite,
            R.drawable.icon_new_guide,
            R.drawable.icon_mine_level,
            R.drawable.icon_cooperation,
            R.drawable.icon_setting,

    };
    private Map<String, Object> map;
    private List<Map<String, Object>> list;

    private FrameLayout gradesLayout;

    private UserCenterData userCenterData;//??????????????????????????????

    private Dialog radioDialog;//????????????dialog

    //????????????
    private CircleImageView userImg;//????????????
    private TextView userName;//?????????
    private ImageView userIsVerify;//????????????????????????

    private TextView aboutNumber;//????????????
    private TextView fansNumber;//?????????
    private TextView ratioNumber;//????????????

    private TextView moneyNumber;//?????????
    private TextView oneMoney;//?????????????????????
    private TextView oneMoneyTo;//???????????????????????????????????????
    private TextView twoMoney;//?????????????????????
    private TextView twoMoneyTo;//???????????????????????????????????????
    private TextView threeMoney;//?????????????????????
    private TextView threeMoneyTo;//???????????????????????????????????????
    private TextView tvMoreChargeRule;//??????????????????
    private TextView userpage_rechargetext;


    ////////////////////////////////////////////???????????????////////////////////////////////////////////
    @Override
    protected View getBaseView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_user_page2, container, false);
    }

    @Override
    protected void initView(View view) {
        userImg = view.findViewById(R.id.userpage_img);
        userName = view.findViewById(R.id.userpage_nickname);
        userIsVerify = view.findViewById(R.id.userpage_isattestation);
        aboutNumber = view.findViewById(R.id.love_number);
        fansNumber = view.findViewById(R.id.fans_number);
        ratioNumber = view.findViewById(R.id.divide_number);
        moneyNumber = view.findViewById(R.id.userpage_money_number);
        oneMoney = view.findViewById(R.id.rechargeSellNumber);
        oneMoneyTo = view.findViewById(R.id.rechargeBuyNumber);
        twoMoney = view.findViewById(R.id.rechargeSellNumber2);
        twoMoneyTo = view.findViewById(R.id.rechargeBuyNumber2);
        threeMoney = view.findViewById(R.id.rechargeSellNumber3);
        threeMoneyTo = view.findViewById(R.id.rechargeBuyNumber3);
        tvMoreChargeRule = view.findViewById(R.id.tv_more_charge_rule);
        userpage_rechargetext = view.findViewById(R.id.userpage_rechargetext);
        tvMoreChargeRule.setOnClickListener(this);

        buyOne = view.findViewById(R.id.buyOne);
        buyTwo = view.findViewById(R.id.buyTwo);
        buyThree = view.findViewById(R.id.buyThree);
        mTopBar = view.findViewById(R.id.userpage_topbar);
        buyGrid = view.findViewById(R.id.userpage_grid);
        userpageMyuserpage = view.findViewById(R.id.userpage_myuserpage);
        userpageMoneyBtn = view.findViewById(R.id.userpage_money_btn);
        gradesLayout = view.findViewById(R.id.grades_layout);


    }

    @Override
    protected void initDate(View view) {
        //??????????????????
        list = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            map = new HashMap<>();
            map.put("titles", titles[i]);
            map.put("imgs", imgRess[i]);
            list.add(map);
        }
    }

    @Override
    protected void initSet(View view) {
        String[] from = {"titles", "imgs"};
        int[] to = {R.id.text_title, R.id.img_title};

        userpage_rechargetext.setText("??????" + RequestConfig.getConfigObj().getCurrency());
        simpleAdapter = new SimpleAdapter(getContext(), list, R.layout.adapter_grid_set, from, to);
        buyGrid.setAdapter(simpleAdapter);
        //mTopBar.setTitle("??????");
        mTopBar.addRightImageButton(R.drawable.mine_edit, R.id.mine_ed).setOnClickListener(this);

        //??????gridview???????????????
        buyGrid.setFocusable(false);

        //????????????##?????????--?????????--?????????????????????--????????????--
        setOnclickListener(view, R.id.count_love_layout, R.id.count_fans_layout, R.id.count_divide_layout, R.id.by_money_btn);
        //????????????##??????39--??????59--??????199--user????????????--??????????????????
        setOnclickListener(buyOne, buyTwo, buyThree, userpageMyuserpage, userpageMoneyBtn);

        buyGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (titles[position].equals("??????")) {
                    //showToastMsg(getContext(),"????????????!");
                    doLogout();
                } else if (titles[position].equals("??????/????????????")) {
                    goActivity(getContext(), ToJoinActivity.class);
                } else {

                    clickMenu(position);
                }
            }
        });
    }

    //?????????????????????
    private void clickMenu(int position) {

        switch (position) {

            case 0:
                //????????????
                clickVideoAuth();
                break;
            case 1:
                //?????????
                ShortVideoActivity.startShortVideoActivity(getContext());
                break;
            case 2:
                //??????
                PrivatePhotoActivity.startPrivatePhotoActivity(getContext(), uId, "", 0);
                break;
            case 3:
                //????????????
                //InviteActivity.startInviteAcitivty(getContext());
                WebViewActivity.openH5Activity(getContext(), true, getString(R.string.inviting_friends), ConfigModel.getInitData().getApp_h5().getInvite_share_menu());
                break;
            case 4:
                //????????????
                WebViewActivity.openH5Activity(getContext(), false, "????????????", RequestConfig.getConfigObj().getNewBitGuideUrl());
                break;
            case 5:
                //????????????
                WebViewActivity.openH5Activity(getContext(), true, "????????????", RequestConfig.getConfigObj().getMyLevelUrl());
                break;
            case 6:
                //????????????
                break;
            case 7:
                //??????
                Intent  intent = new Intent(getContext(), SettingActivity.class);
                intent.putExtra("state", userCenterData.getUser_auth_status());
                intent.putExtra("sex", userCenterData.getSex());
                getContext().startActivity(intent);
                break;

            default:
                break;
        }
    }

    //????????????
    private void clickVideoAuth() {

        if (userCenterData == null) {
            return;
        }

        Intent intent = new Intent(getContext(), CuckooAuthFormActivity.class);
        intent.putExtra(CuckooAuthFormActivity.STATUS, StringUtils.toInt(userCenterData.getUser_auth_status()));
        startActivity(intent);
    }

    @Override
    protected void initDisplayData(View view) {
        requestUserData();//?????????????????????????????????????????????
    }

    ////////////////////////////////////////////??????????????????//////////////////////////////////////////
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_more_charge_rule:

                break;
            case R.id.mine_ed:
                goActivity(getContext(), EditActivity.class);
                break;
            case R.id.buyOne:
                goBuyMoney(39);
                break;
            case R.id.buyTwo:
                goBuyMoney(59);
                break;
            case R.id.buyThree:
                goBuyMoney(199);
                break;
            case R.id.userpage_myuserpage:
                goMyUserPage();
                break;
            case R.id.userpage_money_btn:
                goMoneyPage();
                break;
            case R.id.count_love_layout:
                goMsgListPage(0);
                break;
            case R.id.count_fans_layout:
                goMsgListPage(1);
                break;
            case R.id.count_divide_layout:
                //showDialogRatio();
                break;
            case R.id.by_money_btn:
                goByMoneyMore();
                break;
            case R.id.dialog_left_btn:
                goMyGraderPage();
                break;
            case R.id.dialog_right_btn:
                goInvitePage();
                break;
            case R.id.dialog_close:
                radioDialog.dismiss();
                break;
            default:
                break;
        }
    }

    //////////////////////////////////////////??????????????????////////////////////////////////////////////

    /**
     * ???????????????????????????
     */
    private void requestUserData() {
        Api.getUserDataByMe(
                uId,
                uToken,
                new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        JsonRequestUserCenterInfo jsonRequestUserCenterInfo = JsonRequestUserCenterInfo.getJsonObj(s);
                        if (jsonRequestUserCenterInfo.getCode() == 1) {

                            userCenterData = jsonRequestUserCenterInfo.getData();
                            UserModel userModel = SaveData.getInstance().getUserInfo();
                            userModel.setIs_open_do_not_disturb(userCenterData.getIs_open_do_not_disturb());
                            SaveData.getInstance().saveData(userModel);
                            //log(jsonRequestUserCenterInfo.toString());
                            refreshUserData();
                            refreshOtherData();

                        } else if (jsonRequestUserCenterInfo.getCode() == ApiConstantDefine.ApiCode.LOGIN_INFO_ERROR) {

                            doLogout();
                        } else {

                            showToastMsg(getContext(), jsonRequestUserCenterInfo.getMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        ToastUtils.showLong("????????????????????????!");
                    }
                }
        );
    }

    /*
     * ?????????????????????????????????????????????
     * */
    private void refreshOtherData() {

        //????????????????????????
        if (userCenterData.getPay_coin() != null) {

            if (userCenterData.getPay_coin().size() == 1) {
                oneMoney.setText(userCenterData.getPay_coin().get(0).getMoney());
                oneMoneyTo.setText(userCenterData.getPay_coin().get(0).getFormatCoin());
            } else if (userCenterData.getPay_coin().size() == 2) {
                oneMoney.setText(userCenterData.getPay_coin().get(0).getMoney());
                oneMoneyTo.setText(userCenterData.getPay_coin().get(0).getFormatCoin());
                twoMoney.setText(userCenterData.getPay_coin().get(1).getMoney());
                twoMoneyTo.setText(userCenterData.getPay_coin().get(1).getFormatCoin());
            } else if (userCenterData.getPay_coin().size() == 3) {
                oneMoney.setText(userCenterData.getPay_coin().get(0).getMoney());
                oneMoneyTo.setText(userCenterData.getPay_coin().get(0).getFormatCoin());
                twoMoney.setText(userCenterData.getPay_coin().get(1).getMoney());
                twoMoneyTo.setText(userCenterData.getPay_coin().get(1).getFormatCoin());
                threeMoney.setText(userCenterData.getPay_coin().get(2).getMoney());
                threeMoneyTo.setText(userCenterData.getPay_coin().get(2).getFormatCoin());
            }

        }

    }

    /**
     * ???????????????????????????
     */
    private void showDialogRatio() {
        radioDialog = showViewDialog(getContext(), R.layout.dialog_ratio_view, new int[]{R.id.dialog_close, R.id.dialog_left_btn, R.id.dialog_right_btn});
        TextView text = radioDialog.findViewById(R.id.radio_radio_text);
        text.setText(userCenterData.getSplit());
    }

    /**
     * ??????????????????????????????
     */
    private void refreshUserData() {
        if (ApiUtils.isTrueUrl(userCenterData.getAvatar())) {
            Utils.loadHttpImg(getContext(), Utils.getCompleteImgUrl(userCenterData.getAvatar()), userImg);
        }
        userName.setText(userCenterData.getUser_nickname());
        gradesLayout.addView(new GradeShowLayout(getContext(), userCenterData.getLevel(), userCenterData.getSex()));//??????-??????

        //??????????????????
        userIsVerify.setImageResource(SelectResHelper.getAttestationRes(StringUtils.toInt(userCenterData.getUser_auth_status())));
        aboutNumber.setText(userCenterData.getAttention_all());
        fansNumber.setText(userCenterData.getAttention_fans());
        ratioNumber.setText(userCenterData.getSplit());
        moneyNumber.setText(userCenterData.getCoin());
    }

    /**
     * ??????????????????
     */
    private void goInvitePage() {
        //showToastMsg(getContext(),"??????????????????");
        radioDialog.dismiss();
    }

    /**
     * ????????????????????????
     */
    private void goMyGraderPage() {
        //showToastMsg(getContext(),"????????????????????????");
        radioDialog.dismiss();
    }

    /**
     * ????????????????????????
     */
    private void goByMoneyMore() {
        //showToastMsg(getContext(),"????????????");

        RechargeActivity.startRechargeActivity(getContext());
    }

    /**
     * ????????????????????????##0?????????--1?????????
     *
     * @param i ??????
     */
    private void goMsgListPage(int i) {
        if (i == 0) {
            //??????
            goActivity(getContext(), AboutFansActivity.class, "??????");
        } else {
            //??????
            goActivity(getContext(), AboutFansActivity.class, "??????");
        }
    }

    /**
     * ????????????????????????
     *
     * @param money ???????????????(??????/???[?????????])
     */
    private void goBuyMoney(int money) {
        showToastMsg(getContext(), "??????" + money * 100 + currency);
    }

    /**
     * ?????????????????????
     */
    private void goMyUserPage() {
        goActivity(getContext(), HomePageActivity.class, uId);
    }

    /**
     * ???????????????????????????
     */
    private void goMoneyPage() {
        //showToastMsg(getContext(),"????????????");
        WealthActivity.startWealthActivity(getContext());
    }

    /**
     * ??????/????????????
     */
    private void doLogout() {

        LoginUtils.doLoginOut(getContext());

    }

}
