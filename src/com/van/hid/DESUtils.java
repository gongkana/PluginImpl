package com.van.hid;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.nantian.pluginImpl.DataException;
import com.nantian.utils.HLog;
import com.nantian.utils.StringUtil;

public class DESUtils {
	
	public static byte[] encode(byte[]data,byte[] key) throws DataException {
		byte [] rsp = null;
        if (key.length == 24 ) { 
        	rsp =  encode_3des(data, key);
        }else if (key.length == 16){
        	byte [] dstKey = new byte [24];
        	System.arraycopy(key, 0, dstKey, 0, 16);
        	System.arraycopy(key, 0, dstKey, 16, 8);
        	rsp =  encode_3des(data, dstKey);
        } else if(key.length == 8){  
        	rsp = encode_1des(data, key);
        }else {
        	new DataException(-103,",key is "+key+",length is "+key.length);
        }
		return rsp;
	}
	private static byte[] encode_3des(byte[]data,byte[] key) throws DataException {
		int len = ((data.length-1)/8+1)*8;
		return code_comm(buildData(data,len), key, true,3);     
	}
	private static byte[] buildData(byte[] data,int i8){
        int len = (data.length+i8-1)/i8*i8;  
        
        byte [] needData = new byte[len];  
         
        System.arraycopy(data, 0, needData, 0, data.length); 
        return needData;
	}
	private static byte[] encode_2des(byte[]data,byte[] key) throws Exception{
		return code_comm(buildData(data,8), key,true, 2);      
	}
	
	private static byte[] encode_1des(byte[]data,byte[] key) throws DataException {
		return code_comm(buildData(data,8), key,true, 1);
	}
	
	private static byte[]code_comm(byte[]data,byte[] key,boolean isEncode,int mode) throws DataException {
		KeySpec ks = null;  
		String Algorithm = "DESede";
		String arg = "DESede/ECB/NoPadding";
		//String arg = "DESede/CBC/PKCS5Padding";
		Cipher c;
		try {
			switch (mode) {
			case 1:
				Algorithm = "DES";

					ks = new DESKeySpec(key);

				arg = "DES/ECB/NoPadding";
				break;
			case 3:
				ks = new DESedeKeySpec(key);
			default:
				break;
			}
			
			SecretKeyFactory kf = SecretKeyFactory.getInstance(Algorithm);  
			SecretKey ky = kf.generateSecret(ks);  
			c = Cipher.getInstance(arg); 
			if(isEncode){
				c.init(Cipher.ENCRYPT_MODE, ky);
			}else{
				c.init(Cipher.DECRYPT_MODE, ky);
			}
			return c.doFinal(data);
		} catch (InvalidKeyException e) {
			throw new DataException(-103,e);
		} catch (NoSuchAlgorithmException e) {
			throw new DataException(-5,e);
		} catch (InvalidKeySpecException e) {
			throw new DataException(-103,e);
		} catch (NoSuchPaddingException e) {
			throw new DataException(-5,e);
		}catch (IllegalBlockSizeException e) {
			throw new DataException(-103,e);
		} catch (BadPaddingException e) {
			throw new DataException(-103,e);
		} 
	}
	 
   
    public static byte[] decode(byte data[], byte key[]) throws DataException  {  
		byte [] rsp = null;
        if (key.length == 24 ) { 
        	rsp =  decode_3des(data, key);
        }else if (key.length == 16){
        	byte [] dstKey = new byte [24];
        	System.arraycopy(key, 0, dstKey, 0, 16);
        	System.arraycopy(key, 0, dstKey, 16, 8);
        	rsp =  decode_3des(data, dstKey);
        } else if(key.length == 8){  
        	rsp = decode_1des(data, key);
        }else {
        	new DataException(-103,",key is "+key+",length is "+key.length);
        }
		return rsp;
    }
	private static byte[] decode_1des(byte[] data, byte[] key) throws DataException  {
		// TODO Auto-generated method stub
		return code_comm(buildData(data,8), key,false, 1);
	}
	private static byte[] decode_3des(byte[] data, byte[] key) throws DataException  {
		// TODO Auto-generated method stub
		int len = ((data.length-1)/8+1)*8;
		return code_comm(buildData(data,len), key,false, 3);
	}
	
	public static byte[] ansi98(String account, String pass,int length)  {
		// TODO Auto-generated method stub
		byte [] ac = new byte [length];
		byte [] pa = new byte [length];
		byte [] dst = new byte [length];
		String newPass = pass;
	 	if (pass.length()%2 != 0){
	 		newPass = pass+"F";
	 	}
		byte [] acSrc = StringUtil.hexStringToBytes(account);
		int acstart = 8-acSrc.length%8;
		if (acSrc.length>length){
			acstart = 0;
		}
		byte [] psSrc = StringUtil.hexStringToBytes(newPass);
		int srcLen = acSrc.length>length?length:acSrc.length;
		int passLen = psSrc.length>length?length:psSrc.length;
		System.arraycopy(acSrc, 0, ac, acstart, srcLen);
		HLog.e("", "account :"+StringUtil.bytesToHexString(ac));
		Arrays.fill(pa, (byte) 0xFF);
		pa[0] = (byte) pass.length();
		if (passLen> length-1){
			passLen = length-1;
		}
		System.arraycopy(psSrc, 0, pa, 1, passLen);
		HLog.e("", "password :"+StringUtil.bytesToHexString(pa));
		for (int i = 0; i < length; i++) {
			dst[i] = (byte) (ac[i]^pa[i]);
		}
		HLog.e("", "ansi :"+StringUtil.bytesToHexString(dst));
		return dst;
	}
}
