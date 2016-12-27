package com.nantian.entity;

public class SignBoardInfo {
	private int width;
	private int heigth;
	private int signNum;
	private int sign_x;
	private int sing_y;
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeigth() {
		return heigth;
	}
	public void setHeigth(int heigth) {
		this.heigth = heigth;
	}
	public int getSignNum() {
		return signNum;
	}
	public void setSignNum(int signNum) {
		this.signNum = signNum;
	}
	public int getSign_x() {
		return sign_x;
	}
	public void setSign_x(int sign_x) {
		this.sign_x = sign_x;
	}
	public int getSing_y() {
		return sing_y;
	}
	public void setSing_y(int sing_y) {
		this.sing_y = sing_y;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + heigth;
		result = prime * result + signNum;
		result = prime * result + sign_x;
		result = prime * result + sing_y;
		result = prime * result + width;
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
		SignBoardInfo other = (SignBoardInfo) obj;
		if (heigth != other.heigth)
			return false;
		if (signNum != other.signNum)
			return false;
		if (sign_x != other.sign_x)
			return false;
		if (sing_y != other.sing_y)
			return false;
		if (width != other.width)
			return false;
		return true;
	}	
}
