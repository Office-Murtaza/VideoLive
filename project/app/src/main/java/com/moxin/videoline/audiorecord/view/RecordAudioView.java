package com.moxin.videoline.audiorecord.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.moxin.videoline.audiorecord.AudioRecordManager;


public class RecordAudioView extends Button {

    private static final String TAG = "RecordAudioView";

    private Context context;
    private IRecordAudioListener recordAudioListener;
    private AudioRecordManager audioRecordManager;
    private boolean isCanceled;
    private float downPointY;
    private static final float DEFAULT_SLIDE_HEIGHT_CANCEL = 150;
    private boolean isRecording;


    public RecordAudioView(Context context) {
        super(context);
        initView(context);
    }

    public RecordAudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RecordAudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        this.context = context;
        audioRecordManager = AudioRecordManager.getInstance();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        PPLog.i(TAG, "onTouchEvent");
        super.onTouchEvent(event);
        if(recordAudioListener != null){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    setSelected(true);
                    downPointY = event.getY();
                    recordAudioListener.onFingerPress();
                    startRecordAudio();
                    break;
                case MotionEvent.ACTION_UP:
                    setSelected(false);
                    onFingerUp();
                    break;
                case MotionEvent.ACTION_MOVE:
                    onFingerMove(event);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    isCanceled = true;
                    onFingerUp();
                    break;
                default:

                    break;
            }
        }
        return true;
    }

    /**
     * ????????????,????????????????????????????????????????????????
     */
    private void onFingerUp(){
        if(isRecording){
            if(isCanceled){
                isRecording = false;
                audioRecordManager.cancelRecord();
                recordAudioListener.onRecordCancel();
            }else{
                stopRecordAudio();
            }
        }
    }

    private void onFingerMove(MotionEvent event){
        float currentY = event.getY();
        isCanceled = checkCancel(currentY);
        if(isCanceled){
            recordAudioListener.onSlideTop();
        }else{
            recordAudioListener.onFingerPress();
        }
    }

    private boolean checkCancel(float currentY){
        return downPointY - currentY >= DEFAULT_SLIDE_HEIGHT_CANCEL;
    }

    /**
     * ????????????ready??????,????????????ready???????????????
     */
    private void startRecordAudio() throws RuntimeException {
        boolean isPrepare = recordAudioListener.onRecordPrepare();
        if(isPrepare){
            String audioFileName = recordAudioListener.onRecordStart();
            //????????????????????????
            try{
                audioRecordManager.init(audioFileName);
                audioRecordManager.startRecord();
                isRecording = true;
            }catch (Exception e){
                this.recordAudioListener.onRecordCancel();
            }
        }
    }

    /**
     * ????????????
     */
    private void stopRecordAudio() throws RuntimeException {
        if(isRecording){
            try {
                isRecording = false;
                audioRecordManager.stopRecord();
                this.recordAudioListener.onRecordStop();
            }catch (Exception e){
                this.recordAudioListener.onRecordCancel();
            }
        }
    }

    /**
     * ????????????IRecordAudioStatus,??????????????????????????????????????????,????????????????????????
     * @param recordAudioListener
     */
    public void setRecordAudioListener(IRecordAudioListener recordAudioListener) {
        this.recordAudioListener = recordAudioListener;
    }

    public void invokeStop(){
        onFingerUp();
    }

    public interface IRecordAudioListener {
        boolean onRecordPrepare();
        String onRecordStart();
        boolean onRecordStop();
        boolean onRecordCancel();
        void onSlideTop();
        void onFingerPress();
    }
}
