package com.kanasansoft.WebSocketRemote;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import com.sun.imageio.plugins.png.*;

import org.apache.commons.codec.binary.Base64;

public class Capture {

	public static void main(String[] args) throws AWTException, IOException {
		System.out.println(new Date().getTime());
		new Capture().capture();
		System.out.println(new Date().getTime());
	}

	public void capture() throws AWTException, IOException{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gds = ge.getScreenDevices();
		Rectangle rect=new Rectangle(0,0,-1,-1);
		for(int i=0;i<gds.length;i++){
			rect.add(gds[i].getDefaultConfiguration().getBounds());
		}
		Robot robot = new Robot();
		BufferedImage bf = robot.createScreenCapture(rect);
		IIOImage iioi = new IIOImage(bf,null,null);
		ImageWriter iw = null;
		Iterator<ImageWriter> iwi = ImageIO.getImageWritersByFormatName("png");
		if(iwi.hasNext()){
			iw = iwi.next();
		}
		if(iw==null){
			System.exit(0);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
		iw.setOutput(ios);
		ImageWriteParam iwp = iw.getDefaultWriteParam();
//		PNGImageWriteParam iwp = new PNGImageWriteParam();
//		ImageWriteParam iwp = new ImageWriteParam(null);
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.1F);
		iw.write(null,iioi,iwp);
//		ImageIO.write(bf,"png",baos);

		byte[] bytes = baos.toByteArray();
		byte[] base64 = Base64.encodeBase64(bytes);
		String encoded = new String(base64);
		System.out.println(encoded.length());
/*
		File file = new File("base64.txt");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(encoded);
		fos.close();
*/
	}
}
