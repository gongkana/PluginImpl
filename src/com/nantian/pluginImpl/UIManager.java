package com.nantian.pluginImpl;

import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.nantian.ad.PlayerFragment;
import com.nantian.entity.SignBoardInfo;
import com.nantian.entity.SignPDF;
import com.nantian.plugininterface.IPlayer;
import com.nantian.sign.SignNameDialog;
import com.nantian.utils.DeviceInfo;
import com.nantian.utils.HLog;
import com.nantian.utils.Setting;

public class UIManager implements IPlayer {
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

	private static final int PAUSE_AD = 900;

	private static final int PLAY_AD = 1000;
	
	private static final int SET_AD_TIME = 1100;
	
	private static final int GET_PLAYER = 1200;
	private Context mContext;

	private static UIManager instanse;
	private PlayerFragment player;
	
	public boolean isInit;
	public Context getmContext() {
		return mContext;
	}

	public IPlayer getPlayer(){
		
		return this;
	}
	public void init(Context context) {
		if(isInit){
			return;
		}
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
					int max = (Integer) msg.obj;
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
						HLog.e("", "SET_SIGN_BG_COLOR");
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
							SignPDF sign = (SignPDF) msg.obj;
							sign.setReason(signDialog.getTrack());
							signDialog.sign(sign, png);
							signDialog.dismiss();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				case PAUSE_AD:
					PackageManager packageManager = mContext
							.getPackageManager();

					Intent it = packageManager
							.getLaunchIntentForPackage("com.nantian.iss.isstserver");
					HLog.e("","pauseAD");
					mContext.startActivity(it);
					break;
				case PLAY_AD:
					ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE); 
					
					
					RunningTaskInfo taskInfo = manager.getRunningTasks(1).get(0); 

					
					String shortClassName = taskInfo.topActivity.getShortClassName(); //类名 
					String className = taskInfo.topActivity.getClassName(); //完整类名 
					String packageName = taskInfo.topActivity.getPackageName(); //包名
				
				
					Intent owner = 	new Intent();
					owner.setAction("com.nantian.paperless.ad");
					ComponentName cn = new ComponentName("com.van.paperless", "com.nantian.home.view.MainActivity");
					if (mContext instanceof Service){
						owner.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					}
					owner.setComponent(cn);
					mContext.startActivity(owner);
					
					break;
				case SET_AD_TIME:
					HLog.e("uimanager", "...."+(Integer) msg.obj+",fragment :"+player);
					player.setPlayTime((Integer) msg.obj);
					break;
				case GET_PLAYER:
					if (player == null){
						player =  new PlayerFragment();
					}
				default:
					
					break;
				}
			}
		};

		handler.sendEmptyMessage(GET_PLAYER);
		isInit = true;
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

	public void pauseAD() {

		Message msg = Message.obtain();
		msg.what = PAUSE_AD;
		handler.sendMessage(msg);
	}

	public void playAD() {

		Message msg = Message.obtain();
		msg.what = PLAY_AD;
		handler.sendMessage(msg);
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

	public String saveBitmapAndSign(SignPDF signPDF) throws Exception {
		// Message msg = Message.obtain();
		// msg.what = SAVE_BITMAP_AND_SIGN;
		// msg.obj = signPDF;
		// handler.sendMessage(msg);
		String pngPath = null;
		if (signDialog != null) {

			pngPath = signDialog.saveSignPic();

			signPDF.setReason(signDialog.getTrack());
			signDialog.sign(signPDF, pngPath);
			signDialog.dismiss();

		}
		return pngPath;
	}
	public String getSignTrack(){
		if (signDialog != null) {
			return signDialog.getTrack();
		}
		return "";
	}
	public void setAdTime(int time) {
		Message msg = Message.obtain();
		msg.what = SET_AD_TIME;
		msg.obj = time;
		handler.sendMessage(msg);
		
	}

	public void setAdType(int type) {
		player.setPalyerMode(type);
		
	}

	@Override
	public Fragment getFragment() {
		// TODO Auto-generated method stub
		return player;
	}

	@Override
	public ArrayList<String> getPlayList() {
		// TODO Auto-generated method stub
		return player.getPlayList();
	}

	@Override
	public void init() {
		player.init();
		
	}

	@Override
	public void next() {
		player.init();
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void play(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pre() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPalyerMode(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlayTime(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updataPlayerList() {
		HLog.e("UIManager", "updata begin");
		player.updataPlayerList();
		
	}
	
	
	@Override
	public void release(String arg0) {

	}

}
