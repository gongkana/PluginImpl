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

import android.os.Environment;
import android.text.TextUtils;

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
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.tool.xml.XMLWorkerHelper;



public class PDFCenter {
	
	public static boolean createPDF(String rawHTML, String fileName) {

        File file = new File(fileName);

        try {

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            //  HTML
            //String htmlText = Jsoup.clean(rawHTML, Whitelist.relaxed());
            InputStream inputStream = new ByteArrayInputStream(rawHTML.getBytes());

            //  PDF
            XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                    inputStream, null, Charset.defaultCharset(), new MyFont(Environment.getExternalStorageDirectory() + "/SIMKAI.TTF"));

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
     * @param srcPath pdf路径
     * @param pageNum 签名在第几页
     * @param pdfX pdf签名的x坐标(单位mm)
     * @param pdfY pdf签名的y坐标(单位mm)
     * @param markImagePath 水印图片路径
     * @throws Exception
     */
    public static void signInPDF(String srcPath, int pageNum,float pdfX,float pdfY,
                                 String markImagePath) throws Exception {
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
        img.setAlignment(1);
        //int newWidth =(int) (width*picWithfOriginalWith);
        //int newHeight = (int) (height*picHeightfOriginalHeight);
        //img.scaleAbsolute(newWidth, newHeight);// 控制图片大小
        // 控制图片位置：相对于文档页面的位置，左下角为原点(0, 0)
        //Log.d("qzc","width=="+width*pdfX/210+"---height=="+height*(1-pdfY/297));
        img.setAbsolutePosition(pdfX*72,
                pdfY*72);
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
    public void sign(String src,String keyStorePath,String markImagePath,String fieldName,String passWord)
            throws GeneralSecurityException, IOException, DocumentException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        ks.load(new FileInputStream(keyStorePath), passWord.toCharArray());
        String alias = (String)ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey)ks.getKey(alias, passWord.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        String tempPath = src.replace(".", "_new.");
        FileOutputStream os = new FileOutputStream(tempPath);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("this is the reason");
        appearance.setLocation("this is the location");
        Image img = Image.getInstance(markImagePath);
        if(TextUtils.isEmpty(fieldName)){
            appearance.setVisibleSignature("DigitalSignatureField_0");
        }else{
            appearance.setVisibleSignature(fieldName);
        }
        // Custom text and background image
        appearance.setLayer2Text("");
        appearance.setImage(img);
        appearance.setImageScale(1);
        // Creating the signature
        PrivateKeySignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
        File srcFile = new File(src);
        srcFile.delete();
        File desFile = new File(tempPath);
        desFile.renameTo(srcFile);
    }
}
