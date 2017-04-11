package com.nantian.pluginImpl;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.nantian.plugininterface.IData;
import com.nantian.utils.HLog;
import com.van.hid.VanKeyboard;
import com.van.hid.VanKeyboard.OnClickListener;

public class InputThread extends Thread implements Runnable,OnClickListener{

	private int timeout;//���볬ʱʱ��

	private int minLength;//������С����
	
	private int maxLength;//������󳤶�
	
	private boolean isAutoEnd;//�����Ƿ�����Զ����룬true-�Զ��������룬false-��ȷ�ϼ��Ž�������
	
	private boolean isClean;//true - ��������������룬false-������ɾ��һ���ַ�
	
	private IData idata;//���͸�UI�Ľӿڲ���
	
	private String pwd = "";
	
	private JSONObject json;
	
	
	private int result = -2;
	public InputThread(int timeout,int minLength,int maxLength,IData idata,boolean isAntoEnd,boolean isClean){
		HLog.e("thread...", "timeout ="+timeout+",minLength="+minLength+",maxLength:"+maxLength+",isAutoEnd="+isAntoEnd+",isClean="+isClean);
		this.timeout = timeout;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.idata = idata;
		this.isAutoEnd = isAntoEnd;
		this.isClean = isClean;
		VanKeyboard.instance().setOnClickListener(this);
		json = new JSONObject();
		try {
			json.put("cmd", "keyboard");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getPassword (){
		HLog.e("", "pwd = "+pwd);
		return pwd;
	}
	
	public int result(){
		return result;
	}
	
	private JSONObject putJsonValue(String value){
		try {
			return json.put("key", value);
		} catch (JSONException e) {
			return json;
		}
	}
	
	
	@Override
	public void run() {
		try {
			while (--timeout > 0) {
				
				HLog.e("", "time:"+timeout);
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void destroy(){
		timeout = 0;
	}
	
	/**
	 * ������ְ���
	 * 
	 * @param numberValue
	 *            ����������ּ���ֵ
	 */
	public void clickNumberKey(char numberValue){
		HLog.e("", "key = "+numberValue);
		if (pwd.length()<maxLength){
			pwd+=numberValue;
			idata.notifyData(putJsonValue(pwd));
		}
		if(isAutoEnd && pwd.length() == maxLength){
			timeout = 0;
			result = 0;
			VanKeyboard.instance().closeKeyboard();
		}
	}

	/**
	 * ���ȷ����ȡ������ҳ�ȷ����ְ���
	 * 
	 * @param unnumberKey
	 *            ������ķ����ּ�
	 */
	public void clickUnnumberKey(int unnumberKey){
		HLog.e("", "Unnumbe key = "+unnumberKey);
		switch (unnumberKey) {
		
		case VanKeyboard.KEY_PWD:
			if (pwd.length()<maxLength){
				pwd+=unnumberKey;
			}

		//LogUtil.i(TAG, "pwd::" + mPwdBuilder.toString());
		break;
		case VanKeyboard.KEY_OK:// ȷ����
			HLog.e("", "pwd = "+pwd);
		if (pwd.length() < minLength ){
			if (VanKeyboard.instance().requestOpenKeyboard()){
				VanKeyboard.instance().openKeyboard();
			}
			return;
		}
		timeout = 0;
		result = 0;
		break;
	case VanKeyboard.KEY_CANCEL:// ȡ����
		pwd = "-1";
		result = -1;
		timeout = 0;
		break;
	case VanKeyboard.KEY_MOTIFY:// ������
		if (isClean){
			pwd = "";
		}else{
			pwd = pwd.substring(0, pwd.length()-1);
		}
		idata.notifyData(putJsonValue(pwd));
		break;
	default:
		break;
	}
	}

	@Override
	public void onKey(int key) {
		if (key >= 0x30 && key <= 0x39) {
			clickNumberKey((char)key);
		} else {
			clickUnnumberKey(key);
		}
		
	}
}
