package com.nantian.sign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import android.R.raw;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

public class PDFCenter {

	public static boolean createPDF(String rawHTML, String fileName) {

		File file = new File(fileName);
				
		try {
			if (file.exists()) {
				file.delete();
			}
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(file));
			document.open();
			// HTML
			// String htmlText = Jsoup.clean(rawHTML, Whitelist.relaxed());
			InputStream inputStream = new ByteArrayInputStream(
					rawHTML.getBytes());

			// PDF
			XMLWorkerHelper.getInstance().parseXHtml(
					writer,
					document,
					inputStream,
					null,
					Charset.defaultCharset(),
					new MyFont());

			document.close();
			return true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (DocumentException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 给pdf添加水印签名
	 * 
	 * @param srcPath
	 *            pdf路径
	 * @param pageNum
	 *            签名在第几页
	 * @param pdfX
	 *            pdf签名的x坐标(单位mm)
	 * @param pdfY
	 *            pdf签名的y坐标(单位mm)
	 * @param markImagePath
	 *            水印图片路径
	 * @throws Exception
	 */
	public static void signInPDF(String srcPath, int pageNum, float pdfX,
			float pdfY, String markImagePath,float areaX,float areaY) throws Exception {
		// 选择需要印章的pdf
		PdfReader reader = new PdfReader(srcPath);

		Rectangle pageSize = reader.getPageSize(pageNum);
		float width = pageSize.getWidth();
		float height = pageSize.getHeight();
		String tempPath = srcPath.replace(".", "_new.");
		PdfStamper stamp = new PdfStamper(reader,
				new FileOutputStream(tempPath));// 加完印章后的pdf

		PdfContentByte over = stamp.getOverContent(pageNum);// 设置在第几页打印印章

		Image img = Image.getInstance(markImagePath);// 选择图片
		float scalX = areaX/img.getWidth();
		float scalY = areaY/img.getHeight();
		
		float scale = scalX < scalY?scalX:scalY;
		Log.e("", "width = "+img.getWidth()+",height = "+img.getHeight());
		img.setAlignment(1);
		 img.scaleAbsolute(scale*img.getWidth(), scale*img.getHeight());// 控制图片大小
		// 控制图片位置：相对于文档页面的位置，左下角为原点(0, 0)
		// Log.d("qzc","width=="+width*pdfX/210+"---height=="+height*(1-pdfY/297));
		img.setAbsolutePosition(0, 15);
		over.addImage(img);

		stamp.close();

		File srcFile = new File(srcPath);
		srcFile.delete();
		File desFile = new File(tempPath);
		desFile.renameTo(srcFile);
	}

	public static class MyFont implements FontProvider {
		private static final String FONT_PATH = "/system/fonts/DroidSansFallback.ttf";

		private static final String FONT_ALIAS = "my_font";

		public MyFont() {
			FontFactory.register(FONT_PATH, FONT_ALIAS);
		}

		public MyFont(String filePath) {
			FontFactory.register(filePath, FONT_ALIAS);
		}

		@Override
		public Font getFont(String fontname, String encoding, boolean embedded,
				float size, int style, BaseColor color) {

			return FontFactory.getFont(FONT_ALIAS, BaseFont.IDENTITY_H,
					BaseFont.EMBEDDED, size, style, color);
		}

		@Override
		public boolean isRegistered(String name) {
			return name.equals(FONT_ALIAS);
		}
	}
}
