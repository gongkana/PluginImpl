package com.nantian.pluginImpl;

import java.io.File;
import java.io.ObjectOutputStream.PutField;
import java.security.PublicKey;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.iflytek.tts.TtsService.Tts;
import com.nantian.ad.PlayerFragment;
import com.nantian.entity.SignBoardInfo;
import com.nantian.entity.SignPDF;
import com.nantian.plugininterface.IData;
import com.nantian.plugininterface.IPlayer;
import com.nantian.plugininterface.IPluginInterface;
import com.nantian.sign.PDFCenter;
import com.nantian.utils.HLog;
import com.nantian.utils.Setting;
import com.nantian.utils.StringUtil;
import com.nantian.utils.Utils;
import com.van.hid.CertificateCenter;
import com.van.hid.VanKeyboard;
import com.van.hid.VanKeyboard.OnClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gongkan on 2016/12/8.
 */

public class PluginHandler implements IPluginInterface {

	private static final String TAG = "PluginHandler";

	private IData data;

	private Context mContext;

	private UIManager uiManamger = UIManager.instance();

	public PluginHandler() {

	}

	@Override
	public JSONObject execute(String s, JSONObject pa) {
		JSONObject json = new JSONObject();

		
		try {
			json.put("cmd", s);
			json.put("result", "0");
			Log.e("", "zai -- 新插件啦 33 ,execute:" + s);
			Log.e("", "I am in plugin 1 ,execute:" + s);
			if ("openKeyboard".equalsIgnoreCase(s)) {
				Log.e("", "open Keyboard begin !");
				int type = pa.optInt("type", 0);
				openKeybord(type);
			} else if ("closeKeyboard".equals(s)) {
				closeKeybord();
			} else if ("plainMainKey".equals(s)) {
				String value = pa.optString("value");
				int encodeType = pa.optInt("encodeType ", 1);
				PasswordHandler.instance().setEncodeType(encodeType);
				if (value.length() % 2 != 0) {
					json.put("result", "-1");
					json.put("msg", "数据长度不对");
					return json;
				}
				byte[] data = Utils.getHexData(value);
				int index = pa.optInt("mainIndex", 0);
				PasswordHandler.instance().plainMainKey(data, index);
			} else if ("resetMainKey".equals(s)) {
				PasswordHandler.instance().resetMainkey();
				json.put("result", 0);
			} else if ("cipherMainKey".equals(s)) {
				String value = pa.optString("value");
				if (value.length() % 2 != 0) {
					json.put("result", "-1");
					json.put("msg", "数据长度不对");
					return json;
				}
				int encodeType = pa.optInt("encodeType ", 1);
				PasswordHandler.instance().setEncodeType(encodeType);
				byte[] data = StringUtil.hexStringToBytes(value);
				int decodeType = Setting.instance().getPasswordKeyboardMode();
				if (decodeType == -1) {
					json.put("result", "-1");
					json.put("msg", "加密方式不对");
				}
				int index = pa.optInt("mainIndex", 0);
				if (PasswordHandler.instance().ciphMainKey(data, decodeType,
						index)) {
					json.put("result", "0");
				} else {
					json.put("result", "-1");
					json.put("msg", "密文下载主密钥失败");
				}

			} else if ("cipherWorkKey".equals(s)) {
				String value = pa.optString("value");
				int decodeType = pa.optInt("decodeType", 0);
				int mainIndex = pa.optInt("mainIndex", 0);
				int workIndex = pa.optInt("workIndex", 0);
				PasswordHandler.instance().ciphWorkKey(
						StringUtil.hexStringToBytes(value), decodeType,
						mainIndex, workIndex);

			} else if ("plainWorkKey".equals(s)) {
				byte[] data = StringUtil
						.hexStringToBytes(pa.optString("value"));
				int workIndex = pa.optInt("workIndex", 0);
				if (PasswordHandler.instance().plainWorkKey(data, workIndex)) {
					json.put("result", "0");
				} else {
					json.put("result", "1");
					json.put("msg", "密文下载主密钥失败");
				}
			} else if ("encryptNum".equals(s)) {
				String password = pa.optString("password");
				String account = pa.optString("account");
				int encodeType = pa.optInt("encodeType", 0);
				int workIndex = pa.optInt("workIndex", 0);
				byte[] out = null;
				try {
					out = PasswordHandler.instance().encryptNum(account,
							password, encodeType, workIndex);
				} catch (Exception e) {
					Log.e("", e.getMessage(), e);
				}
				if (null != out) {
					json.put("result", "0");
					json.put("msg", StringUtil.bytesToHexString(out));
				} else {
					json.put("result", "-1");
					json.put("msg", "加密失败");
				}
			} else if ("encryptString".equals(s)) {
				String value = pa.optString("value");
				int mode = pa.optInt("mode", 1);
				int encodeType = pa.optInt("encodeType", 0);
				int index = pa.optInt("index", 0);
				byte[] out = PasswordHandler.instance().encryptString(value,
						mode, encodeType, index);
				if (null != out) {
					json.put("result", "0");
					json.put("msg", StringUtil.bytesToHexString(out));
				} else {
					json.put("result", "-1");
					json.put("msg", "加密失败");
				}
			} else if ("ttsPlay".equals(s)) {
				Log.e("", "ttsplay:" + pa.optString("content"));
				Tts.create();
				Tts.speak(pa.optString("content"));
			} else if ("showSign".equals(s)) {
				SignBoardInfo signinfo = new SignBoardInfo();
				signinfo.setSign_x(pa.optInt("pointX"));
				int statusBarHeight1 = 0;
				// 获取status_bar_height资源的ID
				int resourceId = mContext.getResources().getIdentifier(
						"status_bar_height", "dimen", "android");
				if (resourceId > 0) {
					// 根据资源ID获取响应的尺寸值
					statusBarHeight1 = mContext.getResources()
							.getDimensionPixelSize(resourceId);
				}
				signinfo.setSing_y(pa.optInt("pointY") - statusBarHeight1);
				signinfo.setWidth(pa.optInt("width"));
				signinfo.setHeigth(pa.optInt("height"));
				uiManamger.showSignDialog(signinfo);
			} else if ("dismissSign".equals(s)) {

				if (null != uiManamger) {
					uiManamger.dimissDialog();
				}

			} else if ("signInPoint".equals(s)) {
				if (null != uiManamger) {
					SignPDF signPDF = new SignPDF();
					signPDF.setHeight(pa.optDouble("height"));
					signPDF.setWidth(pa.optDouble("width"));
					signPDF.setPointX(pa.optDouble("pointX"));
					signPDF.setPointY(pa.optDouble("pointY"));
					// signPDF.setHeight(100);
					// signPDF.setWidth(500);
					// signPDF.setPointX(0);
					// signPDF.setPointY(400);
					signPDF.setPageNum(pa.optInt("pageNum"));
					String pdfName = pa.optString("signFilePath");
					if (TextUtils.isEmpty(pdfName)) {
						String signString = pa.optString("signFileString");
						if (!TextUtils.isEmpty(signString)) {
							pdfName = "Temp/sign/sign.pdf";
							if (!PDFCenter.createPDF(
									pa.optString("signFileString"),
									Environment.getExternalStorageDirectory()
											+ "/Nantian" + File.separator
											+ pdfName,uiManamger.getSignTrack())) {
								pdfName = "";
							}
						}
					}
					String keypath = pa.optString("keyStore", "");
					if (TextUtils.isEmpty(keypath)) {
						keypath = Environment.getExternalStorageDirectory()
								+ "/Nantian/Temp" + File.separator + "demo.p12";
						signPDF.setPassword("123456");
					} else {
						keypath = Environment.getExternalStorageDirectory()
								+ "/Nantian" + File.separator + keypath;
						signPDF.setPassword(pa.optString("keyPassword"));
					}
					signPDF.setKeySorePath(keypath);
					signPDF.setPdfPath(Environment
							.getExternalStorageDirectory()
							+ "/Nantian"
							+ File.separator + pdfName);
					String path;
					try {
						path = uiManamger.saveBitmapAndSign(signPDF);
						if (null != path) {
							json.put("msg", Utils.GetImageStr(path));
						}
					} catch (DataException e) {
						json.put("resust", e.getExceptionCode());
						json.put("msg", e.getMessage());
					}

				}
			} else if ("digitalSeal".equals(s)) {
				SignPDF signPDF = new SignPDF();
				signPDF.setHeight(pa.optDouble("height"));
				signPDF.setWidth(pa.optDouble("width"));
				signPDF.setPointX(pa.optDouble("pointX"));
				signPDF.setPointY(pa.optDouble("pointY"));
				// signPDF.setHeight(100);
				// signPDF.setWidth(500);
				// signPDF.setPointX(0);
				// signPDF.setPointY(400);
				signPDF.setPageNum(pa.optInt("pageNum"));
				String pdfName = pa.optString("signFilePath");
				String markImagePath = Environment.getExternalStorageDirectory()+"/Nantian/"+pa.optString("sealPath");
				String keypath = pa.optString("keyStore", "");
				if (TextUtils.isEmpty(keypath)) {
					keypath = Environment.getExternalStorageDirectory()
							+ "/Nantian/Temp" + File.separator + "demo.p12";
					signPDF.setPassword("123456");
				} else {
					keypath = Environment.getExternalStorageDirectory()
							+ "/Nantian" + File.separator + keypath;
					signPDF.setPassword(pa.optString("keyPassword"));
				}
				signPDF.setKeySorePath(keypath);
				signPDF.setPdfPath(Environment
						.getExternalStorageDirectory()
						+ "/Nantian"
						+ File.separator + pdfName);
					
				PDFCenter.sign(markImagePath, signPDF, null);


			} else if ("clearSign".equals(s)) {
				if (null != uiManamger) {
					uiManamger.clearSignBoard();
				}
			} else if ("setSignDialogStyle".equals(s)) {
				Log.e(TAG, "setSignDialogStyle");
				int max = pa.optInt("pen_max");
				;
				if (max > 3) {
					uiManamger.setDPenSize(max);
				}

				try {
					String penColor = pa.optString("pen_color");
					String bgColor = pa.optString("bg_color");
					uiManamger.setDialogBgColor(bgColor);
					uiManamger.setPenColor(penColor);
				} catch (Exception e) {
					json.put("result", "-1");
					json.put("msg", "颜色值不对");
				}

			} else if ("getVersion".equals(s)) {
				json.put("msg", SystemHandler.instance().getVersion());
			} else if ("setVolume".equals(s)) {
				int volumeType = pa.optInt("volumeType");
				int volumeValue = pa.optInt("volumeValue");
				SystemHandler.instance().settingVolume(volumeType, volumeValue);
			} else if ("reboot".equals(s)) {
				json.put("msg", SystemHandler.instance().reboot());
			} else if ("getDeviceInfo".equals(s)) {
				JSONObject jsondata = new JSONObject();
				SystemHandler.instance().getDeviceMes(jsondata);
				json.put("msg",jsondata);
			} else if ("getPublicKey".equals(s)) {
				int type = pa.optInt("encodeType", 1);
				int length = pa.optInt("length");
				boolean isCreate = pa.optBoolean("isCreate", true);
				JSONObject jsData = new JSONObject();
				PasswordHandler.instance().getPublicKey(type,length,isCreate,jsData);
				json.put("msg",jsData);

			}else if ("getMainKey".equals(s)){
				int index = pa.optInt("index",0);
				byte[] key = PasswordHandler.instance().getMainKey(index);
				json.put("msg", StringUtil.bytesToHexString(key));
			} else if ("resetKey".equals(s)) {// 恢复出厂密钥
				int index = pa.optInt("index", -1);
				PasswordHandler.instance().resetKey(index);
			} else if ("initialKey".equals(s)) {// 初始化主密钥
				byte[] initialKey = StringUtil.hexStringToBytes(pa
						.optString("publicKey"));
				int mainKeyNum = pa.optInt("mainIndex", 0);
				int decodeType = pa.optInt("decodeType", Setting.instance()
						.getPasswordKeyboardMode());
				PasswordHandler.instance().initialKey(initialKey,
									mainKeyNum, decodeType);

			} else if ("getKeyboardKeyVerify".equals(s)) {// 获取密码键盘校验值
				int keyType = pa.optInt("keyType", 0);
				int keyIndex = pa.optInt("keyIndex", 0);
				int encodeType = pa.optInt("encodeType", Setting.instance()
						.getPasswordKeyboardMode());

					json.put("msg", StringUtil
							.bytesToHexString(PasswordHandler.instance()
									.getKeyboardKeyVerify(keyType, keyIndex,
											encodeType)));

			} else if ("setPasswordLen".equals(s)) {
				int length = pa.optInt("length", 6);
				PasswordHandler.instance().setPasswordLen(length);
			} else if ("setEncodeType".equals(s)) {
				int EncodeType = pa.optInt("encodeType", 1);
				PasswordHandler.instance().setEncodeType(EncodeType);
			} else if ("passwordEncode".equals(s)) {

				int timeout = pa.optInt("timeout", 20);
				int minLength = pa.optInt("minLength", 6);
				int maxLength = pa.optInt("maxLength", 6);
				boolean isAntoEnd = pa.optBoolean("isAntoEnd", false);
				boolean isClean = pa.optBoolean("isClean", false);
				int encodeType = pa.optInt("encodeType", Setting.instance()
						.getPasswordKeyboardMode());
				String account = pa.optString("account", "");
				int workIndex = pa.optInt("index", 0);
				try {
					openKeybord(1);
					InputThread thread = new InputThread(timeout, minLength,
							maxLength, data, isAntoEnd, isClean);
					thread.start();
					thread.join();
					switch (thread.result()) {
					case 0:
						byte[] enPwd;

							enPwd = PasswordHandler.instance()
									.encryptNum(account, thread.getPassword(),
											encodeType, workIndex);
							json.put("msg", StringUtil.bytesToHexString(enPwd));

						break;
					case -1:
						json.put("result", "-3");
						json.put("msg", "操作被取消");
						break;
					case -2:
						closeKeybord();
						json.put("result", "-1");
						json.put("msg", "操作超时");
						break;
					default:
						break;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				closeKeybord();
			} else if ("getFileInfo".equals(s)) {
				String path = Environment.getExternalStorageDirectory()
						+ "/Nantian/" + pa.optString("fileName");
				json.put("msg", SystemHandler.instance().getFileInfo(path));
			} else if ("clearSignFile".equals(s)) {
				File dir = new File(Environment.getExternalStorageDirectory()
						+ "/Nantian/sign");
				if (dir.exists()) {
					Utils.delete(dir);
				}
			} else if ("playAD".equals(s)) {
				uiManamger.playAD();
			} else if ("pauseAD".equals(s)) {
				uiManamger.pauseAD();
			} else if ("getPluginName".equals(s)) {
				json.put(
						"data",
						Setting.instance().getData(Setting.KEY_PLUGIN_NAME,
								Constans.PLUGIN_NAME));
			} else if ("setPluginName".equals(s)) {
				String data = pa.optString("pluginName", Constans.PLUGIN_NAME);
				Setting.instance().saveData(Setting.KEY_PLUGIN_NAME, data);
			} else if ("setAdTime".equals(s)) {
				int time = pa.optInt("AdTime", 5);
				uiManamger.setAdTime(time);
				Setting.instance().saveIntData(Setting.KEY_GG_TIME, time);
			} else if ("setAdType".equals(s)) {
				int type = pa.optInt("AdType", 0);
				uiManamger.setAdType(type);
				Setting.instance().saveIntData(Setting.KEY_PLAY_TYPE, type);
			}else if ("hidNavigAtionBar".equals(s)) {
			        Intent intent = new Intent();
			        intent.setAction("ACTION_HIDE_NAVIGATIONBAR");
			        mContext.sendBroadcast(intent);				
			}else if ("showNavigAtionBar".equals(s)) {
				        Intent intent = new Intent();
				        intent.setAction("ACTION_SHOW_NAVIGATIONBAR");
				        mContext.sendBroadcast(intent);
			}else if ("encodeByPublicKey".equals(s)){
			    String data = pa.optString("encodeData");
			    byte[] out =PasswordHandler.instance().encodeByPublicKey(StringUtil.hexStringToBytes(data));
				json.put("msg", StringUtil.bytesToHexString(out));
				
			}else if ("decodeByPrivateKey".equals(s)){
				String data = pa.optString("decodeData");
				byte[] out =PasswordHandler.instance().decodeByPrivateKey(StringUtil.hexStringToBytes(data));
				json.put("msg", StringUtil.bytesToHexString(out));
			}else if ("encodeByPublicKeyAndMode".equals(s)){
				    String data = pa.optString("encodeData");
				    String big1 = pa.optString("publicKey");
				    String  big2= pa.optString("exponent");
				    byte[] out =PasswordHandler.instance().encodeByPublicKeyAndMode(big1, big2,StringUtil.hexStringToBytes(data));
					json.put("msg", StringUtil.bytesToHexString(out));
					
			}else if ("pdfToImage".equals(s)){
				String name = pa.optString("pdfName");
				String pdfdir = Environment.getExternalStorageDirectory()+Constans.PDF;
				JSONArray jsonAry = new JSONArray(PDFCenter.pdfToBitmap(pdfdir,name, pdfdir+"/image"));
				json.put("msg", jsonAry);
			}else if ("isKeyboardAva".equals(s)){
				try {
					json.put("msg", VanKeyboard.instance().isUartOpen());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					json.put("msg", "false");
				}
			}else if ("showLogView".equals(s)){
				uiManamger.showLogView();
			}else if ("dimissLogView".equals(s)){
				if (uiManamger != null) {
					uiManamger.dismissView();
				}
			} else {
				json.put("result", "-1");
				json.put("msg", "没有指令：" + s);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (DataException e) {
			
			try {
				json.put("result", ""+e.getExceptionCode());
				json.put("msg", e.getErrMsg());
				HLog.e(TAG, e);
				HLog.e(TAG, e.getErrMsg());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
			
		return json;
	}

	private void closeKeybord() {
		VanKeyboard.instance().closeKeyboard();

	}

	private void openKeybord(int type) {
		switch (type) {
		case 0:// 明文打开
			VanKeyboard.instance().setOnClickListener(new OnClickListener() {

				@Override
				public void onKey(int key) {
					JSONObject json = new JSONObject();
					try {
						json.put("cmd", "keyboard");
						json.put("key", "" + key);
						data.notifyData(json);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
			break;
		case 1:// 密文打开
			break;
		default:
			break;
		}
		if (VanKeyboard.instance().requestOpenKeyboard()) {
			VanKeyboard.instance().openKeyboard();
		}

	}

	@Override
	public String getVesion() {
		return "1.0.1";
	}

	@Override
	public void setData(IData iData) {
		this.data = iData;
	}

	@Override
	public void release() {

			uiManamger.release(null);
			uiManamger.dimissDialog();

		VanKeyboard.instance().release();
		SystemHandler.instance().destory();
		
	}

	@Override
	public void release(String s) {
	}

	@Override
	public void setContext(Context context) {
		HLog.e(TAG, "setContext");
		if (context instanceof Service){
			HLog.e(TAG, "service");
		}else if(context instanceof Activity){
			HLog.e(TAG, "activity");
		}
		this.mContext = context;
		uiManamger.init(context);
		VanKeyboard.instance();
		Setting.instance().setContext(context);
		SystemHandler.instance().init(context);
	}

	@Override
	public IPlayer getPlayer() {

		return uiManamger;
	}

	@Override
	public int printLog(int levl, String tag, String msg, Throwable tr) {
		return HLog.printLog(levl, tag, msg, tr);
	}

}
