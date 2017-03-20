package listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Multicast implements Runnable {

	private MulticastSocket socket;
	private int mcPort;
	private InetAddress mcAddress;
	
	public Multicast(MulticastSocket mc){
		setSocket(mc);
	}

	@Override
	public void run() {
		
		while(true){
			byte[] buffer = new byte[65000];
			
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				socket.receive(packet);
				String received= new String(buffer,0,buffer.length);
				System.out.println(received);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	public MulticastSocket getSocket() {
		return socket;
	}

	private void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

}
