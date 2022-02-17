
package game;

public class NetworkProtocol {

	public static final int NULL_REQUEST = 10000;
	
	public static final byte CONNECTION_REQUEST = 11;
	public static final byte CONNECTION_DROP = 12;
	public static final byte CONNECTION_ACCEPTED = 13;
	
	public static final byte MESSAGE = 2;
	
	public static final byte MAP_HEADER = 31;
	public static final byte MAP_GRID = 32;
	public static final byte MAP_PATH_HEADER = 33;
	public static final byte MAP_PATH_DATA = 34;
	
	
	public static final byte PING = 4;
	
	public static final byte MATCH_META = 51;
	public static final byte MATCH_BOARD = 52;

	public static final byte PLAYER_UPDATE = 6;
	
	public static final byte PLAYER_JOIN = 7;
	public static final byte PLAYER_SELECT_GUN = 70;
	public static final byte PLAYER_JOIN_ACCEPTED = 71;
	public static final byte PLAYER_JOIN_SESSION_FULL = 72;
	public static final byte PLAYER_JOIN_ALREADY_JOINED = 73;
	
	public static final byte PLAYER_LEAVE = 8;	
	
	public static final byte PLAYER_RESPAWN_CONFIRM = 9;
	public static final byte PLAYER_RESPAWN_COUNTDOWN = 91;
	
	public static final byte CLIENT_INPUT = 10;
	
}
