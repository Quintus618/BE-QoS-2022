import java.io.*;
import java.net.*;
 
public class ThreadTCP extends Thread {
    private Socket socket;
 
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
 
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
 
 
            String text;
 
            do {
                //texte envoyé par le client lu par le serveur
                text = reader.readLine();

                //TODO

                //texte renvoyé au client
                writer.println("trucs");
 
            } while (!text.equals("END"));//TODO modifier
 
            socket.close();
        } catch (IOException ex) {
            System.out.println("Exception (thread serveur): " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}