package com.van.hid;

import java.lang.reflect.Constructor;

import com.nantian.plugininterface.IKeyboard;
import com.nantian.utils.Utils;

import android.os.Message;
import android.util.Log;

public class VanKeyboard implements Runnable{
	private static final String TAG = VanKeyboard.class.getSimpleName();
	private static VanKeyboard _instance;
	private static final int MSG_CLICK_NUMBER = 1001;
	private static final int MSG_CLICK_UNNUMBER = 1002;
	/** С���㡰.�� */
	public static final String DOT = ".".trim();
	public static final int KEY_PWD = 42;
	public static final int KEY_OK = 0x0D;
	public static final int KEY_CANCEL = 0x08;
	public static final int KEY_MOTIFY = 0x0B;
	public static final int KEY_PAGEUP = 0x44;
	public static final int KEY_PAGEDOWN = 0x45;
	public static final int KEY_DOT = 0x2E;// С����
	public static final int KEY_ASTERISK = 0x42;// �Ǻ�
	public static final boolean ISHARDABLE = false;
	/** ���������Ĳ������� 0��ҵ��1����棻2��ϵͳ���� */
	public static final int VOLUME_BUSSINESS = 0;
	public static final int VOLUME_AD = 1;
	public static final int VOLUME_KEYTONE = 2;

	private OnClickListener onClickListener;
	private IKeyboard mUartManager;
	private boolean isStopThread;
	private byte[] mBuffer;
	private int mBufLen;

