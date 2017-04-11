package com.nantian.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.nantian.pluginImpl.DataException;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;

public class DeviceInfo {
	private static DeviceInfo _instance;

	private static long totalTime1;
	private static long totalTime2;
	private static long idleTime1;
	private static long idleTime2;

	private DeviceInfo() {
		totalTime1 = 0;
		totalTime2 = 0;
		idleTime1 = 0;
		idleTime2 = 0;
	}

	public static DeviceInfo instance() {
		if (null == _instance) {
			_instance = new DeviceInfo();
		}

		return _instance;
	}

	public static void calcCpuRate() {
		String[] cpuInfos = null;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream("/proc/stat")), 1000);
			String load = reader.readLine();
			reader.close();
			cpuInfos = load.split(" ");
		} catch (IOException e) {
			return;
		}

		long totalCpu = 0;
		long idleCpu = 0;

		try {
			idleCpu = Long.parseLong(cpuInfos[5]);
			totalCpu = Long.parseLong(cpuInfos[2])
					+ Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
					+ idleCpu + Long.parseLong(cpuInfos[6])
					+ Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return;
		}

		if (0 == totalTime1) {
			totalTime1 = totalCpu;
			idleTime1 = idleCpu;
		} else {
			totalTime1 = totalTime2;
			idleTime1 = idleTime2;

			totalTime2 = totalCpu;
			idleTime2 = idleCpu;
		}
	}

	public static int getCpuRate() {
		calcCpuRate();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
		calcCpuRate();
		float total = totalTime2 - totalTime1;
		float idle = idleTime2 - idleTime1;
		return (int) (100 * (total - idle) / total);
	}

	/**
	 * 可用内存
	 * 
	 * @return
	 */
	public static long getMemUnused(Context context) {
		long MEM_UNUSED;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		MEM_UNUSED = mi.availMem;
		return MEM_UNUSED;
	}

	// 获得总内存
	@SuppressLint("NewApi")
	public static long getMemTotal(Context context) {
		long MEM_UNUSED;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		MEM_UNUSED = mi.totalMem;
		return MEM_UNUSED;

	}

	public static boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取手机内部剩余存储空间
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获取手机内部总的存储空间
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获取SDCARD剩余存储空间
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return 0;
		}
	}

	/**
	 * 获取SDCARD总的存储空间
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else {
			return 0;
		}
	}

	/**
	 * 固件版本号
	 * 
	 * @return
	 * @throws DataException 
	 */
	public static String getLocalVersionCode(Context context) throws DataException
			{
		String version = null;
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
			version = info.versionName;// 获取版本号
		} catch (NameNotFoundException e) {
			throw new DataException(-5);
		}
		
		return version;
	}

	public static String dd() {
		return android.os.Build.VERSION.RELEASE;
	}
}
