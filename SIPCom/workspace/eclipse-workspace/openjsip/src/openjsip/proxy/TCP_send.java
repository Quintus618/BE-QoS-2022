package openjsip.proxy;

import java.io.*;
import java.net.*;

public class TCP_send extends Thread {

	private PrintWriter pw;
	private BufferedReader br;
	private String msg = null;
	private String resp = null;
	
	public TCP_send(PrintWriter pw, BufferedReader br, String msg) {
		this.pw = pw;
		this.br = br;
		this.msg = msg;
	}
	
	public String get_resp() {
		return this.resp;
	}
	
	public void run() {
		this.pw.println(this.msg);
		try {
			this.resp = this.br.readLine();
//			try {
//				sleep(2000);
//			} catch (InterruptedException i) {
//				i.printStackTrace();
//			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
