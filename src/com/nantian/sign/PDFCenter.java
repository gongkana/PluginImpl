package com.nantian.sign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.graphics.pdf.PdfRenderer.Page;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.Header;
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
import com.nantian.pluginImpl.DataException;
import com.nantian.utils.HLog;
import com.nantian.utils.Utils;


public class PDFCenter {

	public static boolean createPDF(String rawHTML, String fileName,String hender) {

		File file = new File(fileName);
		HLog.e("","fileName = "+fileName);
		showLogCompletion(rawHTML, 1000);
		try {
			if (file.exists()) {
				file.delete();
			}
			if (!file.getParentFile().exists()){
				file.getParentFile().mkdir();
			}
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(file));
			document.open();
			document.add(new Header("signPath", hender));
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
     * @throws DataException 
     * @throws GeneralSecurityException
     * @throws IOException 密码错误会报IOException
     * @throws DocumentException
     */
    public static void sign(String markImagePath,SignPDF signInfo,String signTrack) throws DataException{
        try {
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
			String tempPath = signInfo.getPdfPath().replace(".", "_new.");
			FileOutputStream os = new FileOutputStream(tempPath);
			PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
			if (null != signTrack && !signTrack.equals("")){
				Map<String, String> signTrackMap = new HashMap<String, String>();
				signTrackMap.put("signTrack", signTrack);
				stamper.setMoreInfo(signTrackMap);
			}
			// Creating the appearance
			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
      // appearance.setReason(signInfo.getReason());
			appearance.setLocation("this is the location");
			Image img = Image.getInstance(markImagePath);
			
			//float newHeight = (float) (signInfo.getHeight()*0.8f);
			//float newWeight = (float) (signInfo.getWidth()*0.8f);
			//img.scaleAbsolute(newWeight, newHeight);
			float pagewidth = pageSize.getWidth();
			float pageheight = pageSize.getHeight();
			float realWidth = (float) (pagewidth*signInfo.getWidth());
			float realHeight = (float) (pageheight*signInfo.getHeight());
			
			HLog.e("", "markImagePath...");
			HLog.e("", signInfo.toString());
			float width = img.getWidth();
			float height = img.getHeight();
			if (width > realWidth){			
				float tempHeight = (float) (height * realWidth/width);
				if (tempHeight <= realHeight){
					width = (float) realWidth;
					height = tempHeight;
				}else {		
					width  =(float) (width * realHeight/height);
					height = (float) realHeight;
				}			
			} else {
				if (height > realHeight){
					width = (float) (width * realHeight/height);
					height = (float) realHeight;
				}
			}
			float[] start = new float []{(float) signInfo.getPointX()*pagewidth,(float) (1-signInfo.getPointY())*pageheight};
			appearance.setVisibleSignature(
					new Rectangle(
							(float) start[0],
							(float) (start[1]-height),
							(float) (start[0]+width),
							(float) (start[1])),signInfo.getPageNum(),appearance.getNewSigName());
			// Custom text and background image
			appearance.setLayer2Text("");
			appearance.setImage(img);
			float xScal = (float) (realWidth)/img.getWidth();
			float yScal = (float) (realHeight)/img.getHeight();
			float scal = xScal<yScal?xScal:yScal;
			if (scal < 1){
				appearance.setImageScale(scal);
			}
			// Creating the signature 后
			PrivateKeySignature pks = new PrivateKeySignature(pk, "SM3", provider.getName());
			ExternalDigest digest = new BouncyCastleDigest();
			MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
			//File srcFile = new File(signInfo.getPdfPath());
			//srcFile.delete();
			//File desFile = new File(tempPath);
			//desFile.renameTo(srcFile);
		} catch (UnrecoverableKeyException e) {
			throw new DataException(-5,e);
		} catch (KeyStoreException e) {
			throw new DataException(-205,e);
		} catch (NoSuchAlgorithmException e) {
			throw new DataException(-5,e);
		} catch (CertificateException e) {
			throw new DataException(-205);
		} catch (FileNotFoundException e) {
			throw new DataException(-204,e);
		} catch (BadElementException e) {
			throw new DataException(-5,e);
		} catch (MalformedURLException e) {
			throw new DataException(-5,e);
		} catch (IOException e) {
			throw new DataException(-5,e);
		} catch (DocumentException e) {
			throw new DataException(-201,e);
		} catch (GeneralSecurityException e) {
			throw new DataException(-5,e);
		}
    }
    /**
    public static void pdfToImage1(String pdfdir,String pdfName,String outDir) throws DataException{
    	
    	File pdfFile = new File(pdfdir, pdfName);
    	 RandomAccessFile raf = null;
		FileChannel channel = null;
		try {
			raf = new RandomAccessFile(pdfFile, "r");
			 
			 channel = raf.getChannel();

			 ByteBuffer bb = ByteBuffer.NEW(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
			 // create a PDFFile from the data

			 PDFFile mCurrentOpenPdfFile = new PDFFile(bb);
			 
			 int pageSize = mCurrentOpenPdfFile.getNumPages();
			 for (int i = 0; i < pageSize; i++) {
					PDFPage mPdfPage = mCurrentOpenPdfFile.getPage(i+1, true);
					
			        int wi = (int) mPdfPage.getWidth();
			        int hei = (int) mPdfPage.getHeight();		        
			        Bitmap bitmap = mPdfPage.getImage( wi, hei, null, true, true);
					File outdir = new File(outDir);
					if (!outdir.exists()){
						outdir.mkdirs();
					}
					Utils.savaBitmap(outdir, (i+1)+".png", bitmap);
			        
			}
			 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
	         try {
				raf.close();
				channel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

         

    }
    */
    public static ArrayList<String> pdfToBitmap(String pdfdir,String pdfName,String outDir) throws DataException {
		
			
			File pdfCopy = new File(pdfdir,pdfName);
			ParcelFileDescriptor parcelFileDescriptor = null;
			if (pdfCopy.exists()) {
				try {
					parcelFileDescriptor = ParcelFileDescriptor.open(pdfCopy,
							ParcelFileDescriptor.MODE_READ_ONLY);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new DataException(-5, e);
				}
			
			}else {
				throw new DataException(-3, "文件不存在");
			}
			
			@SuppressWarnings("resource")
			PdfRenderer reader = null;
			try {
				reader = new PdfRenderer(parcelFileDescriptor);
			int count = reader.getPageCount();
			ArrayList<String> fileList = new ArrayList<String>();
			for (int i = 0; i < count; i++) {
				Page page = reader.openPage(i);
				try {
				Bitmap bitmap = Bitmap.createBitmap(page.getWidth(),
						page.getHeight(), Bitmap.Config.ARGB_8888);
				   page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);  
				
					File outdir = new File(outDir);
					Utils.delete(outdir);
					if (!outdir.exists()){
						outdir.mkdirs();
					}
					Utils.savaBitmap(outdir, i+".png", bitmap);
					fileList.add(i+".png");
				} catch (Exception e) {
					e.printStackTrace();
					throw new DataException(-5, e);
				}finally{
					HLog.e("", "page close");
					page.close();
				}
			}
			return fileList;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DataException(-5, e);
		}finally{
			reader.close();
			try {
				parcelFileDescriptor.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
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
