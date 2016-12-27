package com.nantian.pluginImpl;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.van.uart.VanKeyboard;
import com.iflytek.tts.TtsService.Tts;
import com.nantian.entity.SignBoardInfo;
import com.nantian.entity.SignPDF;
import com.nantian.plugininterface.IData;
import com.nantian.plugininterface.IPluginInterface;
import com.nantian.utils.Setting;
import com.nantian.utils.StringUtil;
import com.nantian.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gongkan on 2016/12/8.
 */

public class PluginHandler implements IPluginInterface,
		VanKeyboard.OnClickListener {

	private static final String TAG = "PluginHandler";

	private IData data;

	private Context mContext;

	private VanKeyboard keyboardManager;

	private UIManager uiManamger;
	public PluginHandler() {
		keyboardManager = new VanKeyboard();
		keyboardManager.setOnClickListener(this);
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
				json.put("result", "0");
			} else if ("closeKeyboard".equals(s)) {
				closeKeybord();
				json.put("result", "0");
			} else if ("plainMainKey".equals(s)) {
				String value = pa.optString("value");
				if (value.length() % 2 != 0) {
					json.put("result", "-1");
					json.put("msg", "数据长度不对");
					return json;
				}
				byte[] data = Utils.getHexData(value);
				int index = pa.optInt("mainIndex", 0);
				if (PasswordHandler.instance().plainMainKey(data, index)) {
					json.put("result", "0");
				} else {
					json.put("result", "-1");
					json.put("msg", "明文下载主密钥失败");
				}
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
				byte[] data = StringUtil.hexStringToBytes(value);
				int decodeType = pa.optInt("decodeType", 0);
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
				if (PasswordHandler.instance().ciphWorkKey(
						StringUtil.hexStringToBytes(value), decodeType,
						mainIndex, workIndex)) {
					json.put("result", "0");
				} else {
					json.put("result", "-1");
					json.put("msg", "密文下载主密钥失败");
				}
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
					json.put("data", StringUtil.bytesToHexString(out));
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
					json.put("data", StringUtil.bytesToHexString(out));
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
				signinfo.setSing_y(pa.optInt("pointY"));
				signinfo.setWidth(pa.optInt("width"));
				signinfo.setHeigth(pa.optInt("height"));
				if (null == uiManamger){
					uiManamger = new UIManager();
					uiManamger.init(mContext);
				}
				uiManamger.showSignDialog(signinfo);
			}else if ("dismissSign".equals(s)) {

				if (null != uiManamger){
					uiManamger.dimissDialog();
				}
				
			}else if ("signInPoint".equals(s)) {
				if (null != uiManamger){
					SignPDF signPDF = new SignPDF();
					signPDF.setHeight(pa.optDouble("height"));
					signPDF.setWidth(pa.optDouble("width"));
					signPDF.setPointX(pa.optDouble("pointX"));
					signPDF.setPointY(pa.optDouble("pointY"));
					signPDF.setPageNum(pa.optInt("pageNum"));
					String pdfName  = pa.optString("signFilePath");
					uiManamger.setSignPdf(signPDF);
					uiManamger.saveBitmapAndSign(Environment.getExternalStorageDirectory()+"/Nantian/Temp"+File.separator+pdfName);
					
				}

			}else if ("signDigital".equals(s)) {
				
			}else if ("clearSign".equals(s)) {
				if (null != uiManamger){
					uiManamger.clearSignBoard();
				}
			}else if ("setSignDialogStyle".equals(s)) {
				Log.e(TAG, "setSignDialogStyle");
					int max = pa.optInt("pen_max");
					if (null == uiManamger){
						uiManamger = new UIManager();
						uiManamger.init(mContext);
					};
					if ( max > 3){
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
					
					
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	private void openKeybord(int mode) {
		keyboardManager.requestOpenKeyboard();
		keyboardManager.openKeyboard();
	}

	private void closeKeybord() {
		keyboardManager.closeKeyboard();
	}

	@Override
	public String getVesion() {
		return null;
	}

	@Override
	public void setData(IData iData) {
		this.data = iData;
	}

	@Override
	public void release() {
		if (null != uiManamger){
			Log.e(TAG, "dismis ...");
			uiManamger.dimissDialog();
			uiManamger = null;
		}
		keyboardManager.closeKeyboard();
	}

	@Override
	public void release(String s) {

	}

	@Override
	public void setContext(Context context) {
		this.mContext = context;
		Setting.instance().setContext(context);

	}

	@Override
	public void onKey(int value) {
		Log.e("", "value:" + value);
		if (data != null) {
			Log.e("", "idata is not null");
			JSONObject json = new JSONObject();
			try {
				json.put("cmd", "keyboard");
				json.put("key", String.valueOf(value));
				data.notifyData(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

}
