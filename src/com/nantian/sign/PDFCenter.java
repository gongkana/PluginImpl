package com.nantian.sign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.nantian.entity.SignPDF;
import com.nantian.utils.HLog;

public class PDFCenter {

	public static boolean createPDF(String rawHTML, String fileName) {

		File file = new File(fileName);
		HLog.e("","fileName = "+fileName);
		showLogCompletion(rawHTML, 1000);
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
					Charset.defaultCharset(),new MyFont());

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

    public static void showLogCompletion(String log,int showCount){  
        if(log.length() >showCount){  
            String show = log.substring(0, showCount);  
            HLog.i("TAG", show+"");  
            if((log.length() - showCount)>showCount){//剩下的文本还是大于规定长度  
                String partLog = log.substring(showCount,log.length());  
                showLogCompletion(partLog, showCount);  
            }else{  
                String surplusLog = log.substring(showCount, log.length());  
                HLog.i("TAG", surplusLog+"");  
            }  
              
        }else{  
            HLog.i("TAG", log+"");  
        }  
    }  

	public static class MyFont implements FontProvider {
		private static final String FONT_PATH = "/system/fonts/songti.ttf";

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



    
    /**
     * 给pdf添加数字签名
     * @param src pdf文件路径
     * @param keyStorePath 数字证书路径
     * @param markImagePath 签名图片路径
     * @param fieldName 签名域名称
     * @param passWord 数字证书密码
     * @throws GeneralSecurityException
     * @throws IOException 密码错误会报IOException
     * @throws DocumentException
     */
    public static void sign(String markImagePath,SignPDF signInfo)
            throws GeneralSecurityException, IOException, DocumentException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        ks.load(new FileInputStream(signInfo.getKeySorePath()), signInfo.getPassword().toCharArray());
        String alias = (String)ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey)ks.getKey(alias, signInfo.getPassword().toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        // Creating the reader and the stamper
        HLog.e("", signInfo.toString());
        PdfReader reader = new PdfReader(signInfo.getPdfPath());
        
        
        Rectangle pageSize = reader.getPageSize(signInfo.getPageNum());
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();
        String tempPath = signInfo.getPdfPath().replace(".", "_new.");
        FileOutputStream os = new FileOutputStream(tempPath);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(signInfo.getReason());
        appearance.setLocation("this is the location");
        Image img = Image.getInstance(markImagePath);
        
        //float newHeight = (float) (signInfo.getHeight()*0.8f);
        //float newWeight = (float) (signInfo.getWidth()*0.8f);
        //img.scaleAbsolute(newWeight, newHeight);
        
        HLog.e("", "markImagePath...");
        HLog.e("", signInfo.toString());
        float[] start = getKeyWords(reader, signInfo.getPageNum());
        HLog.e("", "x=="+start[0]+"---y=="+start[1]);
        appearance.setVisibleSignature(
        		new Rectangle(
        				(float) start[0],
        				(float) (start[1]-signInfo.getHeight()*0.8f),
        				(float) (start[0]+signInfo.getWidth()*0.8f),
        				(float) (start[1])),signInfo.getPageNum(),"Signature1");
        // Custom text and background image
        appearance.setLayer2Text("");
        appearance.setImage(img);
        appearance.setImageScale((float) ((signInfo.getWidth()*0.8f)/img.getWidth()));
        // Creating the signature
        PrivateKeySignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
        File srcFile = new File(signInfo.getPdfPath());
        srcFile.delete();
        File desFile = new File(tempPath);
        desFile.renameTo(srcFile);
    }
    
    private static float[] getKeyWords(PdfReader pdfReader,int num) {
        final float[] resu = new float[2];
        try {
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(
                    pdfReader);

            pdfReaderContentParser.processContent(num, new RenderListener() {
                @Override
                public void renderText(TextRenderInfo textRenderInfo) {
                    String text = textRenderInfo.getText(); // 整页内容

                    if (null != text && text.contains("sign")) {
                        Rectangle2D.Float boundingRectange = textRenderInfo
                                .getBaseline().getBoundingRectange();


                        resu[0] = boundingRectange.x;
                        resu[1] = boundingRectange.y;
                    }
                }

                @Override
                public void renderImage(ImageRenderInfo arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void endTextBlock() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void beginTextBlock() {
                    // TODO Auto-generated method stub

                }
            });

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resu;
    }
}
