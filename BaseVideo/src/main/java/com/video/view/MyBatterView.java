//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.video.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.huoyan.basevideo.R.styleable;

public class MyBatterView extends View {
    private Paint mBatteryPaint;
    private Paint mPowerPaint;
    private float mBatteryStroke;
    private RectF mBatteryRect;
    private RectF mCapRect;
    private RectF mPowerRect;
    private int specWidthSize;
    private int specHeightSize;
    private int batteryColor;
    private int powerColor;
    private int lowPowerColor;
    private int power;
    private float mCapWidth;

    public void setPro(int power) {
        if (power < 0) {
            power = 0;
        } else if (power > 100) {
            power = 100;
        }

        this.power = power;
        this.invalidate();
    }

    public MyBatterView(Context context) {
        this(context, (AttributeSet)null);
    }

    public MyBatterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyBatterView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint({"NewApi"})
    public MyBatterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBatteryStroke = 2.0F;
        this.power = 10;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, styleable.BatteryView);
        this.batteryColor = typedArray.getColor(styleable.BatteryView_batteryColor, Color.argb(255, 150, 150, 150));
        this.powerColor = typedArray.getColor(styleable.BatteryView_powerColor, Color.argb(255, 0, 255, 0));
        this.lowPowerColor = typedArray.getColor(styleable.BatteryView_lowPowerColor, Color.argb(255, 255, 0, 0));
        this.mCapWidth = typedArray.getDimension(styleable.BatteryView_mCapWidth, 20.0F);
        typedArray.recycle();
        this.initPaint();
    }

    public void initPaint() {
        this.mBatteryPaint = new Paint();
        this.mBatteryPaint.setColor(this.batteryColor);
        this.mBatteryPaint.setAntiAlias(true);
        this.mBatteryPaint.setStyle(Style.STROKE);
        this.mBatteryPaint.setStrokeWidth(this.mBatteryStroke);
        this.mPowerPaint = new Paint();
        this.mPowerPaint.setAntiAlias(true);
        this.mPowerPaint.setStyle(Style.FILL);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        this.specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(this.specWidthSize, this.specHeightSize);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.power <= 20) {
            this.mPowerPaint.setColor(this.lowPowerColor);
        } else {
            this.mPowerPaint.setColor(this.powerColor);
        }

        this.mBatteryRect = new RectF(2.0F, 2.0F, (float)(this.specWidthSize - 10) - this.mCapWidth, (float)(this.specHeightSize - 4));
        this.mCapRect = new RectF((float)(this.specWidthSize - 10) - this.mCapWidth, (float)(this.specHeightSize - 2) * 0.25F, (float)(this.specWidthSize - 10), (float)(this.specHeightSize - 4) * 0.75F);
        float right;
        if (this.power < 20) {
            right = ((float)(this.specWidthSize - 10) - this.mCapWidth - 2.0F) / 100.0F * 20.0F - 2.0F;
        } else {
            right = ((float)(this.specWidthSize - 10) - this.mCapWidth - 2.0F) / 100.0F * (float)this.power - 2.0F;
        }

        this.mPowerRect = new RectF(this.mBatteryStroke + 2.0F, 3.0F + this.mBatteryStroke, right, (float)this.specHeightSize - (3.0F + this.mBatteryStroke) - 2.0F);
        canvas.drawRoundRect(this.mBatteryRect, 5.0F, 5.0F, this.mBatteryPaint);
        canvas.drawRoundRect(this.mCapRect, 5.0F, 5.0F, this.mBatteryPaint);
        canvas.drawRoundRect(this.mPowerRect, 5.0F, 5.0F, this.mPowerPaint);
    }
}
