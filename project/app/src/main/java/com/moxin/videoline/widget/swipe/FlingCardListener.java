package com.moxin.videoline.widget.swipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by dionysis_lorentzos on 5/8/14
 * for package com.lorentzos.swipecards
 * and project Swipe cards.
 * Use with caution dinausaurs might appear!
 */


public class FlingCardListener implements View.OnTouchListener {

    private static final String TAG = FlingCardListener.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;

    private final float objectX;
    private final float objectY;
    private final int objectH;
    private final int objectW;
    private final int parentWidth;
    private int parentHeight;
    private final FlingListener mFlingListener;
    private final Object dataObject;
    private final float halfWidth;
    private float halfHeight;
    private float BASE_ROTATION_DEGREES;

    private float aPosX;
    private float aPosY;
    private float aDownTouchX;
    private float aDownTouchY;

    // The active pointer is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;
    private View frame = null;


    private final int TOUCH_ABOVE = 0;
    private final int TOUCH_BELOW = 1;
    private int touchPosition;
    private final Object obj = new Object();
    private boolean isAnimationRunning = false;
    private float MAX_COS = (float) Math.cos(Math.toRadians(45));


    public FlingCardListener(View frame, Object itemAtPosition, FlingListener flingListener) {
        this(frame, itemAtPosition, 15f, flingListener);
    }

    public FlingCardListener(View frame, Object itemAtPosition, float rotation_degrees, FlingListener flingListener) {
        super();
        this.frame = frame;
        this.objectX = frame.getX();
        this.objectY = frame.getY();
        this.objectH = frame.getHeight();
        this.objectW = frame.getWidth();
        this.halfWidth = objectW / 2f;
        this.halfHeight = objectH / 2f;
        this.dataObject = itemAtPosition;
        this.parentWidth = ((ViewGroup) frame.getParent()).getWidth();
        this.parentHeight = ((ViewGroup) frame.getParent()).getHeight();
        this.BASE_ROTATION_DEGREES = rotation_degrees;
        this.mFlingListener = flingListener;

    }


    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                // from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
                // Save the ID of this pointer

                mActivePointerId = event.getPointerId(0);
                float x = 0;
                float y = 0;
                boolean success = false;
                try {
                    x = event.getX(mActivePointerId);
                    y = event.getY(mActivePointerId);
                    success = true;
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Exception in onTouch(view, event) : " + mActivePointerId, e);
                }
                if (success) {
                    // Remember where we started
                    aDownTouchX = x;
                    aDownTouchY = y;
                    //to prevent an initial jump of the magnifier, aposX and aPosY must
                    //have the values from the magnifier frame
                    if (aPosX == 0) {
                        aPosX = frame.getX();
                    }
                    if (aPosY == 0) {
                        aPosY = frame.getY();
                    }

                    if (y < objectH / 2) {
                        touchPosition = TOUCH_ABOVE;
                    } else {
                        touchPosition = TOUCH_BELOW;
                    }
                }

                view.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                resetCardViewOnStack();

