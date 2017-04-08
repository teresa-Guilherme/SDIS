package protocols;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import peer.Peer;

public class ServiceState {

	public static synchronized void getServiceState() {
		
		File metaDataInit = new File("../metadata"+Peer.getPeerId()+"/MyChunks.csv");
		File metaDataStored = new File("../metadata"+Peer.getPeerId()+"/ChunkList.csv");
		
		System.out.println(" -- SERVICE STATE -- ");
		System.out.println();
		System.out.println("This Peer ("+Peer.getPeerId()+")has initiated the backup of the following files:");
		
		int counter = 0;
		String nextChunk;
		String lastChunk = null;
		Scanner scannerInit;
		try {
			scannerInit = new Scanner(metaDataInit);
			scannerInit.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scannerInit.hasNext()){
				String str=scannerInit.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				nextChunk = divided[0];
				if (nextChunk != lastChunk){
					if (counter > 0) {
						System.out.println("  ----------  ");
					}
					counter++;
					System.out.println();
					System.out.println("File "+counter+":");
					System.out.println("Name:" + divided[4]);
					System.out.println("Backup-ID: "+divided[0]);
					System.out.println("Desired Replication Degree: "+divided[3]);
					System.out.println();
					System.out.println("With the chunks:");
					System.out.println(divided[0]+"."+divided[1]);
					//To implement
					System.out.println("Percieved Replication Degree: "+"TO IMPLEMENT");
					System.out.println();
		
				}
				else if (nextChunk == lastChunk) {
					System.out.println(divided[0]+"."+divided[1]);
					//To implement
					System.out.println("Percieved Replication Degree: "+"TO IMPLEMENT");
					System.out.println();
				}
				lastChunk = divided[0];	
			}
			scannerInit.close();
			
			System.out.println("This Peer ("+Peer.getPeerId()+")has stored the following chunks:");
			
			Scanner scannerChunks;
				scannerChunks = new Scanner(metaDataStored);
				scannerChunks.useDelimiter(Constants.NEW_LINE_SEPARATOR);
				while(scannerChunks.hasNext()){
					String str=scannerInit.next();
					String[] divided = str.split(Constants.COMMA_DELIMITER);
					System.out.println();
					System.out.println(divided[0]+"."+divided[1]);
					//To implement
					System.out.println("Percieved Replication Degree: "+"TO IMPLEMENT");
					System.out.println();
					
				}
				scannerChunks.close();
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("This Peer has x KBytes of capacity to store chunks");
		System.out.println("This Peer uses x KBytes of capacity to store chunks");
		System.out.println("  ---------  ");
		System.out.println("");
		
		System.out.println(" -- END OF SERVICE STATE -- ");
	}
}