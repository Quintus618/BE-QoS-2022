package openjsip.proxy;

import java.io.*;
import java.net.*;

public class TCP_client {
	
	private int port;
	private byte [] addr = new byte[4];
	private Socket socket;
	private OutputStream output;
	private PrintWriter pw;
	private InputStream input;
	private BufferedReader br;
	private volatile String resp = null;
	private TCP_send sender;
	
	public TCP_client() {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException h) {
			h.printStackTrace();
		}
	}
	
	public String send(String msg) {
		this.pw.println(msg);
		try {
			this.resp = this.br.readLine();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return this.resp;
	}
}
