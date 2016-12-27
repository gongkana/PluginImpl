package com.nantian.pluginImpl;

import java.util.Arrays;

import android.text.TextUtils;
import android.util.Log;

import com.nantian.utils.LogUtil;
import com.nantian.utils.Setting;
import com.nantian.utils.Setting.EncModeType;
import com.nantian.utils.StringUtil;
import com.nantian.utils.Utils;
import com.van.hid.DESUtils;
import com.van.hid.SMS4;


public class PasswordHandler {
	
	private static final String TAG = "PasswordHandler";
	
	private int type ;
	
	private static PasswordHandler instance;
	
	private PasswordHandler(){

	}
	
	public static PasswordHandler instance(){
		if (instance == null){
			instance = new PasswordHandler();
		}
		return instance;
	}
	/**密文下载主密钥*/
	public boolean ciphMainKey(byte[] data,int dencodeType,int mainIndex) {

		try {

			byte[] key = Setting.instance().getMainKey(mainIndex);
			LogUtil.i(TAG, "主密钥::" + Utils.getHexString(key,key.length));
			byte[] dst = new byte[data.length];
			if (dencodeType == 0) {
				dst = SMS4.decodeSMS4(data, key);
			} else {
				dst = DESUtils.decode(data, key);
			}

			String mainKey = Utils.getHexString(dst, dst.length);
			Setting.instance().setMainKey(mainKey, mainIndex);

			LogUtil.i(TAG, "主密钥::" + mainKey);
			return true;
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString(), new Exception());
			return false;
		}
	}
	/**密文下载工作密钥*/
	public boolean ciphWorkKey(byte[] data,int encodeType,int mainIndex,int workIndex) {
		try {

			LogUtil.i(TAG, "密钥号::" + workIndex);
			int len = data.length;// 工作密钥长度
			String datastring = Utils.getHexString(data, data.length);
			LogUtil.e(TAG, "miwen::" + datastring);
			byte[] key = Setting.instance().getMainKey(mainIndex);
			Log.e("","main key:"+Utils.getHexString(key, key.length));
			byte[] dst = new byte[data.length];
			if (encodeType == 0) {
				dst = SMS4.decodeSMS4(data, key);
			} else {
				dst = DESUtils.decode(data, key);
			}
			String workKey = Utils.getHexString(dst, dst.length);
			Setting.instance().setWorkKey(workKey, workIndex);
			LogUtil.e(TAG, "工作密钥::" + workKey);
			return true;
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString(), new Exception());
			return false;
		}
	}
	
	/**明文下载主密钥*/
	public boolean plainMainKey(byte[] data,int mainIndex) {
		// Utils.showData(data, request.getLength());
		try {
			String mainKey = Utils.getHexString(data, data.length);
			Setting.instance().setMainKey(mainKey, mainIndex);
			LogUtil.i(TAG, "主密钥::" + mainKey);
			return true;
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString(), new Exception());
			return false;
		}
	}

	public boolean plainWorkKey(byte[] data,int workIndex) {
		try {
			String workKey = Utils.getHexString(data, data.length);
			LogUtil.i(TAG, "工作密钥::" + workKey);
			Setting.instance().setWorkKey(workKey, workIndex);
			return true;
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString(), new Exception());
			return false;
		}
	}
	
	public boolean setKeyboardWordmode(int mode) {
		// Utils.showData(data, request.getLength());
		try {
			if (mode == 0) {
				Setting.instance().setKeyboardEncryption(EncModeType.NONE);
				LogUtil.i(TAG, "非加密模式");
			} else if (mode == 1) {
				Setting.instance().setKeyboardEncryption(EncModeType.DES);
				LogUtil.i(TAG, "加密模式");
			} else if (mode == 2) {
				Setting.instance().setKeyboardEncryption(EncModeType.SM4);
				LogUtil.i(TAG, "国密加密模式");
			} else {
				LogUtil.i(TAG, "默认模式");
			}
			return true;

		} catch (Exception e) {
			LogUtil.e(TAG, e.toString(), new Exception());
			return false;
		}
	}
 public byte[] encryptNum(String account,String password,int encodeType,int workIndex) throws Exception{
	if (TextUtils.isEmpty(password)){
		Log.e(TAG, "password is null!");
		return null;
	}
	boolean isAccount = !TextUtils.isEmpty(account);
	String newAcc = account;
	int aclen = account.length();
	if (isAccount){
		
		if(aclen>=13){
			newAcc = account.substring(aclen-13,aclen-1);
		}
		
	}
	Log.e(TAG, "account = "+account);
	byte[] dst = null;
	byte[] ps = StringUtil.hexStringToBytes(password);
	Log.e(TAG, StringUtil.bytesToHexString(ps));
	int keylen = 16;
	Log.e(TAG, "workKey:"+StringUtil.bytesToHexString(Setting.instance().getWorkKey(workIndex)));
	if(encodeType == 0){//SM4 国密加密
		byte[] accAndPassword = DESUtils.ansi98(newAcc, password, keylen);
		Log.e(TAG, "98 sm4:"+StringUtil.bytesToHexString(accAndPassword));
		dst = SMS4.encodeSMS4(accAndPassword, Setting.instance().getWorkKey(workIndex));
		Log.e(TAG, "endoce sm4:"+StringUtil.bytesToHexString(dst));
	 }else{
		 byte[] key = Setting.instance().getWorkKey(workIndex);
		 if (password.length() <= 8){
			 keylen = 8;
		 }
		 if (isAccount){
				//byte[] Pin = new byte[8];
				//Arrays.fill(Pin, (byte) 0);
				//account = account.substring(aclen-13,aclen);
				//VanHid.Ansi98(password.getBytes(), password.getBytes().length, newAcc.getBytes(), newAcc.getBytes().length, Pin);
				//Log.e(TAG, "98 des:"+StringUtil.bytesToHexString(Pin));
			   byte[] accAndPassword = DESUtils.ansi98(newAcc, password, keylen);
				Log.e(TAG, "98 des:"+StringUtil.bytesToHexString(accAndPassword));

				dst = DESUtils.encode(accAndPassword, key);
				Log.e(TAG, "des encode:"+StringUtil.bytesToHexString(dst));
		 }else{
				byte[] src = new byte[ps.length <= 8 ? 8 : 16];
				Arrays.fill(src, (byte) 0xFF);
				System.arraycopy(password, 0, src, 0, ps.length);
				Log.e(TAG, "98 des:"+StringUtil.bytesToHexString(src));
				dst = DESUtils.encode(src, key);

		 }
		 
	 }
	Log.e(TAG, StringUtil.bytesToHexString(dst));
	 return dst;
 }
 
 
	public byte[] encryptString(String data,int mode,int encodeMode,int index) {
		try {
			byte[] src = StringUtil.hexStringToBytes(data);
					int len = src.length;			
			byte[] key  = null;
			if (mode == 0){//
				key = Setting.instance().getMainKey(index);
			}else{
				key = Setting.instance().getWorkKey(index);
			}
			Log.e(TAG, StringUtil.bytesToHexString(key));
			Log.e(TAG, "data:"+StringUtil.bytesToHexString(src));
			if (encodeMode == 0) {//sm4

				return SMS4.encodeSMS4(src, key);

			} else {

				return DESUtils.encode(src, key);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString(), new Exception());
		}
		return null;
	}
	
	public void resetMainkey(){
		Setting.instance().setMainKey(null, -1);
	}
}
