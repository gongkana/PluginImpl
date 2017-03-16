package com.nantian.ad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.WeakHashMap;

import com.nantian.ad.DetailFile.MediaType;
import com.nantian.plugininterface.IPlayer;
import com.nantian.utils.HLog;
import com.nantian.utils.Setting;
import com.nantian.utils.Utils;
import com.van.paperless.R;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class PlayerFragment extends Fragment implements ViewFactory,
		SurfaceHolder.Callback, OnTouchListener, OnGestureListener,
		OnClickListener {
	private static final String TAG = PlayerFragment.class.getSimpleName();
	/** 0-混合，1-图片，2，视频 */
	private int type = -1;
	private TextView tvTip;
	private SurfaceView surfaceView;
	private GallerySwitcher imageSwitcher;
	private SurfaceHolder surfaceHolder;
	private VanHandler handler;
	private MediaPlayer mediaPlayer;
	private View mPrevView, mNextView;
	private int index;
	public int GGTIME = 5;
	private ArrayList<DetailFile> playlist = new ArrayList<DetailFile>();
	private WeakHashMap<String, WeakReference<Drawable>> drableMap = new WeakHashMap<String, WeakReference<Drawable>>();
	@SuppressWarnings("deprecation")
	private GestureDetector detector;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private int playMode = PLAY_NEXT;

	private static final int PLAY_NEXT = 0;
	private static final int PLAY_PRE = 1;

	private DetailFile prePlayFile = null;

	private boolean isPause = false;
	private boolean isUpdated = false;
	// private RecommendPot pot;

	private Activity mActivity;

	int [] ani;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		HLog.e(TAG, "onCreateView");
		mActivity = getActivity();
		RelativeLayout relaView = new RelativeLayout(mActivity);
		handler = new VanHandler(this);
		detector = new GestureDetector(this);
		LayoutParams ra = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		surfaceView = new SurfaceView(mActivity);
		surfaceView.setLayoutParams(ra);
		surfaceView.setVisibility(View.GONE);
		ani = new int []{getResources().getIdentifier("slide_in_left", "anim", mActivity.getPackageName()),
				getResources().getIdentifier("slide_out_right", "anim", mActivity.getPackageName()),
				getResources().getIdentifier("slide_in_right", "anim", mActivity.getPackageName()),
				getResources().getIdentifier("slide_out_left", "anim", mActivity.getPackageName())};
		relaView.addView(surfaceView);
		imageSwitcher = new GallerySwitcher(mActivity);
		LayoutParams ra2 = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		imageSwitcher.setLayoutParams(ra2);
		imageSwitcher.setFactory(this);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		relaView.addView(imageSwitcher);
		LinearLayout linearLayout = new LinearLayout(mActivity);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams bottomRelat = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		bottomRelat.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
				RelativeLayout.TRUE);

		// pot = new RecommendPot(mActivity);
		// pot.setIndicatorChildCount(playlist.size());
		GGTIME = Setting.instance().getInt(mActivity, Setting.KEY_GG_TIME, 5);
		// mPrevView = view.findViewById(R.id.to_left);
		// mNextView = view.findViewById(R.id.to_right);

		// mPrevView.setOnClickListener(this);
		// mNextView.setOnClickListener(this);
		relaView.setOnTouchListener(this);
		// ImageView adView = (ImageView) view.findViewById(R.id.adplayer);
		setPalyerMode(Setting.instance().getInt(mActivity,
				Setting.KEY_PLAY_TYPE, 0));
		return relaView;
	}

	private void initMediaPlayer() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				handler.sendEmptyMessage(VanHandler.PLAY_NEXT);
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				handler.sendEmptyMessage(VanHandler.PLAY_NEXT);
				return true;
			}
		});

		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mediaPlayer.start();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		handler.removeMessages(VanHandler.PLAY_NEXT);
		handler.sendEmptyMessageDelayed(VanHandler.START_PLAY, 300);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		HLog.e(TAG, "onResume");
	}

	public void setPlayTime(int time) {
		HLog.e(TAG, "in time = "+time);
		GGTIME = time;
		handler.removeMessages(VanHandler.PLAY_NEXT);
		handler.removeMessages(VanHandler.START_PLAY);
		handler.sendEmptyMessageDelayed(VanHandler.PLAY_NEXT, GGTIME * 1000);
	}

	@Override
	public void onStop() {
		super.onStop();
		HLog.e(TAG, "onStop");
		try {
			if (mediaPlayer != null) {
				pause();
				mediaPlayer.release();
			}
		} catch (Exception e) {
			HLog.e(TAG, e);
		} finally {
			handler.removeMessages(VanHandler.PLAY_NEXT);
			handler.removeMessages(VanHandler.START_PLAY);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		String path = playlist.get(index).getPath();
		try {
			initMediaPlayer();
			mediaPlayer.reset();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mediaPlayer.setDisplay(surfaceHolder);
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepareAsync();
			// mediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
			handler.sendEmptyMessage(VanHandler.PLAY_NEXT);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mediaPlayer != null) {
			pause();
			mediaPlayer.release();
		}
	}

	@Override
	public View makeView() {
		ImageView imageView = new ImageView(getActivity());
		imageView.setScaleType(ScaleType.FIT_XY);
		return imageView;
	}

	public boolean isPicture(DetailFile file) {
		return file.getType() == MediaType.TYPE_GG_PIC;
	}

	public void playPicture(final String path, boolean toRight) {
		try {
			if (toRight) {
				if (null != prePlayFile
						&& prePlayFile.getType() == MediaType.TYPE_VIDEO) {
					imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(
							getActivity(), ani[0]));
					imageSwitcher.setOutAnimation(null);
					HLog.e(TAG,"to right, pre type is vedio");
				} else {
					imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(
							getActivity(), ani[0]));
					imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(
							getActivity(), ani[1]));
				}
			} else {
				if (null != prePlayFile
						&& prePlayFile.getType() == MediaType.TYPE_VIDEO) {
					imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(
							getActivity(), ani[2]));
					imageSwitcher.setOutAnimation(null);
					HLog.e(TAG,"to left, pre type is vedio");
				} else {
					imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(
							getActivity(), ani[2]));
					imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(
							getActivity(), ani[3]));
				}
			}

			Drawable drawable = null;
			if (isUpdated) {
				drableMap.remove(path);
			}
			WeakReference<Drawable> reference = (WeakReference<Drawable>) drableMap
					.get(path);
			if (reference != null) {
				drawable = (Drawable) reference.get();
			}
			if (drawable == null) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(new File(path));
					Bitmap bmp = BitmapFactory.decodeStream(fis);
					if (null != bmp) {
						drawable = new BitmapDrawable(mActivity.getResources(),
								bmp);// bmp);//
						if (drawable != null)
							drableMap.put(path, new WeakReference<Drawable>(
									drawable));
					}
				} catch (FileNotFoundException e) {
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (fis != null) {
						fis.close();
					}
				}
			}

			if (drawable != null) {
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
						getView().getWidth(), getView().getHeight());
				imageSwitcher.setImageFileAndDrawable(layoutParams, path,
						drawable);
			}else{
				HLog.e(TAG, "drawable is null .......");
			}
			// imageSwitcher.setImageFile(mActity, path);
		} catch (Exception e) {
			HLog.e(TAG, e.fillInStackTrace());
		}
		handler.sendEmptyMessageDelayed(VanHandler.PLAY_NEXT, GGTIME * 1000);
	}

	public void playVideo(String path) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mediaPlayer.setDisplay(surfaceHolder);
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepareAsync();
			// mediaPlayer.start();
		} catch (Exception e) {
			mediaPlayer.release();
			initMediaPlayer();
			handler.sendEmptyMessage(VanHandler.PLAY_NEXT);
		}
	}

	private static class VanHandler extends Handler {
		public static final int START_PLAY = 0;
		public static final int PLAY_NEXT = 1;
		public static final int CLICK_ENABLED = 2;

		private WeakReference<PlayerFragment> mOuter;

		public VanHandler(PlayerFragment playerFragment) {
			mOuter = new WeakReference<PlayerFragment>(playerFragment);
		}

		@SuppressWarnings("static-access")
		@Override
		public void handleMessage(Message msg) {
			PlayerFragment obj = mOuter.get();
			if (null == obj) {
				return;
			}
			switch (msg.what) {
			case START_PLAY:
				obj.play();
				break;
			case PLAY_NEXT:

				if (null != obj.playlist && obj.playlist.size() > 0) {
					obj.prePlayFile = obj.playlist.get(obj.index);
					if (++obj.index >= obj.playlist.size()) {
						obj.index = 0;
					}
					obj.playMode = obj.PLAY_NEXT;
					obj.play();
				}
				break;
			case CLICK_ENABLED:
				// obj.mPrevView.setClickable(true);
				// obj.mNextView.setClickable(true);
				break;
			default:
				break;
			}
		}
	}
	public void setPalyerMode(int arg0) {
		if (arg0 == this.type){
			return;
		}
		this.type = arg0;
		this.index = 0;
		playlist.clear();

		switch (type) {
		case 1:
			playlist.addAll(getFileList(MediaType.TYPE_GG_PIC, "*.*"));
			break;
		case 2:
			playlist.addAll(getFileList(MediaType.TYPE_VIDEO, "*.*"));
			break;
		case 0:
			playlist.addAll(getFileList(MediaType.TYPE_GG_PIC, "*.*"));
			playlist.addAll(getFileList(MediaType.TYPE_VIDEO, "*.*"));
			break;
		default:
			break;
		}
		Collections.sort(playlist, new SortFileByName());

		if (this.isResumed()) {
			handler.removeMessages(VanHandler.PLAY_NEXT);
			handler.sendEmptyMessageDelayed(VanHandler.START_PLAY, 300);
		}
	}

	public void updatePlayList(int type) {
		HLog.e(TAG, "update");
		this.type = type;
		// 播放列表有更新，清空缓存
		drableMap = new WeakHashMap<String, WeakReference<Drawable>>();
		DetailFile name = null;
		int preSize = playlist.size();
		if (preSize > 0) {
			name = playlist.get(index);
			playlist.clear();
		}

		switch (type) {
		case 1:
			playlist.addAll(getFileList(MediaType.TYPE_GG_PIC, "*.*"));
			break;
		case 2:
			playlist.addAll(getFileList(MediaType.TYPE_VIDEO, "*.*"));
			break;
		case 0:
			playlist.addAll(getFileList(MediaType.TYPE_GG_PIC, "*.*"));
			playlist.addAll(getFileList(MediaType.TYPE_VIDEO, "*.*"));
			break;
		default:
			break;

		}
		prePlayFile = name;
		Collections.sort(playlist, new SortFileByName());
		index = playlist.indexOf(name);
		Log.e(TAG, "index=" + index);
		if (index == -1 || preSize == 0) {
			index = 0;
			handler.removeMessages(VanHandler.PLAY_NEXT);
			handler.removeMessages(VanHandler.START_PLAY);
			handler.sendEmptyMessageDelayed(VanHandler.START_PLAY, 100);
		}

	}

	private ArrayList<DetailFile> getFileList(MediaType type, String name) {
		ArrayList<DetailFile> list = new ArrayList<DetailFile>();

		String directory = Utils.getMediaTypeDir(type) + File.separator;

		if (null == name || "*.*".equals(name)) {
			String[] names = Utils.getFileList(type);
			if (names == null) {
				return list;
			}
			for (int i = 0; i < names.length; i++) {
				list.add(new DetailFile(names[i], type, directory + names[i]));
			}
		} else {
			list.add(new DetailFile(name, type, directory + name));
		}

		return list;
	}

	public void play(boolean toRight) {
		if (getActivity() == null)
			return;

		if (null == playlist || 0 == playlist.size()) {

			surfaceView.setVisibility(View.INVISIBLE);
			imageSwitcher.setVisibility(View.INVISIBLE);
			// mPrevView.setVisibility(View.INVISIBLE);
			// mNextView.setVisibility(View.INVISIBLE);

			String strTip = "";
			switch (type) {
			case 1:
				// mPrevView.setVisibility(View.VISIBLE);
				// mNextView.setVisibility(View.VISIBLE);
				break;
			case 2:

				break;
			case 0:
				// mPrevView.setVisibility(View.VISIBLE);
				// mNextView.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}

			// tvTip.setText(strTip);
			// tvTip.setVisibility(View.VISIBLE);
			// tvTip.bringToFront();
		} else {
			String path = playlist.get(index).getPath();
			// pot.setCurrentScreen(index);
			if (isPicture(playlist.get(index))) {
				surfaceView.setVisibility(View.INVISIBLE);
				imageSwitcher.setVisibility(View.VISIBLE);
				// mPrevView.setVisibility(View.VISIBLE);
				// mNextView.setVisibility(View.VISIBLE);
				playPicture(path, toRight);
				HLog.e(TAG, "play picture ! path ="+path);
			} else {
				if (prePlayFile == null
						|| prePlayFile.getType() != MediaType.TYPE_VIDEO) {
					imageSwitcher.setVisibility(View.INVISIBLE);
					surfaceView.setVisibility(View.VISIBLE);
				} else if (prePlayFile.getType() == MediaType.TYPE_VIDEO) {
					surfaceView.setVisibility(View.VISIBLE);
					playVideo(path);
				}
				// mPrevView.setVisibility(View.INVISIBLE);
				// mNextView.setVisibility(View.INVISIBLE);
			}

		}
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		detector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		isPause = true;
		handler.removeMessages(VanHandler.PLAY_NEXT);
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			pre();
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			next();
		}

		return false;
	}

	public void pre() {
		if (null != playlist && playlist.size() > 0) {
			prePlayFile = playlist.get(index);
			if (index == 0) {
				index = playlist.size() - 1;
			} else {
				index--;
			}
			playMode = PLAY_PRE;
			handler.removeMessages(VanHandler.PLAY_NEXT);
			play(false);
		}
	}

	public void next() {
		handler.removeMessages(VanHandler.PLAY_NEXT);
		handler.sendEmptyMessage(VanHandler.PLAY_NEXT);
	}

	@Override
	public void onLongPress(MotionEvent arg0) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// isPause = false;
		// handler.removeMessages(VanHandler.PLAY_NEXT);
		// handler.sendEmptyMessage(VanHandler.PLAY_NEXT);
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// getActivity().unregisterReceiver(broadcastRec);
	}

	
	public ArrayList<String> getPlayList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void init() {
		// TODO Auto-generated method stub

	}


	public void pause() {
		isPause = true;
		handler.removeMessages(VanHandler.PLAY_NEXT);

	}


	public void play(String arg0) {

	}

	public void play() {
		isPause = false;
		play(true);

	}


	public void start() {
		// TODO Auto-generated method stub

	}


	public void updataPlayerList() {
		updatePlayList(type);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}


}
