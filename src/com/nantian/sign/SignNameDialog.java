package com.nantian.sign;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.nantian.entity.SignBoardInfo;
import com.nantian.entity.SignPDF;
import com.nantian.sign.views.SignaturePad;
import com.nantian.utils.Setting;
import com.nantian.utils.Utils;
import com.van.paperless.R;

/**
 * 签锟斤拷锟藉窗锟斤拷
 */
public class SignNameDialog extends Dialog {
	private static final String TAG = SignNameDialog.class.getSimpleName();

	public static final String SIGN_PICTURE_NAME = "sign.png";
	
	private SignBoardInfo signInfo;

	private SignaturePad mSlate;

	private SignPDF signPDF;
	public SignNameDialog(Context context) {
		super(context);
	}

	public SignNameDialog(Context context, SignBoardInfo signInfo) {
		this(context, signInfo.getSign_x(), signInfo.getSing_y(), signInfo
				.getWidth(), signInfo.getHeigth());
	}

	public SignNameDialog(Context context, int x_point, int y_point, int width,
			int height) {
		super(context,R.style.dialogStyle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//FrameLayout dialogView = new FrameLayout(context);
		//LayoutParams pa = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//dialogView.setLayoutParams(pa);
		
		mSlate = new SignaturePad(context);
		if (width < 350) {
			width = 350;
		}
		if (height < 200) {
			height = 200;
		}
		mSlate.setBackgroundColor(Color.parseColor("#00FFFFFF"));
		setContentView(mSlate);
		//dialogView.addView(mSlate);
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.type = WindowManager.LayoutParams.TYPE_TOAST;
		lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		dialogWindow.setGravity(Gravity.TOP | Gravity.LEFT);
		lp.width = width; // 锟斤拷锟�
		lp.height = height; // 锟竭讹拷
		lp.x = x_point;
		lp.y = y_point;
		lp.format = PixelFormat.TRANSLUCENT;
		dialogWindow.setAttributes(lp);
		setCanceledOnTouchOutside(false);
	}

	public void setPenSize(int max) {
		mSlate.setMaxWidth(max);
	}

	public void setSignBackgroudColor(int color) throws RuntimeException {

		mSlate.setBackgroundColor(color);
	}

	public void setPenColor(int color) throws RuntimeException {

		mSlate.setPenColor(color);

	}
	
	public void clear(){
		mSlate.clear();
	}
	
	public void updateDialogSign(int x_point, int y_point, int width, int height) {
		if (width < 350) {
			width = 350;
		}
		if (height < 200) {
			height = 200;
		}
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = width;
		lp.height = height;
		lp.x = x_point;
		lp.y = y_point;
		dialogWindow.setAttributes(lp);
		mSlate.clear();
	}

	public void updateDialogSign(SignBoardInfo signInfo) {
		updateDialogSign(signInfo.getSign_x(), signInfo.getSing_y(),
				signInfo.getWidth(), signInfo.getHeigth());
		this.signInfo = signInfo;
	}

	@Override
	public void show() {
		super.show();

	}

	@Override
	public void dismiss() {
		super.dismiss();
		Log.e("", "dialog  dismis");
		clear();
	}
	
	public String saveSignPic() throws Exception{
		File dir = new File(Environment.getExternalStorageDirectory()+File.separator+"Nantian","Temp");
		Utils.savaBitmap(dir, SIGN_PICTURE_NAME, mSlate.getSignatureBitmap());
		return dir.getAbsolutePath()+File.separator+SIGN_PICTURE_NAME;
	}
	
	public SignBoardInfo getSignInfo() {
		return signInfo;
	}


	public void setSignPDF(SignPDF signPDF) {
		this.signPDF = signPDF;
	}
	
	public String sign (String pdfPath,String pngPath) throws Exception{
		if (signPDF != null){
			PDFCenter.signInPDF(pdfPath, signPDF.getPageNum(), 
					(float) signPDF.getPointX(),(float) signPDF.getPointY(), pngPath);
		}
		return pdfPath;
	}
}
