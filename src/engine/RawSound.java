
package engine;

import java.nio.ByteBuffer;

import org.lwjgl.openal.AL10;

import engine.util.WaveData;

public class RawSound {	
	
	int bufferID;
	int format, sampleRate;
	ByteBuffer rawData;

	/*
	 * loads a sound file as a raw data buffer
	 */
	public RawSound( String soundFile) {
		WaveData wd = WaveData.create( soundFile );
		format = wd.format;
		rawData = wd.data;
		sampleRate = wd.samplerate;
		wd.dispose();
	}
	
	/*
	 * sends this audio raw data buffer to the "audio processing unit" - whatever it is
	 *
	 */
	public void sendToAPU() {
		bufferID = AL10.alGenBuffers();
		AL10.alBufferData( bufferID, format, rawData, sampleRate );
	}
	
	public void destroy() {
		//TODO
	}
	
}
