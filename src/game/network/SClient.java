package game.network;

import java.net.DatagramPacket;
import java.net.InetAddress;

import game.thing.Weapon;
import game.thing.server.PlayerOwner;
import game.thing.server.SSSpawner;

public class SClient {

	public String nickname;
	public InetAddress ip;
	public int port;
	public DatagramPacket packet;
	public int id;
	public PlayerOwner playerOwner;
	public SSSpawner spawner;
	
	public SClient( InetAddress ip, int port, String aNickname ) {
		
		nickname = aNickname;
		this.ip = ip;
		this.port = port;
		
		playerOwner = new PlayerOwner();
		
		playerOwner.nickname = nickname;
		playerOwner.weaponType = Weapon.TYPE_DEAGLE;
		
	}
	
	public void changeWeaponType( byte type ) {
		if( ( type >= 0 ) && ( type < Weapon.MAX_TYPE ) ) {
			System.out.println("Server: weapon changed");
			playerOwner.weaponType = type;
		}
	}
}
