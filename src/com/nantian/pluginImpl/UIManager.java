package com.nantian.pluginImpl;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.nantian.entity.SignBoardInfo;
import com.nantian.entity.SignPDF;
import com.nantian.sign.SignNameDialog;
import com.nantian.utils.Setting;

public class UIManager {
	private SignNameDialog signDialog;

	private Handler handler;

	private static final int SIGN_DIALOG_SHOW = 100;

	private static final int SIGN_DIALOG_DISMSS = 200;
	
	private static final int SET_PEN_SIZE = 300;
	
	private static final int SET_PEN_COLOR = 400;
	
	private static final int SET_SIGN_BG_COLOR = 500;
	
	private static final int CLEAR_SIGN_BOARD = 600;
	
	private static final int SAVE_BITMAP = 700;
	
	private static final int SAVE_BITMAP_AND_SIGN = 800;

	private Context mContext;

	public Context getmContext() {
		return mContext;
	}

	public void init(Context context) {
		this.mContext = context;
		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void dispatchMessage(Message msg) {
				switch (msg.what) {
				case SIGN_DIALOG_SHOW:
					SignBoardInfo info = (SignBoardInfo) msg.obj;
					if (signDialog == null) {

						signDialog = new SignNameDialog(mContext, info);
					} else {
						if (info != null
								&& !info.equals(signDialog.getSignInfo())) {
							signDialog.updateDialogSign(info);
						}
					}
					signDialog.show();
					break;
				case SET_PEN_SIZE:
					int max  = (Integer) msg.obj;
					if (signDialog != null) {
						signDialog.setPenSize(max);
					}
					Setting.instance().saveIntData(Setting.KEY_PEN_MAX, max);
					break;
				case SET_PEN_COLOR:
					if (signDialog != null) {
						signDialog.setPenColor((Integer) msg.obj);
					}
					break;
				case SET_SIGN_BG_COLOR:
					if (signDialog != null) {
						Log.e("", "SET_SIGN_BG_COLOR");
						signDialog.setSignBackgroudColor((Integer) msg.obj);
					}
					break;
				case SIGN_DIALOG_DISMSS:
					if (signDialog != null && signDialog.isShowing()) {
						signDialog.dismiss();
					}
					break;
				case CLEAR_SIGN_BOARD:
					if (signDialog != null) {
						signDialog.clear();
					}
					break;
				case SAVE_BITMAP:
					if (signDialog != null) {
						try {
							signDialog.saveSignPic();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				case SAVE_BITMAP_AND_SIGN:
					if (signDialog != null) {
						try {
							String png = signDialog.saveSignPic();
							signDialog.sign((String) msg.obj, png);
							signDialog.dismiss();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				default:
					break;
				}
			}
		};
	}

	public UIManager() {

	}

	public void showSignDialog(SignBoardInfo info) {
		Message msg = Message.obtain();
		msg.obj = info;
		msg.what = SIGN_DIALOG_SHOW;
		handler.sendMessage(msg);
	}

	public void dimissDialog() {
		Message msg = Message.obtain();
		msg.what = SIGN_DIALOG_DISMSS;
		handler.sendMessage(msg);
	}

	public void setDPenSize(int max) {

		Message msg = Message.obtain();
		msg.obj = max;
		msg.what = SET_PEN_SIZE;
		handler.sendMessage(msg);

	}

	public void setDialogBgColor(String color) throws Exception {
		 int c = Color.parseColor(color);
			Message msg = Message.obtain();
			msg.what = SET_SIGN_BG_COLOR;
			msg.obj = c;
			handler.sendMessage(msg);
			Setting.instance().saveData(Setting.KEY_BG_COLOR, color);

	}

	public void setPenColor(String color) throws Exception {
		 int c = Color.parseColor(color);
			Message msg = Message.obtain();
			msg.what = SET_PEN_COLOR;
			msg.obj = c;
			handler.sendMessage(msg);
		Setting.instance().saveData(Setting.KEY_PEN_COLOR, color);
	}

	public void clearSignBoard() {
		Message msg = Message.obtain();
		msg.what = CLEAR_SIGN_BOARD;
		handler.sendMessage(msg);
	}
	
	public void saveBitmap() {
		Message msg = Message.obtain();
		msg.what = SAVE_BITMAP;
		handler.sendMessage(msg);
	}
	public void saveBitmapAndSign(String pdfPath) {
		Message msg = Message.obtain();
		msg.what = SAVE_BITMAP_AND_SIGN;
		msg.obj = pdfPath;
		handler.sendMessage(msg);
	}
	public void setSignPdf(SignPDF signPDF){
		if (signDialog != null){
			signDialog.setSignPDF(signPDF);
		}
		
	}
}
