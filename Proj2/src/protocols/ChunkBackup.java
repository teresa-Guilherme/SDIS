package protocols;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import fileManager.Chunk;
import fileManager.CsvHandler;
import listeners.SSL_Client;
import peer.Peer;

public class ChunkBackup implements Runnable{
	
	private Chunk chunk;
	private String version;
	
	public ChunkBackup(Chunk chunk,String version){		
		this.chunk=chunk;
		this.version=version;
	}

	@Override
	public void run() {
		
		Message message = new Message(chunk,version);
		((SSL_Client) Peer.getClientThread()).sendMessage(message.backupPeerSSL());
	}
}
