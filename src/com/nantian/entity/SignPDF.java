package com.nantian.entity;

public class SignPDF {
	
	private double width;
	
	private double height;
	
	private int pageNum;
	
	private double pointX;
	
	private double pointY;

	private String keySorePath;

	private String pdfPath;
	
	private String password;
	
	private String reason;
	
	public String getKeySorePath() {
		return keySorePath;
	}

	public void setKeySorePath(String keySorePath) {
		this.keySorePath = keySorePath;
	}

	public String getPdfPath() {
		return pdfPath;
	}

	public void setPdfPath(String pdfPath) {
		this.pdfPath = pdfPath;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public double getPointX() {
		return pointX;
	}

	public void setPointX(double pointX) {
		this.pointX = pointX;
	}

	public double getPointY() {
		return pointY;
	}

	public void setPointY(double pointY) {
		this.pointY = pointY;
	}

	@Override
	public String toString() {
		return "SignPDF [width=" + width + ", height=" + height + ", pageNum="
				+ pageNum + ", pointX=" + pointX + ", pointY=" + pointY
				+ ", keySorePath=" + keySorePath + ", pdfPath=" + pdfPath
				+ ", password=" + password + ", reason=" + reason + "]";
	}

	


}
