
package engine;

import org.lwjgl.openal.AL10;

public class SoundSource {

	
	int sourceID;
	
	public SoundSource() {
		sourceID = AL10.alGenSources();
	}
	
	public void setRawSound( RawSound rawSound ) {
		AL10.alSourcei( sourceID, AL10.AL_BUFFER, rawSound.bufferID );
	}
	
	public void play() {
		
		AL10.alSourcePlay( sourceID );
	
	}
	
	public void destroy() {
		//TODO
	}
	
}
