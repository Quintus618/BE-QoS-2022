package openjsip.proxy;

import java.io.*;
import java.net.*;

public class test_rcv extends Thread{

	private int port;
	private byte [] addr = new byte[4];
	private Socket socket;
	private InputStream input;
	private BufferedReader br;
	private volatile String resp = null;
	
	public test_rcv() {
		this.port = 12122;
		this.addr[0] = (byte) 193;
		this.addr[1] = (byte) 168;
		this.addr[2] = (byte) 1;
		this.addr[3] = (byte) 1;
		try {
			InetAddress dest = InetAddress.getByAddress(this.addr);
			try {
				this.socket = new Socket(dest, this.port);
				this.input = this.socket.getInputStream();
				this.br = new BufferedReader(new InputStreamReader(this.input));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException h) {
			h.printStackTrace();
		}
	}
	
	public void run() {
		while (true) {
			try {
				this.resp = this.br.readLine();
				System.out.println("reçu reçu");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
