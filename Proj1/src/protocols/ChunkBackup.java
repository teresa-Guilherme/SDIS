package protocols;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fileManager.Chunk;
import peer.Peer;

public class ChunkBackup implements Runnable{
	
	private File file;
	private int replication;
	
	private HashMap<String,Integer> backedUp = new HashMap<String,Integer>();
	private List <String> myChunks = new ArrayList<String>();
	
	public ChunkBackup(File f, int repl){		
		file=f;
		replication=repl;
	}

	@Override
	public void run() {
		int chunkNo = 1;
		byte[] buffer = new byte[Constants.CHUNKSIZE];
		
		try {
						
			BufferedInputStream bufferInput = new BufferedInputStream(new FileInputStream(file));
			
			@SuppressWarnings("unused")
			int bytesRead;
			
			//to implement: Partition of file content into chunks 
			
			 while ((bytesRead = bufferInput.read(buffer)) > 0) {
	             
				 MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				 messageDigest.update((file.getName()+Long.toString(file.lastModified())).getBytes());
				 String fileIDbin = new String(messageDigest.digest());
				 String fileID = String.format("%040x", new BigInteger(1, fileIDbin.getBytes()));
				 
	             Chunk chunk = new Chunk(fileID,chunkNo,buffer,replication);
	             
	             Message message = new Message(chunk);
	    
	             sendToMDB(message.createPutChunk());
	             
	             addMyChunks(fileID, chunkNo);
	             
	            }
			 
			 bufferInput.close();
			 
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	private void sendToMDB(byte[] buffer) throws IOException {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, Peer.getMdbAddress(),Peer.getMdbPort());
		MulticastSocket socket = new MulticastSocket();
		socket.send(packet);
		socket.close();
	}
	
	

	public int getReplication() {
		return replication;
	}
	
	public void addMyChunks(String id, int chunkNo){
		myChunks.add(id+ " "+ Integer.toString(chunkNo));
	}
	
	public boolean isMyChunk(String id, int chunkNo){
		return myChunks.contains(id+ " "+ Integer.toString(chunkNo));		
	}

	public void addToBackedUp(int i,String str){
		backedUp.put(str, i);
	}
}
