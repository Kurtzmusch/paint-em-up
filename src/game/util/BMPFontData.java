package game.util;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class BMPFontData {

	public static LinkedList<LinkedList<Integer>> load( String path ) {
		// TODO passar arrays  para funcao preenhcer ao enves de retornar lista de listas
		try {
			
			BufferedImage img = ImageIO.read(new FileInputStream( path ));
			
			int h = img.getHeight();
			int w = img.getWidth();
			
			LinkedList<Integer> uList = new LinkedList<Integer>();
			LinkedList<Integer> vList = new LinkedList<Integer>();
			


			int cellW = 0;
			int cellH = 0;
			for( int x = 1; x < w; x ++ ) {
				
				int c = img.getRGB(x, 0);
				int g = (c& 0xff00) >> 8;
			
				if(g != 255) {
					cellW += 1;
				}
				else {
					break;
				}
			
			}
			
			for( int y = 0; y < h; y ++ ) {
				int c = img.getRGB(0, y);
				int g = (c& 0xff00) >> 8;
				
				if(g == 255) {
					cellH += 1;
				}
				else {
					break;
				}
			
			}
			
			for( int y = 0; y < h; y ++ ) {
				
				for( int x = 0; x < w; x ++ ) {
					
					int c = img.getRGB(x, y);
					
					int r = (c & 0xff0000) >> 16;
					int g = (c& 0xff00) >> 8;
					int b = (c & 0xff);
				
					if( (g == 255) && (r == 0) && (b == 0) ) {
						if(y == 0){
							uList.add(new Integer(x+1));
							uList.add(new Integer(x+cellW+1));
							uList.add(new Integer(x+1));
							uList.add(new Integer(x+cellW+1));
							
							vList.add(new Integer(y));
							vList.add(new Integer(y));
							vList.add(new Integer(y+cellH));
							vList.add(new Integer(y+cellH));
						}
						else {
							c = img.getRGB(x, y-1);
							g = (c& 0xff00) >> 8;
							
							if(g != 255) {
								uList.add(new Integer(x+1));
								uList.add(new Integer(x+cellW+1));
								uList.add(new Integer(x+1));
								uList.add(new Integer(x+cellW+1));
								vList.add(new Integer(y));
								vList.add(new Integer(y));
								vList.add(new Integer(y+cellH));
								vList.add(new Integer(y+cellH));
							}
						}
					}
				}
				
			}
			
		LinkedList<LinkedList<Integer>> list = new  LinkedList<LinkedList<Integer>>();
		LinkedList<Integer> cellList = new LinkedList<Integer>();
		
		cellList.add(new Integer(cellW));
		cellList.add(new Integer(cellH));
		
		list.add(uList);
		list.add(vList);
		list.add(cellList);
		
		System.out.println( list.toString() );
		
		return list;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
