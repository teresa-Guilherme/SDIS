import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
	
	private static String name,request,plate,host;
	private static int port;
	
	public static void main(String[] args) throws IOException{
		if(args.length < 4 || args.length > 5){
			System.out.println("Incorrect number of arguments");
			return;
		}
		
		String message="";
		
		host = args[0];
		port = Integer.parseInt(args[1]);
		request = args[2].toUpperCase();
		plate = args[3];

		if(request.equals(RequestType.REGISTER.toString())){
			name = args[4];
			message = request + " " + plate + " " + name;
		}
		else if(request.equals(RequestType.LOOKUP.toString())){
			message = request + " " + plate;
		}
		
		System.out.println("Request: " + message);
		
		// send request
		DatagramSocket socket = new DatagramSocket();
		byte[]sbuf = message.getBytes();		
		InetAddress address = InetAddress.getByName(host);
		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);
		socket.send(packet);
		
		System.out.println("Request sent!");
		
		// get response
		byte[] rbuf = new byte[300];
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		// display response
		String received = new String(packet.getData());
		System.out.println("Request answer: " + received);
		socket.close();
	}

}
