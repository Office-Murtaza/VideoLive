package com.moxin.videoline.fragment;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.moxin.videoline.LiveConstant;
import com.moxin.videoline.R;
import com.moxin.videoline.adapter.FullyGridLayoutManager;
import com.moxin.videoline.adapter.GridImageAdapter;
import com.moxin.videoline.api.Api;
import com.moxin.videoline.base.BaseFragment;
import com.moxin.videoline.modle.HintBean;
import com.moxin.videoline.utils.CuckooQiniuFileUploadUtils;
import com.google.gson.Gson;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.lzy.okgo.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

public class FeedBackFragment extends BaseFragment {


    @BindView(R.id.ed)
    EditText ed;

    @BindView(R.id.tel)
    EditText tel;

    @BindView(R.id.recy)
    RecyclerView recy;

    @BindView(R.id.max)
    TextView max;

    private List<String> uploadImgUrlList = new ArrayList<>();
    private List<LocalMedia> selectList = new ArrayList<>();
    private CuckooQiniuFileUploadUtils cuckooQiniuFileUploadUtils;
    private GridImageAdapter adapter;

    @Override
    protected View getBaseView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_feed_back, container, false);
    }

    @Override
    protected void initView(View view) {

        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        adapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(3);
        recy.setAdapter(adapter);
        recy.setLayoutManager(manager);

        ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                max.setText(ed.getText().length() + "/" + 100);
            }
        });

    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            PictureSelector.create(getActivity())
                    .openGallery(PictureMimeType.ofImage())
                    .maxSelectNum(3)
                    .forResult(PictureConfig.CHOOSE_REQUEST);
        }

    };


    @OnClick(R.id.submit)
    public void sub() {

        if (selectList.size() == 0) {
            showToastMsg(getContext(), "???????????????");
            return;
        }

        if (TextUtils.isEmpty(ed.getText()) || ed.getText().length() <= 10) {
            showToastMsg(getContext(), "?????????10???????????????");
            return;
        }
        if (TextUtils.isEmpty(tel.getText())) {
            showToastMsg(getContext(), "??????????????????");
            return;
        }

        showLoadingDialog(getString(R.string.loading_now_submit_data));

        uploadImgAndVideo();
    }


    private void uploadImgAndVideo() {

        uploadImgUrlList.clear();

        //???????????? ??????????????????
        cuckooQiniuFileUploadUtils.uploadFileLocalMedia(LiveConstant.IMG_DIR, selectList, new CuckooQiniuFileUploadUtils.CuckooQiniuFileUploadCallback() {
            @Override
            public void onUploadFileSuccess(List<String> fileUrlList) {
                hideLoadingDialog();
                if (fileUrlList.size() == selectList.size()) {
                    uploadImgUrlList.addAll(fileUrlList);
                    //??????
                    toPush();
                } else {
                    ToastUtils.showLong("??????????????????!");
                }
            }
        });

    }

    private void toPush() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < uploadImgUrlList.size(); i++) {
            if (i == uploadImgUrlList.size() - 1) {
                stringBuilder.append(uploadImgUrlList.get(i));
            } else {
                stringBuilder.append(uploadImgUrlList.get(i) + ",");
            }
        }


        Api.doFeedBack(ed.getText().toString(), tel.getText().toString(), stringBuilder.toString(), new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                Log.i("??????", "onSuccess: " + s);
                hideLoadingDialog();
                HintBean hint = new Gson().fromJson(s, HintBean.class);
                if (hint.getCode() == 1) {
                    getActivity().finish();
                    showToastMsg(getContext(), "????????????");
                } else {
                    showToastMsg(getContext(), hint.getMsg());
                }

            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                Log.i("????????????", "onError: " + e.getMessage());
                hideLoadingDialog();
            }
        });
    }

    @Override
    protected void initDate(View view) {
        cuckooQiniuFileUploadUtils = new CuckooQiniuFileUploadUtils();
    }

    @Override
    protected void initSet(View view) {

    }

    @Override
    protected void initDisplayData(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PictureConfig.CHOOSE_REQUEST:
                // ????????????????????????
                selectList = PictureSelector.obtainMultipleResult(data);

                for (LocalMedia media : selectList) {
                    Log.i("??????-----???", media.getPath());
                }
                adapter.setList(selectList);
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

}
