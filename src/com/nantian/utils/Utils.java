package com.nantian.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.util.encoders.Base64Encoder;

import com.nantian.ad.DetailFile.MediaType;
import com.nantian.pluginImpl.DataException;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;

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

		// System.out.println("--- " + str);
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
	public static String getFileMD5(File file) throws DataException {
	
			try {
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				FileInputStream in = new FileInputStream(file);
				FileChannel ch = in.getChannel();
				MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY,
						0, file.length());
				messageDigest.update(byteBuffer);
				return bufferToHex(messageDigest.digest());
			} catch (NoSuchAlgorithmException e) {
				throw new DataException(-5, e);
			} catch (FileNotFoundException e) {
				throw new DataException(-5, "文件不存在",e);
			} catch (IOException e) {
				throw new DataException(-5, "IO异常",e);
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
		if (0 != length % 2) {
			return null;
		}

		byte[] byRet = new byte[length / 2];

		for (int i = 0; i < length / 2; i++) {
			String value = str.substring(i * 2, i * 2 + 2);
			byRet[i] = (byte) Integer.parseInt(value, 16);
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

	public static byte[] bitmapToByteArray(Bitmap bitmap) throws DataException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] data = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
			throw new DataException(-5,e);
		}
		return data;
	}

	public static String GetImageStr(String imgFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		return Base64.encodeToString(data, Base64.DEFAULT); // 返回Base64编码过的字节数组字符串
	}

	public static void savaBitmap(File dir, String fileName, Bitmap bitmap) throws DataException
			{
		saveInfo(dir, fileName, bitmapToByteArray(bitmap));
	}

	public static void saveInfo(File dir, String fileName, byte[] data) throws DataException
			 {
		try {
			OutputStream out = new FileOutputStream(new File(dir, fileName.trim()));
			out.write(data);
			out.close();
		} catch (FileNotFoundException e) {
			throw new DataException(-5);
		} catch (IOException e) {
			throw new DataException(-5);
		}
	}

	public static boolean delete(File file) {
		if (file.isDirectory()) {
			String[] list = file.list();

			if (null != list && list.length > 0) {
				for (String name : list) {
					if (!delete(new File(file, name))) {
						return false;
					}
				}
			}
		}

		return file.delete();
	}
	
	public static Bitmap getBitmapFromAsset(Context context,String filepath){
        AssetManager assets = context.getAssets();
        InputStream is = null;
        try {
        	is = assets.open(filepath);
        } catch (IOException e) {
        	e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeStream(is, null, options);
	}
	
	public static String getMediaTypeDir(MediaType type){
		String path = Environment.getExternalStorageDirectory()+"/Nantian/Web/www/res/image";
		switch (type) {
		case TYPE_VIDEO:
			path = Environment.getExternalStorageDirectory()+"/Nantian/Web/www/res/video";
			break;

		default:
			break;
		}
		return path;
	}
	
	public static String[] getFileList(final MediaType type) {
		File dir = new File(getMediaTypeDir(type));
		String[] files = null;
		switch (type) {
		case TYPE_TEXT:
			return null;
		case TYPE_GG_PIC://过滤广告图片 。jpeg
			files = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File file = new File(getMediaTypeDir(type) + File.separator + filename);
					HLog.e("", dir.getAbsolutePath()+",fileName ="+filename);
					//\.(jpg|png|jpeg|bmp|gif)$
					if (filename.toLowerCase().matches(".*(.jpg|.png|.bmp|.jpeg|.gif)$")) {
						return file.isFile();
					}
					return false;
				}
			});
			break;
		case TYPE_GY_PIC://过渡柜员图片
			files = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File file = new File(getMediaTypeDir(type) + File.separator + filename);
					if (filename.toLowerCase().matches(".*(.jpg|.png|.bmp|.jpeg|.gif)$")) {
						return file.isFile();
					}
					return false;
				}
			});
			break;
		case TYPE_VIDEO://过渡视频
			files = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File file = new File(getMediaTypeDir(type) + File.separator + filename);
					if (filename.toLowerCase().matches(".*(.mp4|.mpg|.avi|.wmv)$")) {
						return file.isFile();
					}
					return false;
				}
			});
			break;
		case TYPE_AUDIO://过渡音频
			files = dir.list();
			break;
		case TYPE_APK://过渡安装包
			files = dir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					File file = new File(getMediaTypeDir(type) + File.separator + filename);
					if (filename.toLowerCase().matches(".*(.apk|.patch)$")) {
						return file.isFile();
					}
					return false;
				}
			});
			break;
		case TYPE_OTHER:
			files = dir.list();
			break;
		default:
			break;
		}

		return files;
	}
}
