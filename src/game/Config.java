
package game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Config implements Serializable {

	
	private static final long serialVersionUID = 7L;
	
	public String nickname = "Unnamed";
	
	public String mouseSpeed = "100";
	public String maximumParticles = "x4";
	public String vSync = "OFF";
	public String fullScreen = "OFF";
	public String masterVolume = "100";
	public String effectsVolume = "100";
	public String musicVolume = "100";
	
	public String joinPort = "60000";
	public String joinIP = "127.0.0.1";
	
	public String hostPort = "60000";
	public String hostMap = "random";
	public String hostType = "DeathMatch";
	
	public String mapperSave = "";
	public String mapperLoad = "";
	
	public String guestSave = "";
	
	public static Config load() {
		
		ObjectInputStream ois;
		FileInputStream fis;
		
		try {
			fis = new FileInputStream(Game.applicationRoot.jarFileLocation + "/peuconf/config.cfg");
		    ois = new ObjectInputStream(fis);
		    Config config = (Config) ois.readObject();
		    fis.close();
		    ois.close();
		    return config;
		} catch (Exception e) {
		    e.printStackTrace();
		}

		return null;
	
	}
	
	public static void save( Config config ) {
		
		String savedConfFolder = Game.applicationRoot.jarFileLocation + "/peuconf";
		if( ! new File(savedConfFolder).exists() ) {
			new File(savedConfFolder).mkdir();
		}
		FileOutputStream fos;
		ObjectOutputStream oos;
		try{
		    fos = new FileOutputStream( (new File(savedConfFolder).getPath()) +"/"+ "config.cfg", false);
		    oos = new ObjectOutputStream(fos);
		    oos.writeObject( config );
		    oos.close();
		    fos.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
	}
	
}