                view.getParent().requestDisallowInterceptTouchEvent(false);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (event.getAction() &
                        MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            case MotionEvent.ACTION_MOVE:

//                Log.e("move", "move......");
                // Find the index of the active pointer and fetch its position
                final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                final float xMove = event.getX(pointerIndexMove);
                final float yMove = event.getY(pointerIndexMove);


                //from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
                // Calculate the distance moved
                final float dx = xMove - aDownTouchX;
                final float dy = yMove - aDownTouchY;

                // Move the frame
                aPosX += dx;
                aPosY += dy;
//                Log.e("x,y", aPosX + "," + aPosY);

                // calculate the rotation degrees
                float distobjectX = aPosX - objectX;
                float rotation = BASE_ROTATION_DEGREES * 2.f * distobjectX / parentWidth;
                if (touchPosition == TOUCH_BELOW) {
                    rotation = -rotation;
                }

                //in this area would be code for doing something with the view as the frame moves.
                frame.setX(aPosX);
                frame.setY(aPosY);
//                frame.setRotation(rotation);
                mFlingListener.onMoveXY(aPosX, aPosY);
                float scrollProgressPercent = getScrollProgressPercent();

                if (scrollProgressPercent == 2f) {
                    mFlingListener.onScroll(scrollProgressPercent, "top");
                } else {
                    mFlingListener.onScroll(scrollProgressPercent, "");
                }


                break;

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                resetCardViewOnStack();
                view.getParent().requestDisallowInterceptTouchEvent(false);
                break;
            }
        }

        return true;
    }

    private float getScrollProgressPercent() {
        if (movedBeyondLeftBorder()) {
            return -1f;
        } else if (movedBeyondRightBorder()) {
            return 1f;
        } else if (movedBeyondTopBorder()) {
            return 2f;
        } else {
            float zeroToOneValue = (aPosX + halfWidth - leftBorder()) / (rightBorder() - leftBorder());
            return zeroToOneValue * 2f - 1f;
        }
    }

    private boolean resetCardViewOnStack() {
        if (movedBeyondLeftBorder()) {
            // Left Swipe
            mFlingListener.leftExit(dataObject);
            mFlingListener.onScroll(-1.0f, "left");
//            mFlingListener.onMoveXY(0, 0);
        } else if (movedBeyondRightBorder()) {
            // Right Swipe
            mFlingListener.rightExit(dataObject);
            mFlingListener.onScroll(1.0f, "right");
//            mFlingListener.onMoveXY(0, 0);
        } else if (movedBeyondTopBorder()) {
            mFlingListener.topExit(dataObject);
            mFlingListener.onScroll(-1.0f, "top");

        } else {
            mFlingListener.onMoveXY(0, 0);
            double abslMoveDistance = Math.sqrt(Math.pow(aPosX - objectX, 2) + Math.pow(aPosY - objectY, 2));
            //????????????????????????
            aPosX = 0;
            aPosY = 0;
            aDownTouchX = 0;
            aDownTouchY = 0;
            Log.e("v", frame.getX() + "");
            frame.animate()
                    .setDuration(200)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationRepeat(Animator animation) {
                            super.onAnimationRepeat(animation);
                            Log.w("paulzzzzzzz", "paulzzzzzzz onAnimationRepeat :");

                        }

                        @Override
                        public void onAnimationPause(Animator animation) {
                            super.onAnimationPause(animation);
//                            Log.e("v", frame.getX() + "");
                            Log.w("paulzzzzzzz", "paulzzzzzzz onAnimationPause :" + frame.getX());
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            Log.w("paulzzzzzzz", "paulzzzzzzz onAnimationCancel :" + frame.getX());
                        }

                    })
                    .x(objectX)
                    .y(objectY)
                    .rotation(0);
            mFlingListener.onScroll(0.0f, "bottom");
            if (abslMoveDistance < 4.0) {
                mFlingListener.onClick(dataObject);
            }
        }
        return false;
    }

    private boolean movedBeyondLeftBorder() {
        return aPosX + halfWidth < leftBorder();
    }

    private boolean movedBeyondRightBorder() {
        return aPosX + halfWidth > rightBorder();
    }

    //????????????????????????????????????
    private boolean movedBeyondTopBorder() {
        return aPosY + halfHeight < topBorder();
    }

    public float leftBorder() {
        return 2 * parentWidth / 5.f;
    }

    public float rightBorder() {
        return 3 * parentWidth / 5.f;
    }

    public float topBorder() {
        return 2 * parentHeight / 5.f;
    }


    public void onSelected(final boolean isLeft,
                           float exitY, long duration) {

        isAnimationRunning = true;
        float exitX;
        if (isLeft) {
            exitX = -objectW - getRotationWidthOffset();
            Log.e("TAG", "isLeft" + isLeft + "objectW" + objectW + "exitX: " + exitX);
        } else {
            exitX = parentWidth + getRotationWidthOffset();
            Log.e("TAG", "isLeft" + isLeft + "parentWidth" + parentWidth + "exitX: " + exitX);
        }
        this.frame.animate()
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .x(exitX)
                .y(exitY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isLeft) {
                            mFlingListener.onCardExited();
                        } else {
                            mFlingListener.onCardExited();
                        }
                        isAnimationRunning = false;
                    }
                })
                .rotation(getExitRotation(isLeft));
    }


    public void onSelectedToTop(final boolean isTop,
                                float exitX, long duration) {

        isAnimationRunning = true;
        float exitY;
        if (isTop) {
            exitY = -objectH - getRotationHeightOffset();
            Log.e("TAG", "isLeft" + isTop + "objectW" + objectH + "exitX: " + exitX);
        } else {
            exitY = parentHeight + getRotationHeightOffset();
            Log.e("TAG", "isLeft" + isTop + "parentWidth" + parentHeight + "exitX: " + exitX);
        }
        this.frame.animate()
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .x(exitX)
                .y(exitY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isTop) {
                            mFlingListener.onCardExited();
                        } else {
                            mFlingListener.onCardExited();
                        }
                        isAnimationRunning = false;
                    }
                })
                .rotation(getExitRotationToTop(isTop));
    }

    public void resetCurrentView(AnimatorListenerAdapter listenerAdapter) {
        aPosX = 0;
        aPosY = 0;
        aDownTouchX = 0;
        aDownTouchY = 0;
        frame.animate()
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .setListener(listenerAdapter)
                .x(objectX)
                .y(objectY)
                .rotation(0);
        mFlingListener.onScroll(0.0f, "");
        mFlingListener.onMoveXY(0, 0);
    }


    /**
     * Starts a default left exit animation.
     */
    public void selectLeft() {
        if (!isAnimationRunning)
            onSelected(true, objectY, 100);
    }

    /**
     * Starts a default right exit animation.
     */
    public void selectRight() {
        if (!isAnimationRunning)
            onSelected(false, objectY, 100);
    }


    public void selectTop() {
        if (!isAnimationRunning)
            onSelectedToTop(true, objectX, 100);
    }


    public void swipeRight() {
        if (!isAnimationRunning)
            onSelected(false, getExitPoint(parentWidth), 100);
    }

    public void swipeLeft() {
        if (!isAnimationRunning)
            onSelected(true, getExitPoint(-objectW), 100);
    }


    public void swipeTop() {
        if (!isAnimationRunning)
            onSelectedToTop(true, getExitPoint(-objectH), 100);
    }

    private float getExitPoint(int exitXPoint) {
        float[] x = new float[2];
        x[0] = objectX;
        x[1] = aPosX;

        float[] y = new float[2];
        y[0] = objectY;
        y[1] = aPosY;

        LinearRegression regression = new LinearRegression(x, y);

        //Your typical y = ax+b linear regression
        return (float) regression.slope() * exitXPoint + (float) regression.intercept();
    }

    private float getExitRotation(boolean isLeft) {
        float rotation = BASE_ROTATION_DEGREES * 2.f * (parentWidth - objectX) / parentWidth;
        if (touchPosition == TOUCH_BELOW) {
            rotation = -rotation;
        }
        if (isLeft) {
            rotation = -rotation;
        }
        return rotation;
    }

    private float getExitRotationToTop(boolean isTop) {
        float rotation = BASE_ROTATION_DEGREES * 2.f * (parentHeight - objectY) / parentHeight;
        if (touchPosition == TOUCH_BELOW) {
            rotation = -rotation;
        }
        if (isTop) {
            rotation = -rotation;
        }
        return rotation;
    }


    /**
     * When the object rotates it's width becomes bigger.
     * The maximum width is at 45 degrees.
     * <p/>
     * The below method calculates the width offset of the rotation.
     */
    private float getRotationWidthOffset() {
        return objectW / MAX_COS - objectW;
    }

    private float getRotationHeightOffset() {
        return objectH / MAX_COS - objectH;
    }

    public void setRotationDegrees(float degrees) {
        this.BASE_ROTATION_DEGREES = degrees;
    }

    public boolean isTouching() {
        return this.mActivePointerId != INVALID_POINTER_ID;
    }

    public PointF getLastPoint() {
        return new PointF(this.aPosX, this.aPosY);
    }

    public interface FlingListener {

        void onCardExited();

        void leftExit(Object dataObject);

        void rightExit(Object dataObject);

        void topExit(Object dataObject);

        void onClick(Object dataObject);

        void onScroll(float scrollProgressPercent, String direction);

        void onMoveXY(float moveX, float moveY);
    }

}