	private VanKeyboard() {
		try {
			Class<?> clazz = Class.forName("com.van.uart.UartManager");
			mUartManager = (IKeyboard) clazz.newInstance(); // ��ȡ�вι���
			mUartManager.open("ttyS3"/* "ttyMFD1" */, "B9600");
			mBuffer = new byte[512];
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public static VanKeyboard instance() {
		if (null == _instance) { // ��߷���Ч��
			synchronized (VanKeyboard.class) {
				if (null == _instance) {
					_instance = new VanKeyboard();
				}
			}
		}

		return _instance;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	/**
	 * ���ɲ���ȡ�������SM2��Կ ͷ Ԥ������ ָ�� ���� β 02H 0000 3002 �� 03H
	 * 
	 * @return SM2��Կ
	 */
	public byte[] getPublicKey() {
		byte[] data = { 0x00, 0x00, 0x30, 0x02 };
		byte[] send = generatePack(data);
		byte[] buff = new byte[256];
		byte[] temp = new byte[32];

		try {
			mUartManager.write(send, send.length);

			int length = mUartManager.read(temp, 32, 2000, 0);
			int offset = 0;

			if (length <= 0) {
				return null;
			}
			System.arraycopy(temp, 0, buff, offset, length);
			while (length > 0 && offset < 256) {
				offset += length;
				length = mUartManager.read(temp, 32, 200, 20);
				System.arraycopy(temp, 0, buff, offset, length);
			}

			Utils.showData(buff, buff.length);
			if (buff[131] != 0x03 || buff[0] != 0x02 || buff[1] != 'O'
					|| buff[2] != 'K') {
				return null;
			}

			return parsePacks(buff, offset);
		} catch (Exception e) {
			//Logger.getLogger().e("LastError: " + e.getMessage());
		}

		return null;
	}

	/**
	 * ��������Կ������Կ
	 * 
	 * @param key
	 * @param isMainKey
	 *            true:����Կ false: ������Կ
	 * @return true ������Կ�ɹ���false ������Կʧ��
	 */
	public boolean setKey(byte[] key, boolean isMainKey) {
		byte[] data = new byte[key.length + 5];
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x30;
		data[3] = (byte) (isMainKey ? 0x03 : 0x04);
		data[4] = 0x04;
		System.arraycopy(key, 0, data, 5, key.length);

		byte[] send = generatePack(data);
		byte[] buff = new byte[256];
		byte[] temp = new byte[32];

		Utils.showData(send, send.length);
		try {
			mUartManager.write(send, send.length);

			int length = mUartManager.read(temp, 32, 3000, 0);// 3���ڻ�δ�������ݷ��ؿ�
			int offset = 0;

			if (length <= 0) {
				return false;
			}

			System.arraycopy(temp, 0, buff, offset, length);
			while (length > 0 && offset < 256) {
				offset += length;
				length = mUartManager.read(temp, 32, 200, 20);
				System.arraycopy(temp, 0, buff, offset, length);
			}

			//Logger.getLogger().i("=====setKey=====");
			Utils.showData(buff, offset);

			if (buff[0] != 0x02 || buff[1] != 'O' || buff[2] != 'K') {
				return false;
			}

			return true;
		} catch (Exception e) {
		}

		return false;
	}

	/**
	 * ��ȡ����Կ������Կ��У��ֵ ͷ Ԥ������ ָ�� ���� β 02H 0000 3019 ��Կ���� 03H 01 ����Կ 02 ������Կ
	 * 
	 * @param isMainKey
	 *            true: ����Կ��false ������Կ
	 * @return
	 */
	public byte[] getKeyCheck(boolean isMainKey) {
		byte[] data = { 0x00, 0x00, 0x30, 0x19, 0x01 };
		if (!isMainKey) {
			data[4] = 0x02;
		}

		byte[] send = generatePack(data);
		byte[] buff = new byte[256];
		byte[] temp = new byte[32];

		try {
			mUartManager.write(send, send.length);

			int length = mUartManager.read(temp, 32, 2000, 0);// 2���ڻ�δ�������ݷ��ؿ�
			int offset = 0;

			if (length < 0)
				return null;
			System.arraycopy(temp, 0, buff, offset, length);
			while (length > 0 && offset < 256) {
				offset += length;
				length = mUartManager.read(temp, 32, 200, 0);
				System.arraycopy(temp, 0, buff, offset, length);
			}

			Utils.showData(buff, buff.length);

			if (buff[0] != 0x02 || buff[1] != 'O' || buff[2] != 'K') {
				return null;
			}

			return parsePacks(buff, offset);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * �������� ͷ Ԥ������ ָ�� ���� β 02H 0000 3024 ��Կ���� LEN ��������(�128�ֽ�) 03H 01 ����Կ 02
	 * ������Կ
	 * 
	 * @param isMainKey
	 * @param encode
	 * @return
	 */
	public byte[] getEncodeString(boolean isMainKey, byte[] encode) {
		byte[] data = new byte[encode.length + 5];// { 0x00, 0x00, 0x30, 0x24,
													// 0x01 };
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x30;
		data[3] = 0x24;
		data[4] = (byte) (isMainKey ? 0x01 : 0x02);

		System.arraycopy(encode, 0, data, 5, encode.length);
		byte[] send = generatePack(data);
		byte[] buff = new byte[256];
		byte[] temp = new byte[32];

		try {
			mUartManager.write(send, send.length);

			int length = mUartManager.read(temp, 32, 3000, 0);// 3���ڻ�δ�������ݷ��ؿ�
			int offset = 0;

			if (length <= 0) {
				return null;
			}

			System.arraycopy(temp, 0, buff, offset, length);
			while (length > 0 && offset < 256) {
				offset += length;
				length = mUartManager.read(temp, 32, 200, 20);
				System.arraycopy(temp, 0, buff, offset, length);
			}

			//Logger.getLogger().i("=====setKey=====");
			Utils.showData(buff, offset);

			if (buff[0] != 0x02 || buff[1] != 'O' || buff[2] != 'K') {
				return null;
			}

			return parsePacks(buff, offset);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * ��ȡ����
	 * 
	 * @return
	 */
	public byte[] getPassword() {
		byte[] buff = new byte[256];

		try {
			int length = mUartManager.read(buff, buff.length, 1000, 0);

			if (buff[0] != 0x02 || buff[1] != 'O' || buff[2] != 'K') {
				return null;
			}

			byte[] data = new byte[16];
			for (int i = 0; i < data.length; i++) {
				int high = buff[i * 2 + 3] - 0x30;
				int low = buff[i * 2 + 4] - 0x30;

				data[i] = (byte) ((high << 4) | low);
			}

			return data;
		} catch (Exception e) {
		}

		return null;
	}

	/**
	 * �����Կ
	 * 
	 * @param nMode
	 *            1-MainKey 2-WorkKey 3-Both
	 * @return
	 */
	public boolean clearKey(int nMode) {
		byte[] data = { 0x00, 0x00, 0x30, 0x23, 0x01 };
		data[4] = (byte) nMode;

		byte[] send = generatePack(data);
		byte[] buff = new byte[64];

		try {
			mUartManager.write(send, send.length);
			int length = mUartManager.read(buff, buff.length, 200, 0);

			System.out.println("------ " + length);

			if (length <= 0) {
				return false;
			}

			Utils.showData(buff, length);

			if (buff[0] != 0x02 || buff[1] != 'O' || buff[2] != 'K') {
				return false;
			}

			return true;
		} catch (Exception e) {
		}

		return false;
	}

	/**
	 * �������� ͷ Ԥ������ ָ�� ���� β 02H 0000 3005 ������ʾ+00 03H ������ʾ+01+�ʺ� (12�ֽ�) ������ʾ:
	 * 01-���������� 02-��������һ�� ������ʾ+02+������󳤶� ������ʾ: 01-���������� 02-��������һ��
	 * ������ʾ+03+������󳤶�+�ʺ�(12�ֽ�) ������ʾ: 01-���������� 02-��������һ��
	 * 
	 * @return
	 */
	public boolean inputPassword(byte type, byte len) {
		byte[] data = { 0x00, 0x00, 0x30, 0x05, 0x01, 0x02, 0x06 };
		data[4] = type;
		data[6] = len;
		byte[] send = generatePack(data);

		try {
			mUartManager.write(send, send.length);

			return true;
		} catch (Exception e) {
		}

		return false;
	}

	/**
	 * ȡ����������
	 * 
	 * @return
	 */
	public boolean cancelInputPassword() {
		byte[] data = { 0x00, 0x00, 0x30, 0x17 };
		byte[] send = generatePack(data);
		byte[] buff = new byte[256];

		try {
			mUartManager.write(send, send.length);
			int length = mUartManager.read(buff, buff.length, 200, 0);

			if (length <= 0) {
				return false;
			}

			if (buff[0] != 0x02 || buff[1] != 'O' || buff[2] != 'K') {
				return false;
			}

			return true;
		} catch (Exception e) {
		}

		return false;
	}

	private byte[] generatePacks(byte[] data) {
		byte[] result = new byte[data.length + 2];

		System.arraycopy(data, 0, result, 1, data.length);

		result[0] = 0x02;
		result[result.length - 1] = 0x03;

		return result;
	}

	private byte[] generatePack(byte[] data) {
		byte[] result = new byte[data.length * 2 + 2];

		for (int i = 0; i < data.length; i++) {
			result[i * 2 + 1] = (byte) (0x30 + ((data[i] & 0xff) >> 4));
			result[i * 2 + 2] = (byte) (0x30 + (data[i] & 0x0f));
		}

		result[0] = 0x02;
		result[result.length - 1] = 0x03;

		return result;
	}

	private byte[] parsePacks(byte[] data, int length) {
		byte[] result = new byte[(length - 4)];

		System.arraycopy(data, 3, result, 0, result.length);

		return result;
	}

	private byte[] parsePack(byte[] data, int length) {
		byte[] result = new byte[(length - 4) / 2];

		for (int i = 0; i < result.length; i++) {
			int high = data[i * 2 + 3] - 0x30;
			int low = data[i * 2 + 4] - 0x30;

			result[i] = (byte) ((high << 4) | low);
		}

		return result;
	}

	/**
	 * ����򿪼����豸
	 * 
	 * @return true���򿪳ɹ���false����ʧ��
	 */
	public boolean requestOpenKeyboard() {
		try {
			byte[] data = { (byte) 0x81 };
			mUartManager.write(data, data.length);

			return true;
		} catch (Exception e) {
			//LogUtil.e(TAG, "open keyboard fail::" + e.toString(), e);
		}

		return false;
	}

	/**
	 * �򿪼����豸
	 */
	public void openKeyboard() {
		isStopThread = false;
		new Thread(this).start();
	}

	/**
	 * �رռ����豸
	 */
	public void closeKeyboard() {
		try {
			isStopThread = true;
			//mHandler.removeMessages(MSG_CLICK_NUMBER);
			//mHandler.removeMessages(MSG_CLICK_UNNUMBER);
			byte[] data = { (byte) 0x83 };
			mUartManager.write(data, data.length);
		} catch (Exception e) {

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
				int length = mUartManager.read(buf, buf.length, 100, 0);
				
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
				//LogUtil.e(TAG, e.toString(), new Exception());
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
		_instance = null;
	}
}
