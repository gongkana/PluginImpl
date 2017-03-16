    /******************************************************************
    *
    *    Java Lib For Android, Powered By Shenzhen Nantian.
    *
    *    Copyright (c) 2015-2025 Yunnan Nantian Electronics Infomation Co.,Ltd
    *    http://www.nantian.com.cn
    *
    *    Package:     com.van.view.widget
    *
    *    Filename:    GallerySwitcher.java
    *
    *    Description: TODO(轮播广告控件)
    *
    *    Copyright:   Copyright (c) 2015-2025
    *
    *    Company:     Shenzhen Nantian Co.,Ltd
    *
    *    @author:     Bell Tan
    *
    *    @version:    1.0.0
    *
    *    Create at:   2016-8-23 下午4:29:14
    *
    *    Revision:
    *
    *    2016-8-23 下午4:29:14
    *        - first revision
    *        
    *
    *****************************************************************/

package com.nantian.ad;
import java.io.File;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ViewSwitcher;

/**
 * 
 * @ClassName GallerySwitcher
 * @Description 自定义轮播广告控件，支持图片和GIF混播
 * @author Bell Tan
 * @Date 2016-8-23 下午4:29:14
 * @version 1.0.0
 */
public class GallerySwitcher extends ViewSwitcher {
    private Context mContext;

    /**
     * Creates a new empty GallerySwitcher.
     *
     * @param context the application's environment
     */
    public GallerySwitcher(Context context) {
        this(context, null);
    }

    /**
     * Creates a new empty GallerySwitcher for the given context and with the
     * specified set attributes.
     *
     * @param context the application environment
     * @param attrs a collection of attributes
     */
    public GallerySwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;
    }

    public void setImageResource(int resid)
    {
        ImageView image = (ImageView)getNextView();
        
        Glide.with(mContext).load(resid).asBitmap().into(image);
        showNext();
    }

    public void setImageURI(Uri uri)
    {
        ImageView image = (ImageView)getNextView();
        
        Glide.with(mContext).load(uri).asGif().into(image);
        showNext();
    }

    public void setImageDrawable(Drawable drawable)
    {
        ImageView image = (ImageView)getNextView();
        //Glide.with(mContext).load(drawable).asBitmap().into(image);
        image.setImageDrawable(drawable);
        showNext();
    }
    
    public void setImageFile(Context context, final String filePath) {
        final ImageView image = (ImageView)getNextView();
        image.setScaleType(ScaleType.FIT_XY);
        
        if (filePath.toLowerCase().endsWith(".gif")) {
            Glide.with(mContext).load(Uri.fromFile(new File(filePath))).asGif().into(image);
        } else {
            Glide.with(mContext).load(Uri.fromFile(new File(filePath))).asBitmap().format(DecodeFormat.PREFER_ARGB_8888).into(image);
        }
    }
    
    public void setImageFile(FrameLayout.LayoutParams params, final String filePath) {
        final ImageView image = (ImageView)getNextView();
        image.setScaleType(ScaleType.FIT_XY);
        image.setLayoutParams(params);
        
        if (filePath.toLowerCase().endsWith(".gif")) {
            Glide.with(mContext).load(Uri.fromFile(new File(filePath))).asGif().into(image);
        } else {
            Glide.with(mContext).load(Uri.fromFile(new File(filePath))).asBitmap().format(DecodeFormat.PREFER_ARGB_8888).into(image);
        }
        showNext();
    }
    
    public void setImageFileAndDrawable(FrameLayout.LayoutParams params, final String filePath, final Drawable drawable) {
        final ImageView image = (ImageView)getNextView();
        image.setScaleType(ScaleType.FIT_XY);
        image.setLayoutParams(params);
        
        if (filePath.toLowerCase().endsWith(".gif")) {
            image.setImageDrawable(drawable);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(mContext).load(Uri.fromFile(new File(filePath))).asGif().into(image);
                }
            }, 300);
        } else {
            image.setImageDrawable(drawable);
            //Glide.with(mContext).load(Uri.fromFile(new File(filePath))).asBitmap().format(DecodeFormat.PREFER_ARGB_8888).into(image);
        }
        showNext();
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event)
    {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(GallerySwitcher.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info)
    {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(GallerySwitcher.class.getName());
    }
}
