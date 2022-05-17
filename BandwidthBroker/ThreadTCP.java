import java.io.*;
import java.net.*;
import java.net.InetAddress;

import bandwidthbroker.java;
 
public class ThreadTCP extends Thread {
    private Socket socket;
    private bandwidthbroker BB;
 
    public ThreadTCP(Socket socket, bandwidthbroker BB) {
        this.socket = socket;
        this.BB=BB;
    }
 
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
 
 
            String text;
            Boolean answered = false;
 
            do {
                //texte envoyé par le client lu par le serveur
                text = reader.readLine();
                String[] tab = text.split("-");
                //TODO TODO
                //TODO exclusion mutuelle des threads
                //texte renvoyé au client
                //InetAddress source, InetAddress receiver, int portSource, int portRecei, double demand, boolean closeConnection
                if (tab.length==6){
                    if (BB.updateRessourcesConnexions(InetAddress.getByName(tab[0]), InetAddress.getByName(tab[1]), Integer.valueOf(tab[2]), Integer.valueOf(tab[3]), Double.valueOf(tab[4]), Boolean.valueOf(tab[5]))){
                        writer.println("OK");
                    }else{
                        writer.println("NOK");
                    }
                    answered = True;
                }else if (tab.length!=0){
                    System.out.println("WARNING : message reçu du proxy SIP de format invalide");
                }
 
            } while (!answered);//TODO modifier
 
            socket.close();
            Thread.currentThread.interrupt();

        } catch (Exception ex) {
            System.out.println("Exception (thread serveur): " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}