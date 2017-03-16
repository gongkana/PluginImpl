package com.nantian.pluginImpl;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.nantian.utils.DeviceInfo;
import com.nantian.utils.HLog;
import com.nantian.utils.Setting;
import com.nantian.utils.Utils;
import com.nantian.utils.VanRequest;

public class SystemHandler {

	private static final String TAG = "PasswordHandler";

	private static SystemHandler instance;

	private VanRequest mVanRequest;
	private double BatteryV; // 电池电压

	private double BatteryT; // 电池温度


	private Context mContext;

	private boolean isInit;
	
	private SystemHandler() {

	}

	public void init(Context context) {

		this.mContext = context;
		mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
	}

	public void destory(){
		mContext.unregisterReceiver(mBroadcastReceiver);
	}


	public static SystemHandler instance() {
		if (instance == null) {
			instance = new SystemHandler();
		}
		return instance;
	}

	public String getVersion() {
		String version = "";
		PackageManager manager = mContext.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(mContext.getPackageName(),
					0);
			version = info.versionName;// 获取版本号
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 音量设置
	 */
	public void settingVolume(int volumeType, int volumeValue) {
		int type = -1;
		switch (volumeType) {
		case 1:
			type = AudioManager.STREAM_SYSTEM;
			break;
		case 2:
			type = AudioManager.STREAM_MUSIC;
			break;
		default:
			break;
		}
		if (type == -1) {
			return;
		}
		AudioManager audioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = audioManager.getStreamMaxVolume(type);// 取得最大音量

		int volume = volumeValue * maxVolume / 100;
		audioManager
				.setStreamVolume(type, volume, AudioManager.FLAG_PLAY_SOUND);

	}

	public String reboot() {
		// Intent intent = new Intent();
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setComponent(new ComponentName("com.android.launcher",
		// "com.android.launcher2.Launcher"));
		// startActivity(intent);
		//
		// intent = new Intent();
		// intent.setAction("ACTION_SHOW_NAVIGATIONBAR");
		// sendBroadcast(intent);
		String result = "success";
		if (mVanRequest == null) {
			mVanRequest = new VanRequest("127.0.0.1", 8998);
		}
		try {
			result = mVanRequest.execute("/cmd", "reboot");
		} catch (Exception e) {
			result = "failed :" + e.getMessage();
		}
		return result;
	}

	public void shutdown() {
		try {
			// 获得ServiceManager类
			Class<?> ServiceManager = Class
					.forName("android.os.ServiceManager");
			// 获得ServiceManager的getService方法
			Method getService = ServiceManager.getMethod("getService",
					java.lang.String.class);
			// 调用getService获取RemoteService
			Object oRemoteService = getService.invoke(null,
					Context.POWER_SERVICE);
			// 获得IPowerManager.Stub类
			Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
			// 获得asInterface方法
			Method asInterface = cStub.getMethod("asInterface",
					android.os.IBinder.class);
			// 调用asInterface方法获取IPowerManager对象
			Object oIPowerManager = asInterface.invoke(null, oRemoteService);
			// 获得shutdown()方法
			Method shutdown = oIPowerManager.getClass().getMethod("shutdown",
					boolean.class, boolean.class);
			// 调用shutdown()方法
			shutdown.invoke(oIPowerManager, false, true);
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
		}

	}

	

	public boolean synchronousTime(String timeString) throws Exception {
		boolean isSuccess = false;
		Pattern pattern = Pattern.compile("^[0-9]*$");
		Matcher mathcer = pattern.matcher(timeString);
		if (timeString.length() == 14 && mathcer.matches()) {
			Intent intent = new Intent("unistrong.intent.action.SYS_CMD");
			intent.putExtra("CMD", "UPDATE_TIME");
			long time = new SimpleDateFormat("yyyyMMddHHmmss")
					.parse(timeString).getTime();
			intent.putExtra("time", time);
			mContext.sendBroadcast(intent);
			isSuccess = true;
		}
		return isSuccess;
	}
	
	public JSONObject getFileInfo(String path) throws JSONException{
		JSONObject json = new JSONObject();
		File file = new File(path);
		if (file.exists()){
			
			json.put("name", file.getName());
			json.put("size", file.length()/1024+"kb");
			json.put("MD5", Utils.getFileMD5(file));		
			long time = file.lastModified();//返回文件最后修改时间，是以个long型毫秒数
			String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(time));
			json.put("lastModifyTime",ctime);
			
		}else{
			json.put("msg", path+", 文件不存在");
		}
		return json;
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				BatteryV = intent.getIntExtra("voltage", 0); // 电池电压
				BatteryT = intent.getIntExtra("temperature", 0); // 电池温度
			}
		}
	};
	public void getDeviceMes(JSONObject json) throws Exception {
		DisplayMetrics displayMetrics = mContext.getResources()
				.getDisplayMetrics();
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getRealMetrics(displayMetrics);

		int cpuRate = DeviceInfo.getCpuRate();
		cpuRate = cpuRate < 0 ? 0 : cpuRate;
		cpuRate = cpuRate > 50 ? 50 : cpuRate;
		long totalMem = DeviceInfo.getMemTotal(mContext);
		long availMem = DeviceInfo.getMemUnused(mContext);
		long totalInMem = DeviceInfo.getTotalInternalMemorySize();
		long availInMem = DeviceInfo.getAvailableExternalMemorySize();
		long battery = (long) (BatteryT);

		if (battery >= 600.0) {
			battery = 600;
		}
		if (((int) availMem << 1) >= totalMem) {
			availMem = (int) totalMem >> 1;
		}
		String MemTotal = String.format("%.2f",
				(float) (totalMem / (1024 * 1024.0)));
		String Memavail = String.format("%.2f", availMem / (1024 * 1024.0));
		String Flashtot = String.format("%.2f", totalInMem / (1024 * 1024.0));
		String Flashavail = String.format("%.2f", availInMem / (1024 * 1024.0));
		String vid = "0x1DFC";
		String pid = "0x8810";
		/**
		String formator = "CPU=%s&MEM_TOT=%s&MEM_AVAIL=%s&FLASH_TOT=%s&FLASH_AVAIL=%s"
				+ "&VER=%s&VOLTAGE=%s&TEMPERATURE=%s&SCREEN_PIXEL=%s&VID=%s&PID=%s";
				*/
		/**
		 * String infos = String.format(formator,
		 * URLEncoder.encode(String.valueOf(cpuRate), "GBK") ,
		 * URLEncoder.encode(MemTotal, "GBK"), URLEncoder.encode(Memavail,
		 * "GBK") , URLEncoder.encode(Flashtot, "GBK"),
		 * URLEncoder.encode(Flashavail, "GBK") , URLEncoder.encode("V1.0:::V" +
		 * DeviceInfo.getLocalVersionCode(this), "GBK") ,
		 * URLEncoder.encode(String.valueOf(BatteryV / 1000) + "V", "GBK") ,
		 * URLEncoder.encode(String.valueOf(battery / 10) + "℃", "GBK") ,
		 * URLEncoder.encode(displayMetrics.widthPixels + "*" +
		 * displayMetrics.heightPixels, "GBK") , URLEncoder.encode(vid, "GBK"),
		 * URLEncoder.encode(pid, "GBK"));
		 */
		// infos = "CPU=" + DeviceInfo.getCpuRate() + "&MEM_TOT="
		// + MemTotal + "&MEM_AVAIL=" + Memavail + "&FLASH_TOT="
		// + Flashtot + "&FLASH_AVAIL=" + Flashavail + "&HWVER=" + "V1.0"
		// + ":::V" + DeviceInfo.getLocalVersionCode(MainActivity.this)
		// + "&VOLTAGE=" + (BatteryV / 1000) + "V&TEMPERATURE="
		// + (BatteryT / 10) + "℃" + "&SCREEN_PIXEL="
		// + displayMetrics.widthPixels + "*"
		// + displayMetrics.heightPixels + "&VID=0x1DFC&PID=0x8810";
		// String infos = String.format(formator,
		// URLEncoder.encode(String.valueOf(DeviceInfo.getCpuRate()), "utf-8"),)
		json.put("CPU", String.valueOf(cpuRate));
		json.put("MEM_TOT", MemTotal);
		json.put("MEM_AVAIL",Memavail);
		json.put("FLASH_TOT", Flashtot);
		json.put("FLASH_AVAIL",Flashavail);
		json.put("VER", DeviceInfo.getLocalVersionCode(mContext));
		json.put("VOLTAGE", String.valueOf(BatteryV / 1000) + "V");
		json.put("TEMPERATURE", String.valueOf(battery / 10) + "℃");
		json.put("SCREEN_PIXEL", displayMetrics.widthPixels + "*"
				+ displayMetrics.heightPixels);
		json.put("VID", vid);
		json.put("PID", pid);
		/*
		String infos = String.format(formator, URLEncoder.encode(
				String.valueOf(cpuRate), "utf-8"), URLEncoder.encode(MemTotal,
				"utf-8"), URLEncoder.encode(Memavail, "utf-8"), URLEncoder
				.encode(Flashtot, "utf-8"), URLEncoder.encode(Flashavail,
				"utf-8"), URLEncoder.encode(
				DeviceInfo.getLocalVersionCode(mContext), "utf-8"), URLEncoder
				.encode(String.valueOf(BatteryV / 1000) + "V", "utf-8"),
				URLEncoder.encode(String.valueOf(battery / 10) + "℃", "utf-8"),
				URLEncoder.encode(displayMetrics.widthPixels + "*"
						+ displayMetrics.heightPixels, "utf-8"), URLEncoder
						.encode(vid, "utf-8"), URLEncoder.encode(pid, "utf-8"));
		// infos = URLEncoder.encode(infos, "utf-8");
		 **/
	
	}

}
