package game;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

public class ApplicationRoot {

	public File jarFileLocation;
	
	public ApplicationRoot() {
		//System.out.println("url" + ClassLoader.getSystemResource("game/Game.class") );
		String url = ClassLoader.getSystemResource("game/Game.class").toString();
		//System.out.println(url);
		
		try {
			url = java.net.URLDecoder.decode( url, System.getProperty("file.encoding") );
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		url = url.replace("file:", "");
		url = url.replace("jar:", "");
		jarFileLocation = new File(url);
		//jarFile = jarFile.getAbsoluteFile();
			System.out.println(jarFileLocation);
			jarFileLocation = jarFileLocation.getParentFile().getParentFile().getParentFile();
		System.err.println(jarFileLocation.exists());
		System.err.println("jar file locatioin: " + jarFileLocation);
		/*
		File testFile = new File("/media/kurtzmusch/appWindows/eclipse oxygen/gameWorkspace");
		System.err.println(testFile.exists());
		System.err.println(testFile);
		*/
	}
	
}
