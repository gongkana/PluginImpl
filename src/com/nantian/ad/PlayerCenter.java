package com.nantian.ad;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.FileObserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.nantian.utils.HLog;

public class PlayerCenter  {

    public static enum PlayMode {
        ONLY_PICTURE, ONLY_VIDEO, MIXED,
    };

	private static PlayerCenter _instance;
	
	private Set<String>       folders  = new HashSet<String>();
    private ArrayList<String> playlist = new ArrayList<String>();
    private int      mVolume;
    private PlayMode mPlayMode;
    private int      playIndex;
    private String   mCurPlayFile;
    private AdFileObsver pictureFileObsver;
    private AdFileObsver videoFileObsver;
	private PlayerCenter() {
        init();
	}
	
	
	public static PlayerCenter instance() {
		if (null == _instance) {
			_instance = new PlayerCenter();
		}

		return _instance;
	}


    private void init() {
    	pictureFileObsver =new AdFileObsver(Environment.getExternalStorageDirectory()+"/Nantian/Web/res/ad");
    	videoFileObsver =new AdFileObsver(Environment.getExternalStorageDirectory()+"/Nantian/Web/res/video");
    }

    public void startWatch(){
    	pictureFileObsver.startWatching();
    	videoFileObsver.startWatching();
    }
    
    public void stopWatch(){
    	pictureFileObsver.stopWatching();
    	videoFileObsver.stopWatching();
    }
    public void setPlayMode(PlayMode mode) {
       
    }

    public int getPlaylistSize() {
        return playlist.size();
    }

    public String getPlayFilePath() {
        int count = playlist.size();
        if (0 == count) {
            return null;
        }

        if (playIndex < 0) {
            playIndex = count - 1;
        } else if (playIndex >= count) {
            playIndex = 0;
        }

        mCurPlayFile = playlist.get(playIndex);
        return mCurPlayFile;
    }

    public void next() {
        ++playIndex;
    }

    public void prev() {
        --playIndex;
    }

    public void setVolume(int volume) {
        if (mVolume == volume) {
            return;
        }



        //mView.action(PlayerFragment.VanHandler.UPDATE_VOLUME, 0);
    }

    public int getVolume() {
        return mVolume;
    }

    public PlayMode getPlayMode() {
        return mPlayMode;
    }

	public boolean playFile(String path) {
        playlist.clear();
        playlist.add(path);

        //mView.action(PlayerFragment.VanHandler.START_PLAY, 0);
        return true;
    }
	
	private class AdFileObsver extends FileObserver {

		public AdFileObsver(String path) {
			super(path);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onEvent(int event, String file) {
			HLog.e("playCenter", "event :"+event+",file = "+file);
			switch (event) {
			case FileObserver.CREATE:
				
				break;
			case FileObserver.DELETE:
				
				break;
			case FileObserver.MODIFY:
				
				break;
			}
			
		}
		
	}

}
