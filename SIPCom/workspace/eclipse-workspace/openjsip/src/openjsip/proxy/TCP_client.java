package openjsip.proxy;

import java.io.*;
import java.net.*;

public class TCP_client extends Thread {
	
	private int port;
	private String dest;
	private byte [] addr = new byte[4];
	private Socket socket;
	private OutputStream output;
	private PrintWriter pw;
	private InputStream input;
	private BufferedReader br;
	private String msg = null;
	private volatile String response = null;
	
	public TCP_client(String msg) {
		this.msg = msg;
		this.port = 12121;
		this.addr[0] = (byte) 193;
		this.addr[1] = (byte) 168;
		this.addr[2] = (byte) 1;
		this.addr[3] = (byte) 1;
		try {
			InetAddress dest = InetAddress.getByAddress(this.addr);
			try {
				this.socket = new Socket(dest, this.port);
				this.output = this.socket.getOutputStream();
				this.pw = new PrintWriter(this.output, true);
				this.input = this.socket.getInputStream();
				this.br = new BufferedReader(new InputStreamReader(this.input));
				this.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException h) {
			h.printStackTrace();
		}
	}
	
	public String get_resp() {
		return this.response;
	}
	
	public void run() {
		this.pw.println(this.msg);
		try {
			this.response = this.br.readLine();
			try {
				sleep(2000);
			} catch (InterruptedException i) {
				i.printStackTrace();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
