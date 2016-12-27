package com.nantian.utils;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

import com.van.hid.DESUtils;

public class Setting {
	
	public static final String KEY_PEN_MIN = "key_pen_min";
	public static final String KEY_PEN_MAX = "key_pen_max";
	public static final String KEY_PEN_COLOR = "key_pen_color";
	public static final String KEY_BG_COLOR = "key_bg_color";
	private static Setting _instance;
	private static final String TAG = Setting.class.getSimpleName();
	private Context mContext;
	private AdType adType; // 锟斤拷锟脚癸拷锟斤拷锟斤拷锟�
	private String sysPassword; // 系统锟斤拷锟斤拷锟斤拷锟斤拷
	private int volumeAd; // 锟斤拷锟斤拷锟斤拷锟�
	private int volumeVoice; // 业锟斤拷锟斤拷锟斤拷 0-锟斤拷锟斤拷锟斤拷1-小锟斤拷2-锟叫ｏ拷3-锟斤拷
	private int volumeKey; // 锟斤拷锟斤拷锟斤拷锟斤拷
	private String[] mMainKeys; // 锟斤拷锟斤拷钥
	private String[] mWorkKeys; // 锟斤拷锟斤拷锟斤拷钥
	private int sysTimeout; // 系统锟斤拷时
	private int passTimeout; // 锟斤拷锟斤拷锟斤拷锟诫超时
	private int otherTimeout; // 锟斤拷锟斤拷锟斤拷锟诫超时
	private int enterAdWait; // 锟斤拷锟斤拷锟斤拷却锟绞憋拷锟�
	private int baudRate; // 锟斤拷锟节诧拷锟斤拷锟绞ｏ拷 0-9600
	private int keyWork; // 锟斤拷锟斤拷锟斤拷坦锟斤拷锟斤拷锟绞�
	private int uPLength; // 锟斤拷锟诫长锟斤拷
	private int btLayout; // 锟斤拷锟斤拷锟街诧拷锟斤拷式
	private int UTimeOut; // 锟斤拷时锟斤拷锟酵斤拷锟�
	private int DCJ; // 锟姐钞锟斤拷
	private int voicenum; // 锟斤拷锟斤拷锟斤拷锟�
	private EncModeType passwordKeyboardMode; // 锟斤拷锟斤拷锟斤拷碳锟斤拷锟侥Ｊ�

	public static final String VOLUME_FILE = "VolumeFile";// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷
	public static final String DEF_MAIN_KEY = "3838383838383838";
	public static final String DEF_WORK_KEY = "3030303030303030";
	public static final int MAIN_KEY_NUMBER = 16;// 十锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷应十锟斤拷锟介工锟斤拷锟斤拷钥
	private byte[] mVerifyCode = { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
			0x18 };

	private Setting() {

	}

	public static Setting instance() {
		if (null == _instance) {
			_instance = new Setting();
		}

		return _instance;
	}

	public void setContext(Context context) {
		mContext = context;
		mMainKeys = new String[MAIN_KEY_NUMBER];
		mWorkKeys = new String[MAIN_KEY_NUMBER];
		load();
	}

