package com.iflytek.tts.TtsService;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


import com.nantian.utils.HLog;


public class AudioData {
	private static AudioTrack mAudio = null;
	private static final String TAG = "TtsService(audio)";
	private static int mStreamType = AudioManager.STREAM_SYSTEM;
	private static int mSampleRate = 16000;
	private static int mBuffSize = 8000;

	static {
		mAudio = new AudioTrack(mStreamType, mSampleRate,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				mBuffSize, AudioTrack.MODE_STREAM);
		HLog.d(TAG, " AudioTrack create ok");
	}

	/**
	 * For C call
	 */
	public static void onJniOutData(int len, byte[] data) {

		if (null == mAudio) {
			HLog.e(TAG, " mAudio null");
			return;
		}

		if (mAudio.getState() != AudioTrack.STATE_INITIALIZED) {
			HLog.e(TAG, " mAudio STATE_INITIALIZED");
			return;
		}

		try {
			mAudio.write(data, 0, len);
			mAudio.play();
		} catch (Exception e) {
			HLog.e(TAG, e.toString());
		}
	}

	/**
	 * For C Watch Call back
	 * 
	 * @param nProcBegin
	 */
	public static void onJniWatchCB(int nProcBegin) {
		HLog.d(TAG, "onJniWatchCB  process begin = " + nProcBegin);
	}
	
	public static void stop() {
		mAudio.stop();
	}
}
