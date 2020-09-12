package com.video.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public class RemoteControlMenu extends View {

    private int mWidth;
    private int mHeight;

    private RectF bigRectF;
    private int bigRadius;
    private RectF smallRectF;
    private int smallRadius;
    private int padding = 0;
    private int sweepAngel = 90;
    private int offsetAngel;

    @TouchArea
    private int mTouchArea = TouchArea.INVALID;

    private Paint mPaint;
    private Paint mCenterPaint;
    private Region topRegion, bottomRegion, leftRegion, rightRegion, centerRegion, globalRegion;
    private Path topPath, bottomPath, leftPath, rightPath, centerPath, selectedPath;

    Matrix mMapMatrix;

    private int unselectedColor = Color.parseColor("#242526");
    private int selectedColor = Color.parseColor("#242526");

    private int unCenterselectedColor = Color.parseColor("#2D2E2F");
    private int centerSelectedColor = Color.parseColor("#2D2E2F");

    private boolean isSelected = false;


    public RemoteControlMenu(Context context) {
        this(context, null);
    }

    public RemoteControlMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoteControlMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(unselectedColor);


        mCenterPaint = new Paint();
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setStyle(Paint.Style.FILL);
        mCenterPaint.setStrokeWidth(10);
        mCenterPaint.setColor(unCenterselectedColor);





        offsetAngel = (360 - sweepAngel * 4) / 4;
        bigRectF = new RectF();
        smallRectF = new RectF();

        topRegion = new Region();
        bottomRegion = new Region();
        leftRegion = new Region();
        rightRegion = new Region();
        centerRegion = new Region();
        globalRegion = new Region();
        topPath = new Path();
        bottomPath = new Path();
        leftPath = new Path();
        rightPath = new Path();
        centerPath = new Path();
        mMapMatrix = new Matrix();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TouchArea.LEFT, TouchArea.TOP, TouchArea.RIGHT, TouchArea.BOTTOM,
            TouchArea.CENTER, TouchArea.INVALID})
    public  @interface TouchArea {
        int LEFT = 1;
        int TOP = 2;
        int RIGHT = 3;
        int BOTTOM = 4;
        int CENTER = 5;
        int INVALID = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float[] pts = new float[2];
        pts[0] = event.getX();
        pts[1] = event.getY();
        Log.d("zhen", "原始触摸位置：" + Arrays.toString(pts) + " mMapMatrix: " + mMapMatrix);
        mMapMatrix.mapPoints(pts);

        int x = (int) pts[0];
        int y = (int) pts[1];
        Log.w("zhen", "转换后的触摸位置：" + Arrays.toString(pts) + " mMapMatrix: " + mMapMatrix);
        int touchArea = TouchArea.INVALID;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (leftRegion.contains(x, y)) {
                    touchArea = TouchArea.LEFT;
                }
                if (topRegion.contains(x, y)) {
                    touchArea = TouchArea.TOP;
                }
                if (rightRegion.contains(x, y)) {
                    touchArea = TouchArea.RIGHT;
                }
                if (bottomRegion.contains(x, y)) {
                    touchArea = TouchArea.BOTTOM;
                }
                if (centerRegion.contains(x, y)) {
                    touchArea = TouchArea.CENTER;
                }
                if (touchArea == TouchArea.INVALID) {
                    mTouchArea = touchArea;
                    Log.w("zhen", "点击outside");
                } else {
//                    if (mTouchArea == touchArea) {
//                        //取消选中
//                        isSelected = false;
//                        mTouchArea = TouchArea.INVALID;
//                    } else {
                        //选中
                        isSelected = true;
                        mTouchArea = touchArea;
//                    }
                    if (mListener != null) {
                        mListener.onMenuClicked(mTouchArea);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                //取消选中
                isSelected = false;
                mTouchArea = TouchArea.INVALID;
                invalidate();
                break;
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        //大圆
        bigRadius = (Math.min(mWidth, mHeight)) / 2;
        bigRectF.set(-bigRadius, -bigRadius, bigRadius, bigRadius);
        //小圆
        smallRadius = (bigRadius - padding) / 2-20;
        smallRectF.set(-smallRadius - padding, -smallRadius - padding,
                smallRadius + padding, smallRadius + padding);

        mMapMatrix.reset();
        globalRegion.set(-mWidth / 2, -mHeight / 2, mWidth / 2, mHeight / 2);

        centerPath.addCircle(0, 0, smallRadius, Path.Direction.CW);
        centerRegion.setPath(centerPath, globalRegion);

        float startAngel = -sweepAngel / 2f;
        rightPath.addArc(bigRectF, startAngel, sweepAngel + 4);
        startAngel += sweepAngel;
        rightPath.arcTo(smallRectF, startAngel, -sweepAngel);
        rightPath.close();
        rightRegion.setPath(rightPath, globalRegion);

        startAngel += offsetAngel;
        bottomPath.addArc(bigRectF, startAngel, sweepAngel + 4);
        startAngel += sweepAngel;
        bottomPath.arcTo(smallRectF, startAngel, -sweepAngel);
        bottomPath.close();
        bottomRegion.setPath(bottomPath, globalRegion);

        startAngel += offsetAngel;
        leftPath.addArc(bigRectF, startAngel, sweepAngel + 4);
        startAngel += sweepAngel;
        leftPath.arcTo(smallRectF, startAngel, -sweepAngel);
        leftPath.close();
        leftRegion.setPath(leftPath, globalRegion);

        startAngel += offsetAngel;
        topPath.addArc(bigRectF, startAngel, sweepAngel + 4);
        startAngel += sweepAngel;
        topPath.arcTo(smallRectF, startAngel, -sweepAngel);
        topPath.close();
        topRegion.setPath(topPath, globalRegion);
        Log.d("zhen", "globalRegion: " + globalRegion);
        Log.d("zhen", "globalRegion: " + globalRegion);
        Log.d("zhen", "leftRegion: " + leftRegion);
        Log.d("zhen", "topRegion: " + topRegion);
        Log.d("zhen", "rightRegion: " + rightRegion);
        Log.d("zhen", "bottomRegion: " + bottomRegion);
        Log.d("zhen", "centerRegion: " + centerRegion);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);
        // 获取测量矩阵(逆矩阵)
        if (mMapMatrix.isIdentity()) {
            canvas.getMatrix().invert(mMapMatrix);
        }
        mCenterPaint.setColor(unCenterselectedColor);
        mPaint.setColor(unselectedColor);
        canvas.drawPath(centerPath, mCenterPaint);
        canvas.drawPath(rightPath, mPaint);
        canvas.drawPath(bottomPath, mPaint);
        canvas.drawPath(leftPath, mPaint);
        canvas.drawPath(topPath, mPaint);

        if (!isSelected) return;
        mPaint.setColor(selectedColor);
        mCenterPaint.setColor(centerSelectedColor);
        switch (mTouchArea) {
            case TouchArea.LEFT:
                canvas.drawPath(leftPath, mPaint);
                break;
            case TouchArea.TOP:
                canvas.drawPath(topPath, mPaint);
                break;
            case TouchArea.RIGHT:
                canvas.drawPath(rightPath, mPaint);
                break;
            case TouchArea.BOTTOM:
                canvas.drawPath(bottomPath, mPaint);
                break;
            case TouchArea.CENTER:
                canvas.drawPath(centerPath, mCenterPaint);
                break;
        }
        Log.e("zhen", " touchArea: " + mTouchArea);
    }

    private MenuListener mListener;

    public void setListener(MenuListener listener) {
        mListener = listener;
    }

    // 点击事件监听器
    public interface MenuListener {
        void onMenuClicked(int type);
    }
}

