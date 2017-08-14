package com.van.hid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Certificate;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import android.util.Base64;

import com.nantian.pluginImpl.DataException;

public class CertificateCenter 
{
    static {
        Security.addProvider(new BouncyCastleProvider());
        java.security.Provider[] p_array=java.security.Security.getProviders();
        for (int i=1;i<= p_array.length;i++){
        	System.out.println("Provider["+i+"]="+p_array[i-1].getName());
        }
    }
	public static byte[] SM2encrypt(byte[] publicKey, byte[] data)
	{
		if (publicKey == null || publicKey.length == 0)
		{
			return null;
		}
		
		if (data == null || data.length == 0)
		{
			return null;
		}
		
		byte[] source = new byte[data.length];
		System.arraycopy(data, 0, source, 0, data.length);

        byte[] formatedPubKey;
        if (publicKey.length == 64){
            //娣诲姞涓�瀛楄妭鏍囪瘑锛岀敤浜嶦CPoint瑙ｆ瀽
            formatedPubKey = new byte[65];
            formatedPubKey[0] = 0x04;
            System.arraycopy(publicKey,0,formatedPubKey,1,publicKey.length);
        }
        else
            formatedPubKey = publicKey;
		
		TCipher cipher = new TCipher();
		SM2 sm2 = SM2.Instance();
		ECPoint userKey = sm2.ecc_curve.decodePoint(formatedPubKey);
		
		ECPoint c1 = cipher.Init_enc(sm2, userKey);
		cipher.Encrypt(source);
		byte[] c3 = new byte[32];
		cipher.Dofinal(c3);
		
		DERInteger x = new DERInteger(c1.getX().toBigInteger());
		DERInteger y = new DERInteger(c1.getY().toBigInteger());
		DEROctetString derDig = new DEROctetString(c3);
		DEROctetString derEnc = new DEROctetString(source);
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(x);
		v.add(y);
		v.add(derDig);
		v.add(derEnc);
		DERSequence seq = new DERSequence(v);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DEROutputStream dos = new DEROutputStream(bos);
        try {
            dos.writeObject(seq);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static byte[] SM2decrypt(byte[] privateKey, byte[] encryptedData)
	{
		if (privateKey == null || privateKey.length == 0)
		{
			return null;
		}
		
		if (encryptedData == null || encryptedData.length == 0)
		{
			return null;
		}
		
		byte[] enc = new byte[encryptedData.length];
		System.arraycopy(encryptedData, 0, enc, 0, encryptedData.length);
		
		SM2 sm2 = SM2.Instance();
		BigInteger userD = new BigInteger(1, privateKey);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(enc);
		ASN1InputStream dis = new ASN1InputStream(bis);
        try {
           // DERObject derObj = dis.readObject();
            //ASN1Sequence asn1 = (ASN1Sequence) derObj;
            ASN1Sequence asn1 = (ASN1Sequence) dis.readObject();
            ASN1Integer x = (ASN1Integer) asn1.getObjectAt(0);
            ASN1Integer y = (ASN1Integer) asn1.getObjectAt(1);
            ECPoint c1 = sm2.ecc_curve.createPoint(x.getValue(), y.getValue(), true);

            TCipher cipher = new TCipher();
            cipher.Init_dec(userD, c1);
            DEROctetString data = (DEROctetString) asn1.getObjectAt(3);
            enc = data.getOctets();
            cipher.Decrypt(enc);
            byte[] c3 = new byte[32];
            cipher.Dofinal(c3);
            return enc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

	

    public static TKeyPair generateKeyPair(){
        SM2 sm2 = SM2.Instance();
        AsymmetricCipherKeyPair keypair = sm2.ecc_key_pair_generator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) keypair.getPublic();

//        System.out.println("绉侀挜: " + ecpriv.getD().toString(16).toUpperCase());
//        System.out.println("鍏挜: " + ecpub.getQ().getX().toBigInteger().toString(16).toUpperCase() +
//                ecpub.getQ().getY().toBigInteger().toString(16).toUpperCase());

        byte[] priKey = new byte[32];
        byte[] pubKey = new byte[64];

        byte[] bigNumArray = ecpriv.getD().toByteArray();
        System.arraycopy(bigNumArray, bigNumArray[0]==0?1:0, priKey, 0, 32);
        System.arraycopy(ecpub.getQ().getEncoded(), 1, pubKey, 0, 64);

//        System.out.println("绉侀挜bigNumArray: " + Util.getHexString(bigNumArray));
//        System.out.println("绉侀挜: " + Util.getHexString(priKey));
//        System.out.println("鍏挜: " + Util.getHexString(pubKey));

        return new TKeyPair(priKey, pubKey);
    }
    
    public static final String KEY_ALGORITHM = "RSA";  

	 /** 
     * 解密<br> 
     * 用私钥解密 
     *  
     * @param data 
     * @param key 
     * @return 
	 * @throws DataException 
     * @throws Exception 
     */  
    public static byte[] RSAdecryptByPrivateKey(byte[] data, byte[] keyBytes) throws DataException   {   
  
        // 取得私钥  
        try {
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
			Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
  
			// 对数据解密  
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
			cipher.init(Cipher.DECRYPT_MODE, privateKey);  
  
			return cipher.doFinal(data);
		} catch (InvalidKeyException e) {
			throw new DataException(-5, e);
		} catch (NoSuchAlgorithmException e) {
			throw new DataException(-5, e);
		} catch (InvalidKeySpecException e) {
			throw new DataException(-5, e);
		} catch (NoSuchPaddingException e) {
			throw new DataException(-5, e);
		} catch (IllegalBlockSizeException e) {
			throw new DataException(-5, e);
		} catch (BadPaddingException e) {
			throw new DataException(-5, e);
		}  
    }  
  
    /** 
     * 解密<br> 
     * 用公钥解密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] RSAdecryptByPublicKey(byte[] data,  byte[] keyBytes)  
            throws Exception {  
        // 取得公钥  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
  
        // 对数据解密  
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
        cipher.init(Cipher.DECRYPT_MODE, publicKey);  
  
        return cipher.doFinal(data);  
    }  
  
    /** 
     * 加密<br> 
     * 用公钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws DataException 
     * @throws Exception 
     */  
    public static byte[] RSAencryptByPublicKey(byte[] data, PublicKey pubKey) throws DataException {  
  

  
			// 对数据加密  
			try {
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
				cipher.init(Cipher.ENCRYPT_MODE, pubKey);  
  
				return cipher.doFinal(data);
			} catch (InvalidKeyException e) {
				throw new DataException(-5, e);
			} catch (NoSuchAlgorithmException e) {
				throw new DataException(-5, e);
			} catch (NoSuchPaddingException e) {
				throw new DataException(-5, e);
			} catch (IllegalBlockSizeException e) {
				throw new DataException(-5, e);
			} catch (BadPaddingException e) {
				throw new DataException(-5, e);
			}
 
    }  
    
    /** 
     * 加密<br> 
     * 用公钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws DataException 
     * @throws Exception 
     */  
    public static byte[] RSAencryptByPublicKey(byte[] data, byte[] keyBytes) throws DataException {  
  
        // 取得公钥  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        try {
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
			Key publicKey = keyFactory.generatePublic(x509KeySpec);  
  
			// 对数据加密  
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
  
			return cipher.doFinal(data);
		} catch (InvalidKeyException e) {
			throw new DataException(-5, e);
		} catch (NoSuchAlgorithmException e) {
			throw new DataException(-5, e);
		} catch (InvalidKeySpecException e) {
			throw new DataException(-5, e);
		} catch (NoSuchPaddingException e) {
			throw new DataException(-5, e);
		} catch (IllegalBlockSizeException e) {
			throw new DataException(-5, e);
		} catch (BadPaddingException e) {
			throw new DataException(-5, e);
		}  
    }  
  
    /** 
     * 加密<br> 
     * 用私钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] RSAencryptByPrivateKey(byte[] data, byte[] keyBytes)  
            throws Exception {  
  
        // 取得私钥  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
  
        // 对数据加密  
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
  
        return cipher.doFinal(data);  
    }  
  

  
    /** 
     * 初始化密钥 
     *  
     * @return 
     * @throws DataException 
     * @throws Exception 
     */  
    public static TKeyPair RSAinitKey(int Length) throws DataException {  
        KeyPairGenerator keyPairGen;
		try {
			keyPairGen = KeyPairGenerator  
			        .getInstance(KEY_ALGORITHM);
	        keyPairGen.initialize(Length);  
	        
	        KeyPair keyPair = keyPairGen.generateKeyPair();  
	  
	        // 公钥  
	        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  
	  
	        // 私钥  
	        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();  
	  
	        return new TKeyPair(privateKey.getEncoded(),publicKey.getEncoded());  
		} catch (NoSuchAlgorithmException e) {
			throw new DataException(-5,e);
		}  

    } 
    public static RSAPublicKey getRSAPublicKey(byte[] keyBytes) throws DataException {
        try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			RSAPublicKey publicKey = null;
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new DataException(-5, e);
		} catch (InvalidKeySpecException e) {
			throw new DataException(-5, e);
		}
    }

    private static PrivateKey getPrivateKey(byte[] keyBytes) {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = null;

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            privateKey = null;
        }

        return privateKey;
    }
    public static byte[] generateCert(byte[] pubKeyByte, byte[] priKeyByte) throws CertificateEncodingException, InvalidKeyException, NoSuchProviderException, SecurityException, SignatureException, DataException {
    	PublicKey pubKey = getRSAPublicKey(pubKeyByte);
    	PrivateKey priKey = getPrivateKey(priKeyByte);
		X509Certificate cert = null;
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		// 设置序列号
		certGen.setSerialNumber(new BigInteger("020310811"));
		
		// 设置颁发者
		certGen.setIssuerDN(new X500Principal("C=CN,ST=BJ,L=BJ,O=SICCA,OU=SC,CN=Van"));
		
		// 设置有效期
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(today);
		cal.add(Calendar.YEAR, 10);
		Date until = cal.getTime();
		
		certGen.setNotBefore(today);
		certGen.setNotAfter(until);
		
		// 设置使用者
		certGen.setSubjectDN(new X500Principal("C=CN,ST=BJ,L=BJ,O=Nantian,OU=SC,CN=Nantian"));
		
		// 公钥
		certGen.setPublicKey(pubKey);
		
		// 签名算法
		//String signMode = hashAlgorithm + "With" + encryptionAlgorithm + "Encryption";
		//certGen.setSignatureAlgorithm(signMode);
		certGen.setSignatureAlgorithm("SHA1WithRSA");
		cert = certGen.generateX509Certificate(priKey, "BC");
		
		return cert.getEncoded();
    }
    
    public static PublicKey getPublicKeyFromCer(byte[] serbyte) throws DataException {  
        try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");   
			ByteArrayInputStream in = new ByteArrayInputStream(serbyte);
			java.security.cert.Certificate crt = cf.generateCertificate(in);  
			PublicKey publicKey = crt.getPublicKey();  
			return publicKey;
		} catch (CertificateException e) {
			throw new DataException(-5, e);
		}  
    }
    public static RSAPublicKey getPublicKeyByBig(String big1,String big2) throws DataException{
    	try {
			BigInteger b1=new BigInteger(big1,16);
			
			BigInteger b2=new BigInteger(big2,16);

			RSAPublicKeySpec rsaPubKS=new RSAPublicKeySpec(new BigInteger(big1,16),new BigInteger(big2,16));
			KeyFactory kf=KeyFactory.getInstance("RSA");
			return (RSAPublicKey) kf.generatePublic(rsaPubKS);
		} catch (NoSuchAlgorithmException e) {
			throw new DataException(-5, e);
		} catch (InvalidKeySpecException e) {
			throw new DataException(-5, e);
		}
    }
    	
    
}
