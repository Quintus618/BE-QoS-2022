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
        	boolean first_time=true;
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
 
 //TODO finalement maintenir la conversation ouverte en permanence ou pas?
            String text;
 
            do {
                //texte envoyé par le client lu par le serveur
            	System.out.println("BEFORE readline");
                text = reader.readLine();
                if (text==null) {
                	if (first_time) {
	                	System.out.println("Fin de la connexion avec un client TCP (probablement un proxy SIP)");
	                    ThreadTCP.currentThread().interrupt();
	                    ThreadTCP.currentThread().stop();
	                    first_time=false;
                    }
                }else {
	                String[] tab = text.split("-");
	            	System.out.println(text);
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
	                    int connexstate = BB.addRessourcesConnexions(source, receiver, portsrc, portrcv, demand); //Manipulation d'une connexion déjà existante
	                    
	                    if (0==connexstate && test_rs){
	                    	BB.addConnexion(new Connexion(source, receiver, portsrc, portrcv, demand, BB.GetId_branch()), demand);
	                    	BB.printConnexionTable();
	                        writer.println("OK"+src_dest);
	                        System.out.println("---OK envoyé---");
	                    }else if (1==connexstate || !test_rs){
	                    //if (true) {//à des fins de test uniquement TODO 
	                        writer.println("NOK"+src_dest);
	                        System.out.println("Une communication a été refusée.");
	                    }else if (2==connexstate) {
	                    	System.out.println("Warning - Une communication déjà en cours a été redemandée.");
	                        writer.println("OK"+src_dest);
	                        System.out.println("-----(OK re-envoyé)");
	                    }else {
	                    	System.out.println("WARNING --- APPARITION D'UNE ERREUR EN THEORIE IMPOSSIBLE");
	                    }
	                }else if (tab.length==2){//ip et port
	                    if (BB.RemoveRessourcesConnexions(InetAddress.getByName(tab[0]), 5060)){
	                    	BB.printConnexionTable();
	                        writer.println("OK-"+tab[0]+"-closure");
	                    }else{
	                        writer.println("NOK-"+tab[0]+"-closure");
	                    }
	                }
	                else{
	                    System.out.println("WARNING : message reçu du proxy SIP de format invalide");
	                }
                }
            } while (true);
 

        } catch (Exception ex) {
            System.out.println("Exception (thread serveur): " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}