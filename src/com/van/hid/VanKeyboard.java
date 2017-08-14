package com.van.hid;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.nantian.utils.HLog;
import com.nantian.utils.Utils;

import android.util.Log;

public class VanKeyboard implements Runnable{
	private static final String TAG = VanKeyboard.class.getSimpleName();
	private static VanKeyboard _instance;
	private static final int MSG_CLICK_NUMBER = 1001;
	private static final int MSG_CLICK_UNNUMBER = 1002;
	/** 小数点“.” */
	public static final String DOT = ".".trim();
	public static final int KEY_PWD = 42;
	public static final int KEY_OK = 0x0D;
	public static final int KEY_CANCEL = 0x1B;
	public static final int KEY_MOTIFY = 0x08;
	public static final int KEY_PAGEUP = 0x44;
	public static final int KEY_PAGEDOWN = 0x45;
	public static final int KEY_DOT = 0x2E;// 小数点
	public static final int KEY_ASTERISK = 0x42;// 星号
	public static final boolean ISHARDABLE = false;
	/** 按键音量的播放类型 0：业务；1：广告；2：系统按键 */
	public static final int VOLUME_BUSSINESS = 0;
	public static final int VOLUME_AD = 1;
	public static final int VOLUME_KEYTONE = 2;

	private OnClickListener onClickListener;
	private Object mUartManager;
	private boolean isStopThread;
	private byte[] mBuffer;
	private int mBufLen;
	private Map<String,Method>methods = new HashMap<String, Method>();
	private VanKeyboard() {
		try {
			Class<?> clazz = Class.forName("com.van.uart.UartManager");
			mUartManager = clazz.newInstance(); // 获取有参构造
			HLog.e(TAG, "open begin..");
			methods.put("open",clazz.getDeclaredMethod("open", String.class,String.class)) ; 
			methods.put("close",clazz.getDeclaredMethod("close"));
			methods.put("isOpen",clazz.getDeclaredMethod("isOpen"));			
			methods.put("write",clazz.getDeclaredMethod("write",byte[].class,int.class));
			methods.put("read",clazz.getDeclaredMethod("read",byte[].class,int.class,int.class,int.class));
			open();
			mBuffer = new byte[512];
		} catch (Exception e) {
			HLog.e(TAG, e.getMessage());
		}
	}

	public void open () throws Exception{	
		methods.get("open").invoke(mUartManager,"ttyS3", "B9600");
	}
	
	public void close () throws Exception{	
		methods.get("close").invoke(mUartManager);
	}
	
	public boolean isUartOpen() throws Exception{
		return (Boolean) methods.get("isOpen").invoke(mUartManager);
	}
	
	public int write(byte[] data,int length) throws Exception{
		return (Integer) methods.get("write").invoke(mUartManager,data,length);
	}
	
	public int read(byte[] data,int length,int wait, int interval) throws Exception{
		return (Integer) methods.get("read").invoke(mUartManager,data,length,wait,interval);
	}
	public static VanKeyboard instance() {
		if (null == _instance) { // 提高访问效率
			synchronized (VanKeyboard.class) {
				if (null == _instance) {
					HLog.e(TAG, "init .....");
					_instance = new VanKeyboard();
					
				}else{
					HLog.e(TAG, "ok");
				}
			}
		}

		return _instance;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
	/**
	 * 请求打开键盘设备
	 * 
	 * @return true：打开成功；false：打开失败
	 */
	public boolean requestOpenKeyboard() {
		try {
			
			byte[] data = { (byte) 0x81 };
			write(data, data.length);

			return true;
		} catch (Exception e) {
			HLog.e(TAG, "open keyboard fail::" + e.toString(), e);
		}

		return false;
	}

	/**
	 * 打开键盘设备
	 */
	public void openKeyboard() {
		isStopThread = false;
		new Thread(this).start();
	}

	/**
	 * 关闭键盘设备
	 */
	public void closeKeyboard() {
		try {
			isStopThread = true;
			//mHandler.removeMessages(MSG_CLICK_NUMBER);
			//mHandler.removeMessages(MSG_CLICK_UNNUMBER);
			byte[] data = { (byte) 0x83 };
			write(data, data.length);
		} catch (Exception e) {
			HLog.e(TAG, e.toString(), new Exception());
		}
	}

	/**
	 * 
	 * @param key
	 */
	private void setKeyValue(int key) {
		Log.e("", "Vanboard key = "+key);
		if (onClickListener != null) {
			onClickListener.onKey(key);
		}
		
		//Logger.getLogger().i("onKey value: " + key);

		/**
		msg.arg1 = key;
		if (key >= 0x30 && key <= 0x39) {
			msg.what = MSG_CLICK_NUMBER;
		} else {
			msg.what = MSG_CLICK_UNNUMBER;
		}
		mHandler.sendMessage(msg);
		*/
	}


	@Override
	public void run() {
		byte[] buf = new byte[8];
		mBufLen = 0;

		while (!isStopThread) {
			try {
				int length = read(buf, buf.length, 100, 0);
				
				if (length > 0) {
					/**
					if (mBufLen + length > mBuffer.length) {
						mBufLen = 0;
					}

					System.arraycopy(buf, 0, mBuffer, mBufLen, length);
					mBufLen += length;
					*/
					setKeyValue(buf[0]);
				}
			} catch (Exception e) {
				HLog.e(TAG, e.toString(), new Exception());
			}

		}
	}

	private void searchKey() {
		int headPos = -1;
		int tailPos = -1;

		for (int i = 0; i < mBufLen; i++) {
			if (0x42 == mBuffer[i]) {
				headPos = i;
			} else if ((0x46 == mBuffer[i]) && (-1 != headPos)) {
				tailPos = i;
				break;
			}
		}

		if (-1 == headPos || -1 == tailPos) {
			return;
		}

		int high = mBuffer[headPos + 7] - 0x30;
		int low = mBuffer[headPos + 8] - 0x30;
		int key = (byte) ((high << 4) | low);

		setKeyValue(key);
		System.arraycopy(mBuffer, tailPos + 1, mBuffer, 0, mBufLen - tailPos
				- 1);
		mBufLen -= tailPos + 1;
	}

	public interface OnClickListener {
		public void onKey(int key);
	}
	
	public void release(){
		closeKeyboard();
		try {
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_instance = null;
	}
}
