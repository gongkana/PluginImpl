package com.nantian.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

import android.graphics.Bitmap;

public class Utils {
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static boolean isEmpty(String text) {
		return null == text || "".equals(text);
	}

	public static void showData(byte[] data, int length) {
		if (length <= 0) {
			return;
		}
		
		String str = "";

		StringBuilder sb = new StringBuilder(length * 3);

		final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		for (int i = 0; i < length; i++) {
			int value = data[i] & 0xff;
			sb.append(HEX[value / 16]).append(HEX[value % 16]).append(' ');
		}

		str = sb.substring(0, sb.length() - 1);

		//System.out.println("--- " + str);
	}

	public static void showString(String str, int time) {
		System.out.println("------ " + str);
	}

	public static String getSysProp(String key) {
		File file = new File("/system/build.prop");

		if (!file.isFile() || !file.exists()) {
			return null;
		}

		String value = null;

		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					file));
			BufferedReader br = new BufferedReader(read);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(key)) {
					value = line.substring(key.length() + 1);
					break;
				}
			}

			read.close();
		} catch (Exception e) {
		}

		return value;
	}

	@SuppressWarnings("resource")
	public static String getFileMD5(File file) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			FileInputStream in = new FileInputStream(file);
			FileChannel ch = in.getChannel();
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY,
					0, file.length());
			messageDigest.update(byteBuffer);
			return bufferToHex(messageDigest.digest());
		} catch (Exception e) {
			return null;
		}
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static byte[] getHexData(String str) {
		str = str.replace(" ", "");
		
		int length = str.length();
		if (0 != length%2) {
			return null;
		}
		
		byte[] byRet = new byte[length/2];
		
		for (int i = 0; i < length/2; i++) {
			String value = str.substring(i*2, i*2+2);
			byRet[i] = (byte)Integer.parseInt(value, 16);
		}
		
		return byRet;
	}
	
	public static String getHexString(byte[] data, int length) {
		StringBuilder sb = new StringBuilder(length * 2);

		final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		for (int i = 0; i < length; i++) {
			int value = data[i] & 0xff;
			sb.append(HEX[value / 16]).append(HEX[value % 16]);
		}
		
		return sb.toString();
	}
	
	public static byte[] bitmapToByteArray(Bitmap bitmap) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] data = baos.toByteArray();
		baos.close();
		return data;
	}
	
	public static void savaBitmap(File dir, String fileName,Bitmap bitmap) throws Exception{
		saveInfo(dir, fileName, bitmapToByteArray(bitmap));
	}
	public static void saveInfo(File dir, String fileName, byte[] data)
			throws Exception {
		OutputStream out = new FileOutputStream(new File(dir, fileName.trim()));
		out.write(data);
		out.close();
	}
}
