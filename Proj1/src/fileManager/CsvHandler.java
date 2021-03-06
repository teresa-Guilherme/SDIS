package fileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import peer.Peer;
import protocols.Constants;

public class CsvHandler {
	
	public synchronized static void deleteChunks(String fileId,String path){
		File metaData = new File(path);
		Scanner scanner;
		
		List<String> metaArray=new ArrayList<String>();
		
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(divided.length>1 &&!divided[0].equals(fileId)){
					metaArray.add(str);
				}
	        }
			
			FileWriter fileWriter = new FileWriter(path);
			for(String str: metaArray){
				fileWriter.append(str);
				fileWriter.append(Constants.NEW_LINE_SEPARATOR);
			}
				
			fileWriter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized static int updateNegative(Chunk chunk, String path){
		File metaData = new File(path);
		Scanner scanner;		
		List<String> metaArray=new ArrayList<String>();	
		int replicationDegree=-1;
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);

			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(divided.length>1 &&Integer.parseInt(divided[1])==chunk.getChunkNumber() && divided[0].equals(chunk.getFileId())){				
					if(divided.length==5)
						metaArray.add(divided[0]+Constants.COMMA_DELIMITER+divided[1]+Constants.COMMA_DELIMITER+divided[2]+Constants.COMMA_DELIMITER+(Integer.parseInt(divided[3])-1)+Constants.COMMA_DELIMITER+divided[4]+Constants.COMMA_DELIMITER);
					else if(divided.length==4){
						metaArray.add(divided[0]+Constants.COMMA_DELIMITER+divided[1]+Constants.COMMA_DELIMITER+divided[2]+Constants.COMMA_DELIMITER+(Integer.parseInt(divided[3])-1)+Constants.COMMA_DELIMITER);
					}
					if(Integer.parseInt(divided[3])-1<Integer.parseInt(divided[2])){
						replicationDegree=Integer.parseInt(divided[2]);
					}
				}else metaArray.add(str);
	        }
			
			FileWriter fileWriter = new FileWriter(path, false);
			for(String str: metaArray){
				fileWriter.append(str);
				fileWriter.append(Constants.NEW_LINE_SEPARATOR);
			}
				
			fileWriter.close();				
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return replicationDegree;
	}
	
	public synchronized static String getHash(String name){
		File metaData = new File("../metadata"+Peer.getPeerId()+"/MyChunks.csv");
		Scanner scanner;
		try {
			scanner = new Scanner(metaData);	
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(divided.length>1 &&divided[4].equals(name)){
					scanner.close();
					return divided[0];
				}
	        }
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized static int repliMyChunk(Chunk chunk,String path){
		File metaData = new File(path);
		Scanner scanner;
		try {
			scanner = new Scanner(metaData);
			
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(divided.length>1 &&Integer.parseInt(divided[1])==chunk.getChunkNumber() && divided[0].equals(chunk.getFileId())){
					scanner.close();
					return Integer.parseInt(divided[3]);
				}
	        }
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public synchronized static int numberOfChunks(String fileId){
		File metaData = new File("../metadata"+Peer.getPeerId()+"/MyChunks.csv");
		Scanner scanner;
		int counter=0;
		try {
			scanner = new Scanner(metaData);
			
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(divided.length>1 &&divided[0].equals(fileId)){
					counter++;
				}
	        }
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return counter;
	}
	
	public synchronized static boolean isMyChunk(Chunk chunk,String path){
		File metaData = new File(path);
		Scanner scanner;
		Boolean iHaveChunk = false;
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(divided.length>1 &&Integer.parseInt(divided[1])==chunk.getChunkNumber() && divided[0].equals(chunk.getFileId())){
					iHaveChunk=true;
				}
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return iHaveChunk;
	}
	
	public synchronized static void updateMyChunks(Chunk chunk,String name,int update){
		
		File metaData = new File("../metadata"+Peer.getPeerId()+"/MyChunks.csv");
		Scanner scanner;
		
		List<String> metaArray=new ArrayList<String>();
		
		Boolean iHaveChunk = false;
		
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(divided.length>1 && Integer.parseInt(divided[1])==chunk.getChunkNumber() && divided[0].equals(chunk.getFileId())){
					iHaveChunk=true;
					if(update==1)
						metaArray.add(divided[0]+Constants.COMMA_DELIMITER+divided[1]+Constants.COMMA_DELIMITER+divided[2]+Constants.COMMA_DELIMITER+(Integer.parseInt(divided[3])+1)+Constants.COMMA_DELIMITER+divided[4]+Constants.COMMA_DELIMITER);
					else if(update==0)
						metaArray.add(divided[0]+Constants.COMMA_DELIMITER+divided[1]+Constants.COMMA_DELIMITER+divided[2]+Constants.COMMA_DELIMITER+0+Constants.COMMA_DELIMITER+divided[4]+Constants.COMMA_DELIMITER);
				}else metaArray.add(str);
	        }
			
			if(iHaveChunk){
				FileWriter fileWriter = new FileWriter("../metadata"+Peer.getPeerId()+"/MyChunks.csv", false);
				for(String str: metaArray){
					fileWriter.append(str);
					fileWriter.append(Constants.NEW_LINE_SEPARATOR);
				}
				
				fileWriter.close();
			}else{
				FileWriter fileWriter = new FileWriter("../metadata"+Peer.getPeerId()+"/MyChunks.csv", true);
				
				fileWriter.append(chunk.getFileId());
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(Integer.toString(chunk.getChunkNumber()));
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(Integer.toString(chunk.getReplication()));
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(Integer.toString(0));
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(name);
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(Constants.NEW_LINE_SEPARATOR);
				
				fileWriter.close();
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public synchronized static Chunk eliminateBadChunk(){
		System.out.println("vou eliminar bad chunk");
		
		File metaData = new File("../metadata"+Peer.getPeerId()+"/ChunkList.csv");
		Scanner scanner;
		List<String> metaArray=new ArrayList<String>();
		Boolean iHaveChunk = false;		
		Chunk chunk=null;
		
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(!iHaveChunk){
					iHaveChunk=true;
					chunk=new Chunk(divided[0],Integer.parseInt(divided[1]),null,0);
					HandleFiles.eraseFile("../Chunks"+Peer.getPeerId()+"/"+divided[0]+"."+divided[1]);
				}else metaArray.add(str);
	        }
			
			if(iHaveChunk){
				FileWriter fileWriter = new FileWriter("../metadata"+Peer.getPeerId()+"/ChunkList.csv", false);
				for(String str: metaArray){
					fileWriter.append(str);
					fileWriter.append(Constants.NEW_LINE_SEPARATOR);
				}
				
				fileWriter.close();
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return chunk;
	}
	
	public synchronized static Chunk eliminateGoodChunk(){
		File metaData = new File("../metadata"+Peer.getPeerId()+"/ChunkList.csv");
		Scanner scanner;
		
		List<String> metaArray=new ArrayList<String>();
		
		Boolean iHaveChunk = false;
		
		Chunk chunk=null;
		
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				System.out.println(Integer.parseInt(divided[2]));
				System.out.println(Integer.parseInt(divided[3]));
				if(Integer.parseInt(divided[2])<Integer.parseInt(divided[3]) && !iHaveChunk){
					iHaveChunk=true;
					System.out.println("vou apagar");
					chunk=new Chunk(divided[0],Integer.parseInt(divided[1]),null,0);
					HandleFiles.eraseFile("../Chunks"+Peer.getPeerId()+"/"+divided[0]+"."+divided[1]);
				}else metaArray.add(str);
	        }
			
			if(iHaveChunk){
				FileWriter fileWriter = new FileWriter("../metadata"+Peer.getPeerId()+"/ChunkList.csv", false);
				for(String str: metaArray){
					fileWriter.append(str);
					fileWriter.append(Constants.NEW_LINE_SEPARATOR);
				}
				
				fileWriter.close();
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return chunk;
	}
	
	public synchronized static int getMemory(){
		File metaData = new File("../metadata"+Peer.getPeerId()+"/MyChunks.csv");
		Scanner scanner;
		int memory=0;
		try {
			scanner = new Scanner(metaData);
			if(scanner.hasNext()){
				String str=scanner.next();
				memory=Integer.parseInt(str);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return memory;
	}
	
	public synchronized static void updateMemory(int memory){
		File metaData = new File("../metadata"+Peer.getPeerId()+"/MyChunks.csv");
		Scanner scanner;		
		List<String> metaArray=new ArrayList<String>();
		
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			String str;
			if(scanner.hasNext()){
				str=scanner.next();
			}
			metaArray.add(String.valueOf(memory)+Constants.COMMA_DELIMITER);
			while(scanner.hasNext()){
				str=scanner.next();
				metaArray.add(str);
	        }

			FileWriter fileWriter = new FileWriter("../metadata"+Peer.getPeerId()+"/MyChunks.csv", false);
			for(String str1: metaArray){
				fileWriter.append(str1);
				fileWriter.append(Constants.NEW_LINE_SEPARATOR);
			}
			fileWriter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized static void updateChunkRepl(Chunk chunk,int update,int count){
		
		File metaData = new File("../metadata"+Peer.getPeerId()+"/ChunkList.csv");
		Scanner scanner;
		
		List<String> metaArray=new ArrayList<String>();
		
		Boolean iHaveChunk = false;
		
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(Integer.parseInt(divided[1])==chunk.getChunkNumber() && divided[0].equals(chunk.getFileId())){
					iHaveChunk=true;
					if(update==1)
						metaArray.add(divided[0]+Constants.COMMA_DELIMITER+divided[1]+Constants.COMMA_DELIMITER+divided[2]+Constants.COMMA_DELIMITER+(Integer.parseInt(divided[3])+1)+Constants.COMMA_DELIMITER);
					else if(update==0)
						metaArray.add(divided[0]+Constants.COMMA_DELIMITER+divided[1]+Constants.COMMA_DELIMITER+divided[2]+Constants.COMMA_DELIMITER+1+Constants.COMMA_DELIMITER);
					else if(update==2){
						metaArray.add(divided[0]+Constants.COMMA_DELIMITER+divided[1]+Constants.COMMA_DELIMITER+divided[2]+Constants.COMMA_DELIMITER+count+Constants.COMMA_DELIMITER);
					}
				}else metaArray.add(str);
	        }
			
			if(iHaveChunk){
				FileWriter fileWriter = new FileWriter("../metadata"+Peer.getPeerId()+"/ChunkList.csv", false);
				for(String str: metaArray){
					fileWriter.append(str);
					fileWriter.append(Constants.NEW_LINE_SEPARATOR);
				}
				
				fileWriter.close();
			}else{
				FileWriter fileWriter = new FileWriter("../metadata"+Peer.getPeerId()+"/ChunkList.csv", true);
				
				fileWriter.append(chunk.getFileId());
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(Integer.toString(chunk.getChunkNumber()));
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(Integer.toString(chunk.getReplication()));
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(String.valueOf(count));
				fileWriter.append(Constants.COMMA_DELIMITER);
				fileWriter.append(Constants.NEW_LINE_SEPARATOR);
				
				fileWriter.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public synchronized static List<Chunk> getBadChunks(){
		File metaData = new File("../metadata"+Peer.getPeerId()+"/ChunkList.csv");
		Scanner scanner;
		List<Chunk> badchunks= new ArrayList<Chunk>();
		try {
			scanner = new Scanner(metaData);
			scanner.useDelimiter(Constants.NEW_LINE_SEPARATOR);
			while(scanner.hasNext()){
				String str=scanner.next();
				String[] divided = str.split(Constants.COMMA_DELIMITER);
				if(Integer.parseInt(divided[3])<Integer.parseInt(divided[2])){
					badchunks.add(new Chunk(divided[0],Integer.parseInt(divided[1]),HandleFiles.readFile("../Chunks"+Peer.getPeerId()+"/"+divided[0]+"."+divided[1]),Integer.parseInt(divided[2])));
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return badchunks;
	}
}
