package peer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import listeners.Multicast;
import listeners.MulticastBackup;
import listeners.MulticastRestore;
import protocols.ChunkBackup;

public class Peer extends UnicastRemoteObject  implements IPeer {

	private static int protocolVersion;
	private static int peerId;
	private static String remoteObject;
	
	private static InetAddress mcAddress, mdbAddress, mdrAddress;
	private static int mcPort;
	private static int mdbPort;
	private static int mdrPort;
	
	private static MulticastSocket mc;
	private static MulticastSocket mdb;
	private static MulticastSocket mdr;
	
	protected Peer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws UnknownHostException, RemoteException, MalformedURLException, AlreadyBoundException{
		
		if(!validateArgs(args)){
			return;
		}
		
		Peer peer= new Peer();
		
		Registry registry = LocateRegistry.getRegistry();
		
		IPeer iserver= (IPeer) peer;
		
		registry.rebind(remoteObject, iserver);		
		
		joinGroups();
		
		System.out.println("Server ready");
		
	}
	
	private static boolean validateArgs(String[] args) throws UnknownHostException{
		
		if(args.length!=9){
			System.out.println("Usage: Peer <protocol version> <peerID> <service access point> <mc address> <mc port> <mdb address> <mdb port> <mdr address> <mdr port> ");
			return false;
		}
		
		protocolVersion=Integer.parseInt(args[0]);
		setPeerId(Integer.parseInt(args[1]));
		remoteObject=args[2];
		
		mcAddress=InetAddress.getByName(args[3]);
		mcPort=Integer.parseInt(args[4]);
		mdbAddress=InetAddress.getByName(args[5]);
		mdbPort=Integer.parseInt(args[6]);
		mdrAddress=InetAddress.getByName(args[7]);
		mdrPort=Integer.parseInt(args[8]);
		
		return true;
		
	}
	
	private static void joinGroups(){
		
		try {
			
			mc = new MulticastSocket(mcPort);
			mc.joinGroup(mcAddress);
			Runnable mc1=new Multicast(mc);
			new Thread(mc1).start();
			
			mdb = new MulticastSocket(mdbPort);
			mdb.joinGroup(mdbAddress);
			Runnable mc2=new MulticastBackup(mdb);
			new Thread(mc2).start();
			
			mdr = new MulticastSocket(mdrPort);
			mdr.joinGroup(mdrAddress);
			Runnable mc3=new MulticastRestore(mdr);
			new Thread(mc3).start();
			
			System.out.println("Added to all multicast groups");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void backup(File file, int replDeg) throws RemoteException {
		Runnable run=new ChunkBackup(file,replDeg);
		new Thread(run).start();
	}

	@Override
	public void restore(File file) throws RemoteException{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(File file) throws RemoteException{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reclaim(int space) throws RemoteException{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void state() throws RemoteException{
		// TODO Auto-generated method stub
		
	}

	public static int getPeerId() {
		return peerId;
	}

	private static void setPeerId(int peerId) {
		Peer.peerId = peerId;
	}
	
	public static InetAddress getMdbAddress(){
		return mdbAddress;
	}
	
	public static int getMdbPort(){
		return mdbPort;
	}
}
