package listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLSocket;

import fileManager.Chunk;
import fileManager.CsvHandler;
import fileManager.HandleFiles;
import peer.Peer;
import protocols.Constants;
import protocols.Election;
import protocols.EnterSystem;
import protocols.Message;
import protocols.SpaceReclaiming;
import user.User;

public class SSL_Handler implements Runnable {

	private SSLSocket socket;
	private PrintWriter out=null;
	private BufferedReader in=null;
	private boolean awaitedAnswer=false;
	private String answer="";
	private final Semaphore sem = new Semaphore(0, true);
	
	public SSL_Handler(SSLSocket socket) {
		
		this.socket = socket;
	}


	@Override
	public void run() {
		
		String received="";
		
		try {
		
			out = new PrintWriter(socket.getOutputStream(), true);
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
			
			while(true){
				received = in.readLine();
				
				System.out.println("vou ler input" + received);
				String[] divided = received.split(" ");
				if(awaitedAnswer){
					System.out.println("resposta em espera " +received );
					
					if(divided[0].equals(Constants.COMMAND_RESTORE)){
						int counter=0;
						char[] buffer = new char[64000];
						char[] result = new char[64000];
						while((counter+=in.read(buffer))!=-1){
							result=Message.concatBytes(Handler.trim(result),Handler.trim(buffer));
							System.out.println(counter + "counter");
							if(counter-2==Integer.parseInt(divided[1]))
								break;
							
							buffer = new char[64000];
						}
						result=Handler.trim(result);
						
						result=Arrays.copyOfRange(result, 0, result.length-2);
						System.out.println("sai do semaforo");
						answer= new String(result);
					}else answer=received;
					
					sem.release();
					continue;
				}
				
				
				
				//TODO verificar autenticacao do peer
				
				String type = divided[0];
				
				String id = divided[1];
				
				if(divided[1].equals("oi")){
					Peer.getPeers().put(divided[0], this);
					System.out.println("tenho x peers " + Peer.getPeers().size());
					continue;
				}
				
				String pass = divided[2];
				
				String protocol = divided[3];
				
				String filename ="";
				
				String repDegree = "";
				
				String spaceReclaim = "";
				
				String username = "";
				
				String newPassword = "";
				
				String level = "";
				
				String chunkSize = "";
				
				String chunkNumber = "";
				
				String originalPeer = "";
				
				String realName = "";
				
				String answer = "ok";
				
				byte[] bytesBody;
				
				in.readLine();
				
				switch(protocol){
				
				case Constants.COMMAND_BACKUP:
					
					chunkNumber = divided[5];
					filename = divided[4];
					chunkSize = divided[7];
					
					char[] buffer = new char[64000];
					char[] result = new char[64000];
					
					int counter=0;
					
					if(type.equals("1")){
						
						repDegree = divided[6];	
						realName=divided[8];
						while((counter+=in.read(buffer))!=-1){
							result=Message.concatBytes(Handler.trim(result),Handler.trim(buffer));
							if(counter-2==Integer.parseInt(chunkSize))
								break;
							
							buffer = new char[64000];
						}			
						result=Handler.trim(result);
						
						result=Arrays.copyOfRange(result, 0, result.length-2);
						
						
						answer="";
						Chunk chunk = new Chunk(filename,Integer.parseInt(chunkNumber),new String(result).getBytes(),Integer.parseInt(repDegree));
						CsvHandler.addMasterMeta(chunk, id, realName);
						HashMap<String,Runnable> copy = new HashMap<String,Runnable>(Peer.getPeers());
						Iterator<Entry<String,Runnable>> it = copy.entrySet().iterator();
						
						int replication = Integer.parseInt(repDegree);
						int counterRepl=0;
						answer="PEERBACKUP ";
						while(it.hasNext()) {
							Map.Entry<String,Runnable> pair = (Map.Entry<String,Runnable>)it.next();
							String idThread = pair.getKey();
							Runnable thread = pair.getValue();
							if(thread==this){
								Message message = new Message(chunk,id,realName);
								out.println(new String(message.backupMasterSSL()));
								if(in.readLine().equals("ok")){
									System.out.println("recebo");
									answer+=idThread+" ";
									CsvHandler.addMasterMeta(chunk, idThread, realName);
									counterRepl++;
								}
							}
							else if(!idThread.equals(Peer.getPeerId()+"")){
								Message message = new Message(chunk,id,realName);
								if(((SSL_Handler) thread).sendMessage(message.backupMasterSSL()).equals("ok")){
									answer+=idThread+" ";
									CsvHandler.addMasterMeta(chunk, idThread, realName);
									counterRepl++;
								}
							}else{
								if(Peer.iHaveSpace(SpaceReclaiming.directorySize()+chunk.getChunkData().length)){
									HandleFiles.writeFile("../Chunks"+Peer.getPeerId()+"/"+filename+"."+chunkNumber,(new String(result).getBytes("ISO-8859-1")));
									CsvHandler.addChunkMeta(chunk, id, realName);
									CsvHandler.addMasterMeta(chunk, Peer.getPeerId()+"", realName);
									counterRepl++;
									answer+=Peer.getPeerId()+" ";
								}
							}
							
							if(replication==counterRepl)
								break;
							
							it.remove();
						}
					}					
					break;
					
				case "DELETE":
					
					if(type.equals("1")){
						realName = divided[4];
						filename = CsvHandler.getHash(realName);
						
						HandleFiles.eraseFile("../Chunks"+Peer.getPeerId()+"/",filename);
						
						HashMap<String,Runnable> copy = new HashMap<String,Runnable>(Peer.getPeers());
						Iterator<Entry<String,Runnable>> it = copy.entrySet().iterator();
						List<String> peersContained = CsvHandler.getPeersChunk(filename);
						while(it.hasNext()) {
							Map.Entry<String,Runnable> pair = (Map.Entry<String,Runnable>)it.next();
							String idThread = pair.getKey();
							Runnable thread = pair.getValue();
							Chunk chunk = new Chunk(filename,0,null,0);
												
							if(!idThread.equals(Peer.getPeerId()+"") && peersContained.contains(idThread)){
								System.out.println("vou mandar po peer " + idThread);
								Message message = new Message(chunk,"");
								((SSL_Handler) thread).sendMessageNoRspns(message.deleteMasterSSL());
								CsvHandler.deleteChunks(filename, "../metadata"+Peer.getPeerId()+"/AllChunks.csv");
							}else if(idThread.equals(Peer.getPeerId()+"") && peersContained.contains(idThread)){
								CsvHandler.deleteChunks(filename, "../metadata"+Peer.getPeerId()+"/MyChunks.csv");
								CsvHandler.deleteChunks(filename, "../metadata"+Peer.getPeerId()+"/ChunkList.csv");
							}
							
							it.remove();
						}
					}
					
					break;
				case Constants.COMMAND_REMOVED:{
					
					answer = "ok";
					
					char[] chunkData = new char[64000];
					char[] chunkDataTotal = new char[64000];
					
					filename=divided[4];
					chunkSize = divided[6];
					chunkNumber = divided[5];
					counter=0;
					if(type.equals("1")){
						while((counter+=in.read(chunkData))!=-1){
							chunkDataTotal=Message.concatBytes(Handler.trim(chunkDataTotal),Handler.trim(chunkData));
							chunkDataTotal=Handler.trim(chunkDataTotal);
							if(counter-2==Integer.parseInt(chunkSize))
								break;
							
							chunkData = new char[64000];
						}	
					}
					chunkDataTotal=Handler.trim(chunkDataTotal);
					chunkDataTotal=Arrays.copyOfRange(chunkDataTotal, 0, chunkDataTotal.length-2);

					HashMap<String,Runnable> copy = new HashMap<String,Runnable>(Peer.getPeers());
					Iterator<Entry<String,Runnable>> it = copy.entrySet().iterator();
					String newPeerId="";
					Chunk chunk = new Chunk(filename,Integer.parseInt(chunkNumber),new String(chunkDataTotal).getBytes(),0);
					while(it.hasNext()) {
						Map.Entry<String,Runnable> pair = (Map.Entry<String,Runnable>)it.next();
						String idThread = pair.getKey();
						Runnable thread = pair.getValue();
						
						if(!idThread.equals(id) && !idThread.equals(Peer.getPeerId()+"")){
							Message message = new Message(chunk,id,CsvHandler.getRealName(chunk.getFileId()));
							if(((SSL_Handler) thread).sendMessage(message.backupMasterSSL()).equals("ok")){
								CsvHandler.replacePeer(chunk,id,idThread);
								newPeerId=idThread;
								break;
							}
						}else if(idThread.equals(Peer.getPeerId()+"") && !HandleFiles.fileExists("../Chunks"+Peer.getPeerId()+"/"+filename+"."+chunkNumber)){
							HandleFiles.writeFile("../Chunks"+Peer.getPeerId()+"/"+filename+"."+chunkNumber, new String(chunkDataTotal).getBytes());
							CsvHandler.addChunkMeta(chunk, id, CsvHandler.getRealName(chunk.getFileId()));
							CsvHandler.replacePeer(chunk,id,Peer.getPeerId()+"");
							newPeerId=Peer.getPeerId()+"";
							break;
						}
						
						it.remove();
					}
					copy = new HashMap<String,Runnable>(Peer.getPeers());
					it = copy.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry<String,Runnable> pair = (Map.Entry<String,Runnable>)it.next();
						String idThread = pair.getKey();
						Runnable thread = pair.getValue();
						if(!idThread.equals(Peer.getPeerId()+"") &&idThread.equals(CsvHandler.getInitiatorPeer(chunk.getFileId(), chunk.getChunkNumber()).split(Constants.COMMA_DELIMITER)[0])){
							Message message = new Message(chunk,id,newPeerId);
							if(thread!=null){
							((SSL_Handler) thread).sendMessageNoRspns(message.createReplace());
							break;}
						}
						
						it.remove();
					}
					
					
				break;
				}
					
				case Constants.COMMAND_GETMYCHUNKS:
						List<String> list = CsvHandler.getChunksByPeers(id);
						answer= Constants.COMMAND_GETMYCHUNKS + " " + list.size() + " "+ Constants.CRLF + Constants.CRLF;
						for(int i =0;i<list.size();i++){
							answer+= " " + list.get(i).substring(0, list.get(i).length()-1);
						}
						break;
					
				case "RESTORE":
					
					if(type.equals("1")){
						realName = divided[4];
						filename = CsvHandler.getHash(realName);
						int chunkNr = 0;
						String file="";

						while(true){
							HashMap<String,Runnable> copy = new HashMap<String,Runnable>(Peer.getPeers());
							Iterator<Entry<String,Runnable>> it = copy.entrySet().iterator();
							List<String> peers = CsvHandler.getPeersChunk(filename,chunkNr);
							if(peers.size()==0)
								break;
							while(it.hasNext()) {
								Map.Entry<String,Runnable> pair = (Map.Entry<String,Runnable>)it.next();
								String idThread = pair.getKey();
								Runnable thread = pair.getValue();
								Chunk chunk = new Chunk(filename,chunkNr,null,0);
													
								if(!idThread.equals(Peer.getPeerId()+"") && peers.contains(idThread)){
									
									Message message = new Message(chunk,"");
									String chunkReceived = ((SSL_Handler) thread).sendMessage(message.restoreMasterSSL());
									
									file+=chunkReceived;
									break;
								}
								
								it.remove();
							}
							chunkNr++;
						}
						out.println("RESTOREANSWER");
						out.println(file);
						out.println("ok");
					}
					in.readLine();
					
					
					break;
					
				case Constants.COMMAND_CREATEUSER:
					
					username = divided[4];
					newPassword = divided[5];
					level = divided[6];
					
					HashMap<String,Runnable> copy = new HashMap<String,Runnable>(Peer.getPeers());
					Iterator<Entry<String,Runnable>> it = copy.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry<String,Runnable> pair = (Map.Entry<String,Runnable>)it.next();
						String idThread = pair.getKey();
						Runnable thread = pair.getValue();
						
						if(!idThread.equals(id) && !idThread.equals(Peer.getPeerId()+"")){
							Message message = new Message(new User(username,newPassword,level));
							((SSL_Handler) thread).sendMessageNoRspns(message.createUser());
						}else if(idThread.equals(Peer.getPeerId()+"")){
							CsvHandler.createUser(new User(username,newPassword,level));
						}
						
						it.remove();
					}
					
					break;
				
				case  Constants.COMMAND_GETOTHERCHUNKS: 
										
					if(type.equals("1")){
						
						String numberOfNames = divided[4];
						
						char[] bufferNames = new char[64000];
						char[] resultNames = new char[64000];
						
						while((in.read(bufferNames))!=-1){
							resultNames=Message.concatBytes(Handler.trim(resultNames),Handler.trim(bufferNames));
							resultNames=Handler.trim(resultNames);
							resultNames=Arrays.copyOfRange(resultNames, 0, resultNames.length-2);
							
							if(new String(resultNames).split(" ").length==Integer.parseInt(numberOfNames)+1)
								break;
							
							bufferNames = new char[64000];
						}			
						
						
						
						if(resultNames.length>0)
							resultNames=Arrays.copyOfRange(resultNames, 1, resultNames.length);
						
						String[] dividedNames = new String(resultNames).split(" ");
						
						String peer;
						String listOfPeers="";
						
						for(int i=0;i<dividedNames.length;i++){
							String[] dividedName = dividedNames[i].split("\\.");
							if(dividedName.length==2 && (peer=CsvHandler.getInitiatorPeer(dividedName[0],Integer.parseInt(dividedName[1])))!=null){
								listOfPeers+= dividedNames[i]+";"+peer + " ";
							}
						}
						
						answer=Constants.COMMAND_GETOTHERCHUNKS + " " + listOfPeers.split(" ").length + " ";
						answer+= Constants.CRLF + Constants.CRLF;
						answer+=listOfPeers;
						
					}
					
					break;
				
				case  Constants.COMMAND_EVERYTHING:{
					int numberList = Integer.parseInt(divided[4]);
					int numberMyChunks = Integer.parseInt(divided[5]);
					
					if(numberList + numberMyChunks>0){
						char[] bufferNames = new char[64000];
						char[] resultNames = new char[64000];
						while((in.read(bufferNames))!=-1){
							resultNames=Message.concatBytes(Handler.trim(resultNames),Handler.trim(bufferNames));
							resultNames=Handler.trim(resultNames);
							resultNames=Arrays.copyOfRange(resultNames, 0, resultNames.length-2);
							if(new String(resultNames).split(" ").length==numberList + numberMyChunks+1)
								break;
							
							bufferNames = new char[64000];
						}
						
						if(resultNames.length>0)
							resultNames=Arrays.copyOfRange(resultNames, 1, resultNames.length);
						
						String[] dividedNames = new String(resultNames).split(" ");
						
						for(int i=0;i<dividedNames.length;i++){
							String[] dividedName = dividedNames[i].split(";");
							if(i+1<=numberList){
								if(CsvHandler.getInitiatorPeer(dividedName[0],Integer.parseInt(dividedName[1]))==null)
									CsvHandler.addMasterMeta(new Chunk(dividedName[0],Integer.parseInt(dividedName[1]),null,0),dividedName[3] , dividedName[2]);
								CsvHandler.addMasterMeta(new Chunk(dividedName[0],Integer.parseInt(dividedName[1]),null,0),id, dividedName[2]);
							}
							else{
								if(CsvHandler.getInitiatorPeer(dividedName[0],Integer.parseInt(dividedName[1]))==null){
									CsvHandler.addMasterMeta(new Chunk(dividedName[0],Integer.parseInt(dividedName[1]),null,0), id, dividedName[2]);
								}
								for(int j=3;j<dividedName.length;j++){
									CsvHandler.addMasterMeta(new Chunk(dividedName[0],Integer.parseInt(dividedName[1]),null,0), dividedName[j], dividedName[2]);
								}
							}
						}
					}else in.readLine();
				}
					break;
				
				case  Constants.COMMAND_NAMES: 
					
					String numberOfNames = divided[4];
					
					char[] bufferNames = new char[64000];
					char[] resultNames = new char[64000];
					
					if(type.equals("1")){
						while((in.read(bufferNames))!=-1){
							resultNames=Message.concatBytes(Handler.trim(resultNames),Handler.trim(bufferNames));
							resultNames=Handler.trim(resultNames);
							resultNames=Arrays.copyOfRange(resultNames, 0, resultNames.length-2);
							
							if(new String(resultNames).split(" ").length==Integer.parseInt(numberOfNames)+1)
								break;
							
							bufferNames = new char[64000];
						}			
						
						
						if(resultNames.length>0)
							resultNames=Arrays.copyOfRange(resultNames, 1, resultNames.length);
						
						String[] dividedNames = new String(resultNames).split(" ");
						
						for(int i=0;i<dividedNames.length;i++){
							String[] dividedName = dividedNames[i].split(";");
							if(dividedName.length==3 && !CsvHandler.checkUser(dividedName[0])){
								User user = new User(dividedName[0],dividedName[1],dividedName[2]);
								CsvHandler.createUser(user);
							}
						}
						
						List<String> names = CsvHandler.getUsers();
						answer=Constants.COMMAND_NAMES + " " + names.size() + " ";
						answer+= Constants.CRLF + Constants.CRLF;
						for(int i=0;i<names.size();i++){
							if(i+1!=names.size())
								answer+=names.get(i) + " ";
							else answer+=names.get(i);
						}
						
					}
					
					break;
				}
				
				
				out.println(answer);
			}
			
			
		} catch (IOException e) {
			System.out.println("foi abaixo a socket server");
			
			e.printStackTrace();
		}

		System.out.println("vou fechar");
		out.close();
		try {
			socket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	private void sendMessageNoRspns(byte[] message){
		
		out.println(new String(message));
	}
	
	private String sendMessage(byte[] message){
		awaitedAnswer=true;
		System.out.println("vou mandar msg ");
		try {
			out.println(new String(message,"ISO-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		awaitedAnswer=false;
		return answer;
	}
}
