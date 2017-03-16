package com.nantian.ad;

import java.util.Comparator;

import net.sourceforge.pinyin4j.PinyinHelper;

public class SortFileByName implements Comparator<DetailFile> {

	public int compare(DetailFile o1, DetailFile o2) {

		int length1 = o1.getName().length();
		int length2 = o2.getName().length();
		int length = length1 - length2 > 0 ? length2 : length1;
		for (int i = 0; i < length; i++) {
			char codePoint1 = o1.getName().toLowerCase().charAt(i);
			char codePoint2 = o2.getName().toLowerCase().charAt(i);
			if (Character.isSupplementaryCodePoint(codePoint1)
					|| Character.isSupplementaryCodePoint(codePoint2)) {
				continue;
			}
			if (codePoint1 != codePoint2) {
				if (Character.isSupplementaryCodePoint(codePoint1)
						|| Character.isSupplementaryCodePoint(codePoint2)) {
					return codePoint1 - codePoint2;
				}
				String pinyin1 = pinyin((char) codePoint1);
				String pinyin2 = pinyin((char) codePoint2);
				if (pinyin1 != null && pinyin2 != null) { // �����ַ����Ǻ���
					if (!pinyin1.equals(pinyin2)) {
						return pinyin1.compareTo(pinyin2);
					}
				} else {
					return codePoint1 - codePoint2;
				}
			}
		}
		return length1 - length2;
	}

	/**
	 * �ַ���ƴ���������־͵õ���һ��ƴ�������Ǻ��֣���return null��
	 */
	private String pinyin(char c) {
		String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
		if (pinyins == null) {
			return null;
		}
		return pinyins[0];
	}
}