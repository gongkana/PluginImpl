package com.van.uart;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class VanKeyboard implements Runnable{
	private static final String TAG = VanKeyboard.class.getSimpleName();
	private static final int MSG_CLICK_NUMBER   = 1001;
	private static final int MSG_CLICK_UNNUMBER = 1002;
	/** 小锟斤拷锟姐“.锟斤拷 */
	public static final String DOT = ".".trim();
	public static final int KEY_PWD    = 42;
	public static final int KEY_OK     = 0x0D;
	public static final int KEY_CANCEL = 0x1B;
	public static final int KEY_MOTIFY = 0x08;
	public static final int KEY_PAGEUP = 0x44;
	public static final int KEY_PAGEDOWN = 0x45;
	public static final int KEY_DOT      = 0x2E;// 小锟斤拷锟斤拷
	
	private OnClickListener onClickListener;
	private UartManager mUartManager;
	private boolean isStopThread;

	public VanKeyboard() {
		try {
			Log.e("", "new Uart begin :333");
			mUartManager = new UartManager();
			Log.e("", "will open begin :");
			mUartManager.open("ttyS3", UartManager.BaudRate.B9600);
		} catch (Exception e) {
			Log.e("", "init Keyboad err :"+e.getMessage());
		}
	}
	
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	/**
	 * 锟斤拷锟斤拷蚩锟斤拷锟斤拷璞�
	 * 
	 * @return true锟斤拷锟津开成癸拷锟斤拷false锟斤拷锟斤拷失锟斤拷
	 */
	public boolean requestOpenKeyboard() {
		try {
			byte[] data = { (byte) 0x81 };
			mUartManager.write(data, data.length);
			return true;
		} catch (LastError e) {
			Log.e(TAG, "open keyboard fail::" + e.toString(), e);
		}
		
		return false;
	}

	/**
	 * 锟津开硷拷锟斤拷锟借备
	 */
	public void openKeyboard() {
		isStopThread = false;
		new Thread(this).start();
	}

	/**
	 * 锟截闭硷拷锟斤拷锟借备
	 */
	public void closeKeyboard() {
		try {
			isStopThread = true;
			byte[] data = { (byte) 0x83 };
			mUartManager.write(data, data.length);
		} catch (LastError e) {

		}
	}

	public void release(){
		closeKeyboard();
		mUartManager = null;
		//setOnClickListener(null);
	}



	@Override
	public void run() {
		int count = 0;
		byte[] buf = new byte[8];

		while (!isStopThread) {
			try {
				count = mUartManager.read(buf, 8, 500, 20);
			} catch (LastError e1) { 
			}

			if (count > 0) {
				if (onClickListener != null){
					onClickListener.onKey(buf[0]);
				}
				
			}
		}
	}

	public interface OnClickListener {

		public void onKey(int value);


	}
}
