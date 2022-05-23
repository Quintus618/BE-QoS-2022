import java.io.*;
import java.net.*;
import java.net.InetAddress;

 
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
 
 //TODO finalement maintenir la conversation ouverte en permanence?
            String text;
            Boolean answered = false;
 
            do {
                //texte envoyé par le client lu par le serveur
                text = reader.readLine();
                String[] tab = text.split(",");
                //TODO TODO
                //TODO exclusion mutuelle des threads
                //texte renvoyé au client
                //InetAddress source, InetAddress receiver, int portSource, int portRecei, double demand
                if (tab.length==5){
                	InetAddress source = InetAddress.getByName(tab[0]);
                	InetAddress receiver = InetAddress.getByName(tab[1]);
                	int portsrc = Integer.valueOf(tab[2]);
                	int portrcv = Integer.valueOf(tab[3]);//pas d'utilité à toujours garder ces deux ports qui vaudront toujours 5060, sauf pour détecter un éventuel détournement
                	Double demand=Double.parseDouble(tab[4]);
                    boolean test_rs = BB.checkRessourceUtilization(demand);
                    String src_dest = "-"+tab[0]+"-"+tab[1];
                    int connexstate = BB.addRessourcesConnexions(source, receiver, portsrc, portrcv, demand);
                    
                    if (0%2==connexstate && test_rs){
                        writer.println("OK"+src_dest);
                        BB.addConnexion(new Connexion(source, receiver, portsrc, portrcv, demand, BB.GetId_branch()), demand);
                    }else if (1==connexstate){
                        writer.println("NOK"+src_dest);
                    }
                    //answered = true;
                }else if (tab.length==2){//ip et port
                    if (BB.RemoveRessourcesConnexions(InetAddress.getByName(tab[0]), Integer.valueOf(tab[1]))){
                        writer.println("OK-"+tab[0]+"-closure");
                    }else{
                        writer.println("NOK-"+tab[0]+"-closure");
                    }
                    answered = true;
                }
                else{
                    System.out.println("WARNING : message reçu du proxy SIP de format invalide");
                }
 
            } while (!answered);//TODO modifier
 
            ThreadTCP.currentThread().interrupt();

        } catch (Exception ex) {
            System.out.println("Exception (thread serveur): " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}