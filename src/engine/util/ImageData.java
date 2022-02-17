package engine.util;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class ImageData {

	public static LinkedList<int[]> load( InputStream is ) {
		
		try {
			
			BufferedImage img = ImageIO.read( is );
			
			int h = img.getHeight();
			int w = img.getWidth();
			
			int[] pixel;
			
			pixel = new int[h*w];
			
			img.getRGB(0, 0, w, h, pixel, 0, w );
			
			//int[] correctPixel = new int[h*w];
			int a,r,g,b;
			for( int i = 0; i < pixel.length; i ++ ) {
				a = (pixel[i] & 0xff000000) >> 24;
				r = (pixel[i] & 0xff0000) >> 16;
				g = (pixel[i] & 0xff00) >> 8;
				b = (pixel[i] & 0xff);
			
				pixel[i] = a << 24 | b << 16 | g << 8 | r;
			}
			
			LinkedList<int[]> list = new LinkedList<int[]>();
			list.add(pixel);
			
			int[] dimensions = new int[2];
			dimensions[0] = w;
			dimensions[1] = h;
			
			list.add(dimensions);
			
			return(list);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static LinkedList<int[]> load( BufferedImage img ) {
		int h = img.getHeight();
		int w = img.getWidth();
		
		int[] pixel;
		
		pixel = new int[h*w];
		
		img.getRGB(0, 0, w, h, pixel, 0, w );
		
		//int[] correctPixel = new int[h*w];
		int a,r,g,b;
		for( int i = 0; i < pixel.length; i ++ ) {
			a = (pixel[i] & 0xff000000) >> 24;
			r = (pixel[i] & 0xff0000) >> 16;
			g = (pixel[i] & 0xff00) >> 8;
			b = (pixel[i] & 0xff);
		
			pixel[i] = a << 24 | b << 16 | g << 8 | r;
		}
		
		LinkedList<int[]> list = new LinkedList<int[]>();
		list.add(pixel);
		
		int[] dimensions = new int[2];
		dimensions[0] = w;
		dimensions[1] = h;
		
		list.add(dimensions);
		
		return(list);
	}
	
	public static LinkedList<int[]> load( String path ) {
		
		try {
			
			BufferedImage img = ImageIO.read(new FileInputStream( path ));
			
			int h = img.getHeight();
			int w = img.getWidth();
			
			int[] pixel;
			
			pixel = new int[h*w];
			
			img.getRGB(0, 0, w, h, pixel, 0, w );
			
			//int[] correctPixel = new int[h*w];
			int a,r,g,b;
			for( int i = 0; i < pixel.length; i ++ ) {
				a = (pixel[i] & 0xff000000) >> 24;
				r = (pixel[i] & 0xff0000) >> 16;
				g = (pixel[i] & 0xff00) >> 8;
				b = (pixel[i] & 0xff);
			
				pixel[i] = a << 24 | b << 16 | g << 8 | r;
			}
			
			LinkedList<int[]> list = new LinkedList<int[]>();
			list.add(pixel);
			
			int[] dimensions = new int[2];
			dimensions[0] = w;
			dimensions[1] = h;
			
			list.add(dimensions);
			
			return(list);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