	public void saveData(String key, String value) {
		SharedPreferences cfg = mContext.getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void saveIntData(String key, int value) {
		SharedPreferences cfg = mContext.getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public String getData(String key, String defaultValue) {
		SharedPreferences cfg = mContext.getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		return cfg.getString(key, defaultValue);
	}

	public int getInt(String key, int defaultValue) {
		SharedPreferences cfg = mContext.getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		return cfg.getInt(key, defaultValue);
	}

	private void load() {
		SharedPreferences cfg = mContext.getSharedPreferences(TAG,
				Context.MODE_PRIVATE);

		adType = AdType.values()[cfg.getInt("AdType", 0)];
		sysPassword = cfg.getString("Password", "123456");
		volumeAd = cfg.getInt("VolumeAd", 2);
		volumeVoice = cfg.getInt("VolumeVoice", 2);
		volumeKey = cfg.getInt("VolumeKey", 2);

		for (int i = 0; i < mMainKeys.length; i++) {// 一锟斤拷始锟斤拷锟饺讹拷取十锟斤拷锟斤拷锟斤拷钥锟酵癸拷锟斤拷锟斤拷钥
			mMainKeys[i] = cfg.getString("MainKey" + i, DEF_MAIN_KEY);
			LogUtil.i(TAG, "mMainKeys[" + i + "]::" + mMainKeys[i]);
			mWorkKeys[i] = cfg.getString("WorkKey" + i, DEF_WORK_KEY);
			LogUtil.i(TAG, "mWorkKeys[" + i + "]::" + mWorkKeys[i]);
		}

		sysTimeout = cfg.getInt("SystemTimeout", 20);
		passTimeout = cfg.getInt("PasswordTimeout", 20);
		otherTimeout = cfg.getInt("OtherTimeout", 20);
		enterAdWait = cfg.getInt("EnterAdWait", 2);

		baudRate = cfg.getInt("BaudRate", 3);
		keyWork = cfg.getInt("KeyWork", 1);
		uPLength = cfg.getInt("UPLength", 6);
		btLayout = cfg.getInt("BtLayout", 0);

		UTimeOut = cfg.getInt("UTimeOut", 0);
		DCJ = cfg.getInt("DCJ", 0);

		passwordKeyboardMode = EncModeType.valueOf(cfg.getString("EncMode",
				"NONE"));
	}

	private void priSave() {
		SharedPreferences cfg = mContext.getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();

		editor.putInt("AdType", adType.ordinal());
		editor.putString("Password", sysPassword);
		editor.putInt("VolumeAd", volumeAd);
		editor.putInt("VolumeVoice", volumeVoice);
		editor.putInt("VolumeKey", volumeKey);

		editor.putInt("VolumeKey", volumeKey);
		editor.putInt("VolumeKey", volumeKey);
		editor.putInt("VolumeKey", volumeKey);

		for (int i = 0; i < mMainKeys.length; i++) {
			editor.putString("MainKey" + i, mMainKeys[i]);
			editor.putString("WorkKey" + i, mWorkKeys[i]);
		}

		editor.putInt("SystemTimeout", sysTimeout);
		editor.putInt("PasswordTimeout", passTimeout);
		editor.putInt("OtherTimeout", otherTimeout);
		editor.putInt("EnterAdWait", enterAdWait);

		editor.putInt("BaudRate", baudRate);
		editor.putInt("KeyWork", keyWork);
		editor.putInt("UPLength", uPLength);
		editor.putInt("BtLayout", btLayout);
		editor.putInt("VoiceNum", voicenum);

		editor.putString("EncMode", passwordKeyboardMode.toString());

		editor.commit();
	}

	private void save() {
		priSave();
	}

	public AdType getAdType() {
		return adType;
	}

	public void setAdType(AdType type) {
		adType = type;
		save();
	}

	public String getSysPassword() {
		return sysPassword;
	}

	public void setSysPassword(String password) {
		sysPassword = password;
		save();
	}

	public int getVolumeAd() {
		return volumeAd;
	}

	public void setVolumeAd(int value) {
		volumeAd = value;
		updateVolumeAd();
		save();
	}

	public void setVolumeNum(int value) {
		voicenum = value;
		save();
	}

	public int getVolumeNum() {
		return voicenum;
	}

	public int getVolumeVoice() {
		return volumeVoice;
	}

	public void setVolumeVoice(int value) {
		volumeVoice = value;
		updateVolumeVoice();
		save();
	}

	public int getVolumeKey() {
		return volumeKey;
	}

	public void setVolumeKey(int value) {
		volumeKey = value;
		save();
	}

	/**
	 * 锟斤拷取锟斤拷锟斤拷钥
	 * 
	 * @param arrayIndex
	 *            锟斤拷钥锟斤拷锟斤拷锟斤拷锟斤拷
	 * @return 锟斤拷锟斤拷钥
	 */
	public byte[] getMainKey(int arrayIndex) {
		byte[] mainKeyEncoded = null;// 锟斤拷锟杰猴拷锟斤拷锟斤拷锟皆�
		byte[] mainKey = null;// 实锟绞碉拷锟斤拷锟斤拷钥
		if (arrayIndex >= 0 && arrayIndex < mMainKeys.length) {
			mainKeyEncoded = StringUtil.hexStringToBytes(mMainKeys[arrayIndex]);
		} else {
			mainKeyEncoded = StringUtil.hexStringToBytes(DEF_MAIN_KEY);
		}
		try {
			mainKey = DESUtils.decode(mainKeyEncoded, mVerifyCode);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mainKey;
	}

	/**
	 * 锟斤拷取锟斤拷锟斤拷锟斤拷钥
	 * 
	 * @param arrayIndex
	 *            锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷锟斤拷
	 * @return 锟斤拷锟斤拷锟斤拷钥
	 */
	public byte[] getWorkKey(int arrayIndex) {
		byte[] workKey = null;
		if (arrayIndex >= 0 && arrayIndex < mWorkKeys.length) {
			workKey = StringUtil.hexStringToBytes(mWorkKeys[arrayIndex]);
		} else {
			workKey = StringUtil.hexStringToBytes(DEF_WORK_KEY);
		}
		return workKey;
	}

	// /**
	// * 锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷强锟斤拷址锟斤拷锟斤拷透锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟酵癸拷锟斤拷锟斤拷钥
	// *
	// * @param keyValue
	// * @param index
	// */
	// public void setMainKey(String keyValue, int index) {
	// byte[] mainKeyEncoded = new byte[mVerifyCode.length];// 锟斤拷锟杰猴拷锟斤拷锟斤拷锟皆�
	// if (StringUtil.isNotEmpty(keyValue)) {
	// if (index >= 0 && index < mMainKeys.length) {
	// LogUtil.i(TAG, "keyValue::" + keyValue);
	// VanHid.encodeKey(mVerifyCode,
	// StringUtil.hexStringToBytes(keyValue), mainKeyEncoded);// 锟斤拷锟斤拷锟斤拷钥锟斤拷锟叫硷拷锟斤拷
	// mMainKeys[index] = StringUtil.bytesToHexString(mainKeyEncoded);//
	// 锟斤拷锟斤拷锟斤拷羌锟斤拷芎锟斤拷锟斤拷锟斤拷钥
	// }
	// } else if (!StringUtil.isNotEmpty(keyValue) && index < 0) {
	// for (int i = 0; i < mMainKeys.length; i++) {
	// VanHid.encodeKey(mVerifyCode,
	// StringUtil.hexStringToBytes(DEF_MAIN_KEY),
	// mainKeyEncoded);// 锟斤拷默锟斤拷锟斤拷锟斤拷钥锟斤拷锟叫硷拷锟斤拷
	// mMainKeys[i] = StringUtil.bytesToHexString(mainKeyEncoded);// 锟斤拷锟斤拷锟斤拷羌锟斤拷芎锟斤拷锟斤拷锟斤拷钥
	// LogUtil.i(TAG, "mMainKeys[" + i + "]::" + mMainKeys[i]);
	//
	// mWorkKeys[i] = DEF_WORK_KEY;
	// }
	// }
	//
	// save();// 锟斤拷锟矫憋拷锟芥函锟斤拷锟斤拷锟斤拷锟斤拷锟�
	// }

	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷强锟斤拷址锟斤拷锟斤拷透锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟酵癸拷锟斤拷锟斤拷钥
	 * 
	 * @param keyValue
	 * @param index
	 */
	public void setMainKey(String keyValue, int index) {
		byte[] keyBefore = StringUtil.hexStringToBytes(keyValue);
		byte[] mainKeyEncoded = null;

		if (null != keyBefore) {
			mainKeyEncoded = new byte[keyBefore.length];// 锟斤拷锟杰猴拷锟斤拷锟斤拷锟皆�
		} else {
			mainKeyEncoded = new byte[mVerifyCode.length];
		}

		if (StringUtil.isNotEmpty(keyValue)) {
			if (index >= 0 && index < mMainKeys.length) {
				LogUtil.i(TAG, "keyValue::" + keyValue);
				//VanHid.encodeKey(mVerifyCode,
				//		StringUtil.hexStringToBytes(keyValue), mainKeyEncoded);// 锟斤拷锟斤拷锟斤拷钥锟斤拷锟叫硷拷锟斤拷
				try {
					mMainKeys[index] = StringUtil.bytesToHexString(
							DESUtils.encode(StringUtil.hexStringToBytes(keyValue), mVerifyCode));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// 锟斤拷锟斤拷锟斤拷羌锟斤拷芎锟斤拷锟斤拷锟斤拷钥
			}
		} else if (!StringUtil.isNotEmpty(keyValue) && index < 0) {
			for (int i = 0; i < mMainKeys.length; i++) {
				try {
					mMainKeys[i] = StringUtil.bytesToHexString(
							DESUtils.encode(StringUtil.hexStringToBytes(DEF_MAIN_KEY), mVerifyCode));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// 锟斤拷锟斤拷锟斤拷羌锟斤拷芎锟斤拷锟斤拷锟斤拷钥
				LogUtil.i(TAG, "mMainKeys[" + i + "]::" + mMainKeys[i]);

				mWorkKeys[i] = DEF_WORK_KEY;
			}
		}

		save();// 锟斤拷锟矫憋拷锟芥函锟斤拷锟斤拷锟斤拷锟斤拷锟�
	}

	/**
	 * 锟斤拷锟芥工锟斤拷锟斤拷钥
	 * 
	 * @param keyValue
	 * @param index
	 */
	public void setWorkKey(String keyValue, int index) {
		if (StringUtil.isNotEmpty(keyValue)) {
			if (index >= 0 && index < mWorkKeys.length) {
				mWorkKeys[index] = keyValue.trim();
				save();
			}
		}
	}

	public int getSysTimeout() {
		return sysTimeout;
	}

	public void setSysTimeout(int value) {
		sysTimeout = value;
		save();
	}

	public int getPassTimeout() {
		return passTimeout;
	}

	public void setPassTimeout(int value) {
		passTimeout = value;
		save();
	}

	public int getOtherTimeout() {
		return otherTimeout;
	}

	public void setOtherTimeout(int value) {
		otherTimeout = value;
		save();
	}

	public int getEnterAdWait() {
		return enterAdWait;
	}

	public void setEnterAdWait(int value) {
		enterAdWait = value;
		save();
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int value) {
		baudRate = value;
		save();
	}

	public int getKeyWork() {
		return keyWork;
	}

	public void setKeyWork(int value) {
		keyWork = value;
		save();
	}

	public int getUPLength() {
		return uPLength;
	}

	public void setUPLength(int value) {
		uPLength = value;
		save();
	}

	public int getBtLayout() {
		return btLayout;
	}

	public void setBtLayout(int value) {
		btLayout = value;
		save();
	}

	public int getUTimeOut() {
		return UTimeOut;
	}

	public void setUTimeOut(int value) {
		UTimeOut = value;
		save();
	}

	public int getDCJ() {
		return DCJ;
	}

	public void setDCJ(int value) {
		DCJ = value;
	}

	public EncModeType getKeyboardEncryption() {
		return passwordKeyboardMode;
	}

	public void setKeyboardEncryption(EncModeType encryption) {
		passwordKeyboardMode = encryption;
	}

	/**
	 * 锟斤拷锟�
	 * 
	 * @param AdVolume
	 */
	public void updateVolumeAd(int AdVolume) {
		volumeAd = AdVolume;
		AudioManager manager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, AdVolume,
				AudioManager.FLAG_PLAY_SOUND);
	}

	public void updateVolumeAd() {
		updateVolumeAd(volumeAd);
	}

	/**
	 * 业锟斤拷
	 * 
	 * @param volume
	 */
	public void updateVolumeVoice(int volume) {
		volumeVoice = volume;
		AudioManager manager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume,
				AudioManager.FLAG_PLAY_SOUND);
	}

	public void updateVolumeVoice() {
		updateVolumeVoice(volumeVoice);
	}

	public void updateVolumeKey() {
		AudioManager manager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamVolume(AudioManager.STREAM_RING, volumeKey,
				AudioManager.FLAG_PLAY_SOUND);
	}

	public void playKey() {
		updateVolumeKey();
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer
					.setDataSource("/system/media/audio/ui/KeypressSpacebar.ogg");
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mediaPlayer.prepare();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.stop();
					mp.release();
				}
			});
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mp.stop();
					mp.release();
					return true;
				}
			});
			mediaPlayer.start();
		} catch (IOException ex) {
		} catch (IllegalArgumentException ex) {
		} catch (IllegalStateException ex) {
		}
	}

	public static enum AdType { // 锟斤拷锟斤拷锟斤拷锟�
		TYPE_PICTURE, // 图片
		TYPE_VIDEO, // 锟斤拷频
		TYPE_MIXED, // 锟斤拷锟�
	}

	public static enum EncModeType { // 锟斤拷锟斤拷模式
		NONE, // 锟斤拷锟斤拷锟斤拷
		DES, // 3DES锟斤拷锟斤拷
		SM4, // SM4
		AES // AES
	}
}
