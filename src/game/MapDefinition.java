
package game;

import java.io.Serializable;

public class MapDefinition implements Serializable {

	
	private static final long serialVersionUID = -1434587864665515013L;

	public int theme;
	public int team1ColorIndex, team2ColorIndex;
	public int[] teamColorIndex = { 0, 1, 2 };
	
	public byte[][] byteLayer1 = new byte[64][64];
	public byte[][] byteLayer2 = new byte[64][64];
	
}
