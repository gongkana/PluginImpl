package com.nantian.ad;

import android.os.Environment;


public class DetailFile {
	private String name;
	private MediaType type;
	private String path;

	public DetailFile(String name, MediaType type, String path) {
		super();
		this.name = name;
		this.type = type;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MediaType getType() {
		return type;
	}

	public void setType(MediaType type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DetailFile other = (DetailFile) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
	public static enum MediaType { // 播放类型
		TYPE_TEXT, // TTS
		TYPE_AUDIO, // 音乐[.mid;.mp3;.wav;.ogg]
		TYPE_VIDEO, // 视频[.rmvb;.avi;.wmv;.mp4;.3gp;.mov]
		TYPE_GG_PIC, // 广告图片[.jpg;.jpeg;.png;.bmp;.gif]
		TYPE_GY_PIC, // 柜员图片[.jpg;.jpeg;.png;.bmp;.gif]
		TYPE_APK, // 升级包
		TYPE_PDF, // PDF
		TYPE_OTHER, // 其他文件
		TYPE_PACKAGE, //
		TYPE_TTS, //
		TYPE_ZIP,//
		TYPE_BIN

	}
}