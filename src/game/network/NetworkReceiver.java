package game.network;

public interface NetworkReceiver {

	abstract boolean getReceived();
	abstract void request();
	
}
