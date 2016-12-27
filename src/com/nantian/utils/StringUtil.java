package com.nantian.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Xml;

import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

@SuppressLint("DefaultLocale")
public class StringUtil {

	// private static final String TAG = StringUtil.class.getSimpleName();

	public static boolean isNotEmpty(String stringValue) {
		if (stringValue != null && stringValue.length() > 0) {
			return true;
		}
		return false;
	}

	public static String addZeroBefortText(String originalString,
			final int expectStringToLen) {
		StringBuffer stringBuffer = new StringBuffer(originalString.trim());
		while (stringBuffer.length() < expectStringToLen) {
			stringBuffer.insert(0, "0");
		}
		return stringBuffer.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")
				|| 0 != (hexString.length() % 2)) {
			return null;
		}

		int length = hexString.length() / 2;
		byte[] dst = new byte[length];

		for (int i = 0; i < length; i++) {
			String value = hexString.substring(i * 2, i * 2 + 2);
			dst[i] = (byte) Integer.parseInt(value, 16);
		}

		return dst;
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder sb = new StringBuilder(src.length * 2);

		final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		for (int i = 0; i < src.length; i++) {
			int value = src[i] & 0xff;
			sb.append(HEX[value / 16]).append(HEX[value % 16]);
		}

		return sb.toString();
	}

	public String printHexString(byte[] b) {
		String a = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}

			a = a + hex;
		}

		return a;
	}

	// make image HTML for PDF show
	public static String getPNGHTML(ArrayList<String> listIMGPath) {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><body style='margin:0;padding:0'>");

		for (String imgPath : listIMGPath) {
			sb.append("<img style='width:100%' src='" + imgPath + "'/>");
		}

		sb.append("</body></html>");

		return sb.toString();
	}

	public static String parseStringToXML(String xmlText) throws Exception {
		if (TextUtils.isEmpty(xmlText)) {
			return "";
		}

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		serializer.setOutput(writer);

		serializer.startDocument("utf-8", true);
		serializer.startTag(null, "root");

		String[] tagTexts = xmlText.split("&&");
		// LogUtil.i(TAG, "tagTexts.length-->" + tagTexts.length);

		String prefix = "";
		String prePrefix = prefix;
		String[] tempInfos;
		String parentTag = "";
		String[] tagInfos;
		String[] childInfos;
		for (int i = 0; i < tagTexts.length; i++) {
			String tagText = tagTexts[i];
			// LogUtil.i(TAG, tagText);
			tempInfos = tagText.split(":");
			prefix = tempInfos[0];

			if ("input".equalsIgnoreCase(prefix)) {
				tagInfos = tempInfos[1].split("/");

				if (!prefix.equals(prePrefix)) {
					parentTag = tagInfos[0];
					serializer.startTag(null, parentTag);
				}

				childInfos = tagInfos[1].split(",");
				serializer.startTag(null, childInfos[0]);
				serializer.text(childInfos[1]);
				serializer.endTag(null, childInfos[0]);

				if ((i + 1) < tagTexts.length) {
					if (!prefix.equals(tagTexts[i + 1].split(":")[0])) {
						serializer.endTag(null, parentTag);
					}
				} else {
					serializer.endTag(null, parentTag);
				}

			}

			if ("select".equalsIgnoreCase(prefix)) {
				childInfos = tempInfos[1].split(",");
				serializer.startTag(null, childInfos[0]);
				serializer.text(childInfos[1]);
				serializer.endTag(null, childInfos[0]);
			}

			if ("tr".equalsIgnoreCase(prefix)) {
				tagInfos = tempInfos[1].split("/");

				parentTag = tagInfos[0];

				serializer.startTag(null, parentTag);

				childInfos = tagInfos[1].split("\\|");

				for (String childInfo : childInfos) {
					// LogUtil.i(TAG, childInfo);
					childInfos = childInfo.split(",");
					serializer.startTag(null, childInfos[0]);
					serializer.text(childInfos[1]);
					serializer.endTag(null, childInfos[0]);
				}

				serializer.endTag(null, parentTag);
			}

			prePrefix = prefix;
		}

		serializer.endTag(null, "root");
		serializer.endDocument();
		return writer.toString();
	}

}
