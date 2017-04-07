package protocols;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fileManager.Chunk;
import fileManager.CsvHandler;

public class ReadFile implements Runnable {

	private File file;
	private int replication;
	
	public ReadFile(File f, int replication) {
		this.file = f;
		this.replication = replication;
	}
	
	public void run() {
		
		try {
			
			BufferedInputStream bufferInput = new BufferedInputStream(new FileInputStream(file));
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update((file.getName()+Long.toString(file.lastModified())).getBytes());
			String fileIDbin = new String(messageDigest.digest());
			String fileID = String.format("%040x", new BigInteger(1, fileIDbin.getBytes()));
			
			int chunkNo = 1;
			int fileSize = (int) file.length();
			
			int headerSize = Constants.COMMAND_PUT.length() + 1;
			headerSize += Message.getVersion().length() + 1;
			headerSize += fileID.length() + 1;
			if (replication < 10 && replication > 0) {
				headerSize += 2;
			}
			else if (replication > 100 && replication < 1000) {
				headerSize += 3;
			}
			else {
				headerSize += 4;
			}
			headerSize+=2*(Constants.CRLF.length());
			
			int chunkSize = Constants.CHUNKSIZE - headerSize;
			
			byte[] temporary = null;
			  
			int totalBytesRead = 0;
			
			while ( totalBytesRead < fileSize ) {
				
				int bytesRemaining = fileSize - totalBytesRead;
			    if ( bytesRemaining < Constants.CHUNKSIZE) {
			    	chunkSize = bytesRemaining;
			    }
			    temporary = new byte[chunkSize];
			    int bytesRead = bufferInput.read(temporary, 0, chunkSize);
			    
			    Chunk chunk = new Chunk(fileID, chunkNo, temporary, replication);
				//Peer.addBackup(chunk,null);
				CsvHandler.updateMyChunks(chunk,file.getName(),0);
				Runnable run=new ChunkBackup(chunk);
				new Thread(run).start();
			    
			    if (bytesRead > 0) {
			    	totalBytesRead += bytesRead;
			    	chunkNo++;
			    }
			}
			
			bufferInput.close();
			
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
