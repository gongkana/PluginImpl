package com.iflytek.tts.TtsService;

import java.io.File;
import java.util.LinkedList;


public final class Tts extends Thread {
	static {
		System.loadLibrary("Aisound");
	}
	
	private static Tts _instance;
	private static boolean isCreated  = false;
	private LinkedList<String> strList;

	public static native int JniGetVersion();
	public static native int JniCreate(String resFilename);
	public static native int JniDestory();
	public static native int JniStop();
	public static native int JniSpeak(String text);
	public static native int JniSetParam(int paramId, int value);
	public static native int JniGetParam(int paramId);
	public static native int JniIsPlaying();
	public static native boolean JniIsCreated();
	
	private Tts() {
		strList = new LinkedList<String>();
		start();
	}
	
	public static boolean create() {
		if (isCreated) {
			return isCreated;
		}
		
		String path = "/system/lib/Resource.irf";
		
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}
		
		JniCreate(path);
		JniSetParam(256, 1);
		JniSetParam(1280, 3);
		
		if (null == _instance) {
			_instance = new Tts();
		}
		
		isCreated = true;
		return isCreated;
	}
	
	public static void destroySelf() {
		isCreated = false;
		_instance = null;
		JniStop();
		JniDestory();
	}
	
	public static void speak(final String str) {
		if (null != _instance) {
			_instance.addString(str);
		}
	}
	
	public static void clear() {
		if (null != _instance) {
			JniStop();
			_instance.strList.clear();
		}
	}
	
	public void addString(final String str) {
		synchronized (_instance) {
			strList.add(str);
			notify();
		}
	}
	
	@Override
	public void run() {
		while (isCreated) {
			String str = null;
			synchronized (_instance) {
				str = strList.pollFirst();
				if (null == str) {
					AudioData.stop();
					try {
						wait();
					} catch (InterruptedException e) {
					}
					
					str = strList.pollFirst();
				}
			}
			if (null != str) {
				JniSpeak(str);
			}
		}
	}
}
