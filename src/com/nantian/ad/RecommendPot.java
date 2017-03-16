package com.nantian.ad;
import java.io.IOException;
import java.io.InputStream;

import com.nantian.utils.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;


public class RecommendPot extends ImageView {
    private int mIdcSpaceWidth = 10; //the distance between two circle-center
    private int mCurrentScreen;
    private int mStartPos;

    private float startPosX;
    private float startPosY;
    private int mHeight;

    private Paint mPaint;
    private Bitmap mNormalPoint;
    private Bitmap mSelectPoint;
    
    public int mIndicatorChildCount;
    
    public RecommendPot(Context context) {
        super(context);
        initialize(context);
    }

    public RecommendPot(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public RecommendPot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        startPosY = 0;
        mPaint = new Paint();
        mPaint.setAlpha((int)(255));

        mNormalPoint = Utils.getBitmapFromAsset(context, "navigation_spot_normal.png");
        mSelectPoint = Utils.getBitmapFromAsset(context, "navigation_spot_selected");
        mHeight = Math.max(mHeight, mNormalPoint.getHeight());
;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, mHeight);
    }

    public int getCurrentScreen() {
        return mCurrentScreen;
    }

    public void setCurrentScreen(int currentScren) {
        this.mCurrentScreen = currentScren;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(0 >= mIndicatorChildCount){
        	return;
        }
        mPaint.setAntiAlias(true);
        mStartPos = startPosition();
        for (int i = 0; i < mIndicatorChildCount; i++) {
            startPosX = position(mStartPos, i);// - mNavigationBitmapRadus;
            if (i == mCurrentScreen) {
                canvas.drawBitmap(mSelectPoint, startPosX, startPosY, mPaint);
            } else {
                canvas.drawBitmap(mNormalPoint, startPosX, startPosY, mPaint);
            }
        }
    }

    private int position(int start, int i) {
        return start + i * mIdcSpaceWidth;
    }

    private int startPosition() {
        int sc = mIndicatorChildCount;
        int totalLen = (sc - 1) * mIdcSpaceWidth + mNormalPoint.getWidth();
        int viewWidth = getWidth();
        int startPos = (viewWidth - totalLen) >> 1;
        return startPos;
    }

    public boolean onTouchEvent(MotionEvent event) {
        /**
         * must return false, if not, the scroll at this view will not response
         * the scroll screen event.Do this for a better experience.
         */
        super.onTouchEvent(event);
        return false;

    }
    
    @SuppressLint("NewApi")
	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        buildLayer();
    }
    
    public void setIndicatorChildCount(int childCount){
        if(mIndicatorChildCount == childCount){
            return;
        }
        mIndicatorChildCount = childCount;
        requestLayout();
    }
}
