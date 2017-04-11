package com.nantian.pluginImpl;

import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.nantian.utils.HLog;
import com.nantian.utils.LogUtil;
import com.nantian.utils.Setting;
import com.nantian.utils.StringUtil;
import com.nantian.utils.Utils;
import com.van.hid.DESUtils;
import com.van.hid.CertificateCenter;
import com.van.hid.SMS4;
import com.van.hid.TKeyPair;


public class PasswordHandler {
	
	private static final String TAG = "PasswordHandler";
	
	private int type ;
	
	private static PasswordHandler instance;
	
	private TKeyPair keyPair;
	
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
			byte[] dst = null;
			if (keyPair != null){
				if (keyPair.getType() == 1){
					dst = CertificateCenter.SM2decrypt(keyPair.getPriKey(), data);
				}else{
					dst = CertificateCenter.RSAdecryptByPrivateKey(keyPair.getPriKey(), data);
				}
			}else{
			
			byte[] key = Setting.instance().getMainKey(mainIndex);
			LogUtil.i(TAG, "主密钥::" + Utils.getHexString(key,key.length));

			if (dencodeType == 0) {
				dst = SMS4.decodeSMS4(data, key);
			} else if(dencodeType == 1){
				dst = DESUtils.decode(data, key);
			}else{
				throw new DataException(-4);
			}


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
	
	public void getPublicKey(int encodeType,int length,boolean isCreate,JSONObject jsonOut)throws DataException, JSONException{
		if (encodeType == 1){
				if (!isCreate && null != keyPair && keyPair.getType() == encodeType){

				}else {
					keyPair = CertificateCenter.generateKeyPair();
					keyPair.setType(encodeType);
				}
				jsonOut.put("publicKey", StringUtil.bytesToHexString(keyPair.getPubKey()));
		}else if(encodeType == 2){
			if (!isCreate && null != keyPair && keyPair.getType() == encodeType){

			}else {
				if (length%512 != 0 || length/512 > 4){
					throw new DataException(-4,",长度参数不对："+length);
				}
				keyPair = CertificateCenter.RSAinitKey(length);
				keyPair.setType(encodeType);
			}
		
				RSAPublicKey pubKey = CertificateCenter.getRSAPublicKey(keyPair.getPubKey());
				byte [] pubContent = pubKey.getModulus().toByteArray();
				int keyLength = (int) Math.ceil(length/8.0);
				byte [] pubOut = new byte[keyLength];
				System.arraycopy(pubContent, pubContent.length-keyLength, pubOut, 0, keyLength);
				jsonOut.put("publicKey", StringUtil.bytesToHexString(pubOut));
				jsonOut.put("exponent", StringUtil.bytesToHexString(pubKey.getPublicExponent().toByteArray()));

		}else{
			throw new DataException(-4);
		}
	}
	public byte[] encodeByPublicKey(byte[] data) throws DataException{
		if (null == keyPair){
			throw new DataException(-4, ",公钥为空");
		}
	    if (keyPair.getType() == 2){  
	    	return CertificateCenter.RSAencryptByPublicKey(data,keyPair.getPubKey());
	    } else if (keyPair.getType() == 1){

	    	return CertificateCenter.SM2encrypt(keyPair.getPubKey(), data);
	    }else {
	    	throw new DataException(-4, ",没有此加密方式");
	    }
	}
	public byte[] encodeByPublicKeyAndMode(String big1,String big2,byte[] data) throws DataException{
		RSAPublicKey pub = CertificateCenter.getPublicKeyByBig(big1, big2);
		return CertificateCenter.RSAencryptByPublicKey(data, pub);
	}
	public byte[] decodeByPrivateKey(byte[] data) throws DataException{
		if (keyPair.getType() == 2){
			return CertificateCenter.RSAdecryptByPrivateKey(data, keyPair.getPriKey());
		}else{
			return CertificateCenter.SM2decrypt(keyPair.getPriKey(), data);
		}		
	}
	/**获取公钥
	 * @throws DataException */
	public byte[] getMainKey(int index) throws DataException{
		return Setting.instance().getMainKey(index);
	}
	
	/**恢复出厂密钥*/
	public void resetKey(int index) {
		Setting.instance().setMainKey(null, index);
	}
	
	/**初始化主密钥
	 * @throws DataException 
	 * @throws Exception */
	public byte[] initialKey(byte[] initialKey,int mainKeyNum,int decodeType) throws DataException  {
		
		byte[] mainKey ;
		if (decodeType == 0){
			mainKey = SMS4.decodeSMS4(initialKey, Utils.getHexData(Setting.instance().DEF_MAIN_KEY));
		}else if(decodeType ==1){
			mainKey = DESUtils.decode(initialKey, Utils.getHexData(Setting.instance().DEF_MAIN_KEY));
		}else{
			mainKey = Utils.getHexData(Setting.instance().DEF_MAIN_KEY);
		}
		Setting.instance().setMainKey(StringUtil.bytesToHexString(mainKey), mainKeyNum);
		
		return mainKey;
	}
	
	/** 获取密码键盘密钥校验值 
	 * 	keyType  密钥类型：0-主密钥，1-工作密钥
		keyIndex 密钥索引
	 *  encodeType 加密方式 0-sm4,1-des
	 * @throws DataException 
	 * @throws Exception 
	 * */
	public byte[] getKeyboardKeyVerify(int keyType,int keyIndex,int encodeType) throws DataException{
	
	HLog.e(TAG, "keyType = "+keyType+",keyIndex = "+keyIndex+",encodeType = "+encodeType);
		byte[] strKey = (0 == keyType) ? Setting.instance().getMainKey(keyIndex)
					: Setting.instance().getWorkKey(keyIndex);
		
		byte[] cvVerify ;
		byte [] cvVerifyValue;
		if (encodeType == 0){
			cvVerify = new byte[16];
			cvVerifyValue = SMS4.encodeSMS4(cvVerify, strKey);
		}else if (encodeType == 1){
			cvVerify = new byte[8];
			cvVerifyValue = DESUtils.encode(cvVerify, strKey);
		}else{
			cvVerifyValue = new byte[8];
		}
		return cvVerifyValue;
	}

	/**密文下载工作密钥
	 * @throws DataException */
	public void ciphWorkKey(byte[] data,int encodeType,int mainIndex,int workIndex) throws DataException {

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

	}
	
	/**明文下载主密钥*/
	public void plainMainKey(byte[] data,int mainIndex) {
		// Utils.showData(data, request.getLength());
			String mainKey = Utils.getHexString(data, data.length);
			Setting.instance().setMainKey(mainKey, mainIndex);
			LogUtil.i(TAG, "主密钥::" + mainKey);

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
	

 public byte[] encryptNum(String account,String password,int encodeType,int workIndex) throws DataException{
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
		} else if(aclen <12){
			throw new DataException(-104);
		}
		
	}
	Log.e(TAG, "account = "+account);
	byte[] dst = null;

	int keylen = 16;
	Log.e(TAG, "workKey:"+StringUtil.bytesToHexString(Setting.instance().getWorkKey(workIndex)));
 	int length  = password.length();
 	if (length%2 != 0){
 		password = password+"F";
 	}
	if(encodeType == 0){//SM4 国密加密
		byte[] accAndPassword;
		if (isAccount){
			accAndPassword = DESUtils.ansi98(newAcc, password, keylen);
		}else {
			accAndPassword = StringUtil.hexStringToBytes(password);
		}
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

			 	byte[] ps = StringUtil.hexStringToBytes(password);
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

	public void setPasswordLen(int length) {
		// TODO Auto-generated method stub
		
	}

	public void setEncodeType(int encodeType) throws DataException {
		if (encodeType==0 || encodeType == 1){
		Setting.instance().setPasswordKeyboardMode(encodeType);
		}else{
			throw new DataException(-4, ",加密方式参数设置错误："+encodeType);
		}
		
	}
}
