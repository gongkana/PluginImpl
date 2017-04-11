package com.nantian.pluginImpl;

import android.text.TextUtils;

public class DataException extends Exception{
	
	/**
	 * Serializable
	 */
	private static final long serialVersionUID = 475253081756354472L;
	
	private String extendMsg = "";
	private int exceptionCode;
	
	public int getExceptionCode() {
		return exceptionCode;
	}

	public DataException(int exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
	
	public DataException(int exceptionCode,String s) {
		super(s);
		this.exceptionCode = exceptionCode;
		this.extendMsg = s;
	}
	
	public DataException( int exceptionCode , Throwable t ) {
		super( t );
		this.exceptionCode = exceptionCode;
	}

	public DataException( int exceptionCode , String s , Throwable t ) {
		super( s, t );
		this.exceptionCode = exceptionCode;
		this.extendMsg = s;
	}
	
	public String getErrMsg(){
		String ms = "";
		switch (exceptionCode) {
		case -1:
			ms =  "操作超时";
		case -2:		
			ms = "操作取消";
		case -3:
			ms = "操作被取消";
		case -4:
			ms = "参数错误";
		case -101:
			ms = "工作密钥不存在";
		case -102:
			ms = "主密钥不存在";
		case -103:
			ms = "密钥支持算法不匹配";
		case -104:
			ms = "密码计算用户号错误";
		case -201:
			ms = "PDF文件不存在";
		case -202:
			ms = "PDF文件内容错误";
		case -203:
			ms = "签名区域坐标错误";
		case -204:
			ms = "证书不存在";
		case -205:
			ms = "证书密码不正确";
		case -5:
		default:
			ms ="其他错误";
		}
		if (!TextUtils.isEmpty(extendMsg)){
			ms = ms+extendMsg;
		}
		return ms;
	}
	
		
}
