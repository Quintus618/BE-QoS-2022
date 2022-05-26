import java.lang.management.ThreadInfo;
import java.net.*;
import java.util.*;
import com.jcraft.jsch.*;
import java.io.*;



public class bandwidthbroker{

    // Premier élément SLA_A->Réseau de coeur
    // Second élément Réseaux de coeur -> SLA-B
    private double[] SLA = {1,2};

    // Utilisation des ressources EF des deux SLAs
    private double[] SLA_u = {0,0};

    // Tableau récapitulatif des connexions établies
    private ArrayList<Connexion> t_connexion = new ArrayList<Connexion>();
    private int listening_port = 12121;//note: le proxy SIP utilise le même
    private String proxy_SIP_IP = "193.168.1.2";
    
    private String CE_A = "193.168.1.254";
    private String CE_B = "193.168.2.254";
    private String portSIP="5060";//attention tout ça est statique et ne supporte pas de modifications de la topographie du réseau
    private String eth_used = "eth1";//en cas de changement penser à netoyer les qdiscs sur l'ancienne
    private int id_branch;

    private static String BB_IP = "193.168.1.1";


    public bandwidthbroker(){
    	
        //initialisation des tc avec les SLAs
        String TC_A_init = "tc qdisc del dev "+eth_used+" root;tc qdisc add dev "+eth_used+" root handle 1: htb default 2;tc class add dev "+eth_used+" parent 1: classid 1:1 htb rate 1000kbit ceil 1100kbit;tc class add dev "+eth_used+" parent 1: classid 1:2 htb rate 2000kbit ceil 2100kbit;tc filter add dev "+eth_used+" parent 1: protocol ip prio 1 fw flowid 1:2";
        //1: voix, 2:parasite ->handle 2 va en 1:2 parasite
        String TC_B_init = "tc qdisc del dev "+eth_used+" root;tc qdisc add dev "+eth_used+" root handle 1: htb default 2;tc class add dev "+eth_used+" parent 1: classid 1:1 htb rate 2000kbit ceil 2100kbit;tc class add dev "+eth_used+" parent 1: classid 1:2 htb rate 3000kbit ceil 3100kbit;tc filter add dev "+eth_used+" parent 1: protocol ip prio 1 fw flowid 1:2";

        
        init_iptables();

        try {
			askSSH(this.CE_A, TC_A_init);
	        askSSH(this.CE_B, TC_B_init);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erreur à l'initialisation des qdiscs via ssh.");
		}
        //pour plus de clients possible de faire un for avec un dictionnaire associant host et commandes

        this.id_branch = 5;
        this.listenTCP();

    }

    
    private void init_iptables() {

    	//initialisation des iptables mangle pour MPLS
    	/*String mangle_check_F = "iptables -t mangle -C FORWARD -p udp --dport "+portSIP+" -j DSCP --set-dscp-class EF;echo $?";
    	String mangle_check_O = "iptables -t mangle -A OUTPUT -p udp --dport "+portSIP+" -j DSCP --set-dscp-class EF;echo $?";
    	String mangle_init_F = "iptables -t mangle -A FORWARD -p udp --dport "+portSIP+" -j DSCP --set-dscp-class EF;";
    	String mangle_init_O = "iptables -t mangle -A OUTPUT -p udp --dport "+portSIP+" -j DSCP --set-dscp-class EF;";
    	*/
    	String mangle_init_EF = "iptables -t mangle -A FORWARD -p udp --dport "+portSIP+" -j DSCP --set-dscp-class EF;iptables -t mangle -A OUTPUT -p udp --dport "+portSIP+" -j DSCP --set-dscp-class EF;";
    	String mangle_proxySIP = "iptables -t mangle -A FORWARD -p udp -d "+proxy_SIP_IP+" -j DSCP --set-dscp-class CS4;iptables -t mangle -A OUTPUT -p udp -d "+proxy_SIP_IP+" -j DSCP --set-dscp-class CS4;iptables -t mangle -A FORWARD -p udp -s "+proxy_SIP_IP+" -j DSCP --set-dscp-class CS4;iptables -t mangle -A OUTPUT -p udp -s "+proxy_SIP_IP+" -j DSCP --set-dscp-class CS4;";
    	//peu importe si on utilise sport ou dport, ce sont les mêmes
    	//pas obligatoire de préciser les ports pour les mangle_proxySIP
        try {/*
            //ici un début de strings pour vérification de la non-existence des règles dans la table mangle pour éviter une redondance
 			askSSH(this.CE_A, mangle_init_F);
 	        askSSH(this.CE_B, mangle_init_O);
 			askSSH(this.CE_A, mangle_init_F);
 	        askSSH(this.CE_B, mangle_init_O);*/
        	//!!!!!!!!!!!!!CRUCIAL de passer d'abord les règles proxySIP pour qu'elles soient prioritaires!!!
 			//askSSH(this.CE_A, mangle_proxySIP+mangle_init_EF);
 	        //askSSH(this.CE_B, mangle_proxySIP+mangle_init_EF);
        	//!!!en l'état actuel pas de supression ou de check des iptables avant d'en rajouter, donc décommenter ces deux lignes rajoutera juste des règles redondantes!
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("Erreur à l'initialisation des iptables via ssh.");
 		}
        System.out.println("--Initialisation des iptables mangle sautée car déjà rentrées précédemment.--");
    }
    
    
    public int GetId_branch() {
    	return this.id_branch;
    }
    
    
    //communication ssh
    //pour faire passer plusieurs commandes, passer en arg "cmd1 ; cmd2" !!
    private static String askSSH(String host, String commande) throws Exception {
        //possible de modifier pour faire passer username et password en argument pour plus de généralité sur le déploiement
    		String responseString = "error";
          Session session = null;
          ChannelExec channel = null;
          String password="blanshneij";//TODO faux bien sûr, à modifier pour utilisation réelle
          //utiliser un property file pour la sécurité?
          
          try {
              session = new JSch().getSession("root", host, 22);
              session.setPassword(password);
              session.setConfig("StrictHostKeyChecking", "no");
              session.connect();
              
              channel = (ChannelExec) session.openChannel("exec");
              channel.setCommand(commande);
              ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
              channel.setOutputStream(responseStream);
              channel.connect();
              
              while (channel.isConnected()) {
                  Thread.sleep(100);
              }
              
              responseString = new String(responseStream.toByteArray());
              System.out.println(responseString);
          } finally {
              if (session != null || channel!=null) {
                  session.disconnect();
              }
          }
          return responseString;
      }


    // Vérifier si la connexion demandée peut être établie ou non
    // Quentin
    protected boolean checkRessourceUtilization(double demand){
        boolean conclusion_feasibility = false;

        if(SLA[0] >= demand && SLA[1] >= demand && demand >= 0.0){
            System.out.println("La connexion est faisable.");
            conclusion_feasibility = true;
        }

        // Dans les autres cas, l'établissement de la connexion reste infaisable
        return conclusion_feasibility;
    }

    public void addConnexion(Connexion connOK, double demand) {
        //Mise à jour du tableau de connexion
        t_connexion.add(connOK);//new Connexion(source, receiver, portSource, portRecei, demand, id_branch));
        id_branch++;

        //Mise à jour des ressources
        for(int i=0;i<SLA_u.length;i++){
            SLA_u[i]+=demand;
        }
        
        System.out.println("...connexion ajoutée! ("+connOK.getSource().getHostAddress()+" to "+connOK.getReceiver().getHostAddress()+")");
    }

    // Si connexion autorisée / fermée mise à jour de la table des connexions et utilisation des ressources
    public synchronized int addRessourcesConnexions(InetAddress source, InetAddress receiver, int portSource, int portRecei, double demand){
        int already_exists = 0;
        if(demand <=0){
            System.out.println("Erreur: La demande est négative");
        }
        else {
            int index = 0;
            if (t_connexion.size()>0) {
            	
	            while (index < t_connexion.size() && already_exists==0){
	                //possible de remplacer tout ça par une vérification de l'id_branch?
	                if(t_connexion.get(index).getSource().equals(source)){
	                    already_exists++;
	                }
	                if(t_connexion.get(index).getReceiver().equals(receiver)){
	                    already_exists++;
	                }
	                else{
	                    index++;
	                }           
	            }
            }
            if (already_exists==0) {
                try {
    				qdisc_EF_branch_update(true,(int)(1000*(float)demand), source, portSource, id_branch-1);
    			} catch (Exception e) {
    				e.printStackTrace();
    				System.out.println("Echec de la mise à jour des routeurs via ssh.");
    			}

	        System.out.println("La connexion "+source.getHostAddress()+" to "+receiver.getHostAddress()+" va être ajoutée...");
	        }
	    }
	    return already_exists;//à utiliser par la communication TCP
	}

    public synchronized boolean RemoveRessourcesConnexions(InetAddress source, int portSource){
        boolean retour = false;

        //Mise à jour du tableau de connexions
        int index = 0;
        int id = 0;
        boolean trouve = false;
        double bpaenlever = 0.0;
 
	        while (index < t_connexion.size() && trouve==false){
	            //possible de remplacer tout ça par une vérification de l'id_branch?
	            if(t_connexion.get(index).getSource().equals(source) && t_connexion.get(index).getPortSource()==portSource){
	                id=t_connexion.get(index).getId_branch();
	                bpaenlever = t_connexion.get(index).getBandWidth();
	                t_connexion.remove(index);
	                trouve=true;
	            }
	            else{
	                index++;
	            }           
	        }
	        if (trouve) {
		        try {
					qdisc_EF_branch_update(false,(int)(1000*(float)bpaenlever), source, portSource,id);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Echec de la mise à jour des routeurs via ssh.");
				}
		        //Mise à jour des ressources
		        for(int i=0;i<SLA_u.length;i++){
		            SLA_u[i]-=bpaenlever;
		        }
		        System.out.println("La connexion a été supprimée.");
		        
		     }else {
		        	System.out.println("--(suppression d'une communication non enregistrée demandée)--"); 
		        	retour = true;//en l'état on renverra toujours OK-closure et jamais NOK-closure
		     }
 
        return retour;//à utiliser par la communication TCP
    }
    
//Pierre
    private void qdisc_EF_branch_update(boolean creatrue_removalse, int debit_asked, InetAddress ipsource, int portsource, int indice) throws Exception{
       String update_tc = "";
       String indc=Integer.toString(indice+10);//on incrémente de 10 pour être sûr qu'il n'y ait pas d'interférences avec 1:2 qui est le best effort, vérifier le fonctionnement de tc
       String addr_src=ipsource.getHostAddress();
       if (creatrue_removalse){
            String dba=Integer.toString(debit_asked);        
            update_tc = "tc class add dev "+eth_used+" parent 1:1 classid 1:"+indc+" htb rate "+dba+"kbit ceil "+dba+"kbit";
            update_tc = update_tc+";tc filter add dev "+eth_used+" parent 1:1 protocol ip prio 1 u32 match ip src "+addr_src+"/32 match ip dport "+Integer.toString(portsource)+" 0xffff flowid 1:"+indc;
       
        }else{//note: il est obligatoire de del tous les filters et classes filles avant de supprimer une classe
            update_tc = "tc filter del dev "+eth_used+" parent 1:1 protocol ip prio 1 u32 match ip src "+addr_src+"/32 match ip dport "+Integer.toString(portsource)+" 0xffff flowid 1:"+indc;
            update_tc = update_tc+";tc class del dev "+eth_used+" classid 1:"+indc;
        }

		askSSH(this.CE_A, update_tc);
	    askSSH(this.CE_B, update_tc);
        //ATTENTION, plus il y aura de CE plus id_branch augmentera rapidement et risquera d'atteindre intMAX !

        System.out.println("QDisc mis à jour");
    }


    // Connexion TCP avec proxy SIP
    // Pierre
    private void listenTCP(){
        try (ServerSocket servSock = new ServerSocket(listening_port)) {
    
            System.out.println("Attente de connexion sur le port " + listening_port);

            while (true) {
                Socket socket = servSock.accept();
                System.out.println("Connexion d'un client TCP (probablement un proxy SIP)");
                new ThreadTCP(socket, this).start();//surtout pas run, start fait le parallelisme
            }

        } catch (IOException ex) {
            System.out.println("Exception (serveur TCP): " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Parser une string pour la table des connexions
    //Quentin
    private String[] parseString(String s){
        String[] parse = s.split(",");   //OK: @IPsrc , @IPdest, portsrc, portdest, bandwidth
                                         //BYE : @IPsrc, portsrc
        return parse;
    }


    // Affichage du tableau des connexions et de l'utilisation des ressources en %
    // Quentin
    protected void printConnexionTable(){

        //Afficher la table des connexion;
        System.out.println("SOURCE       RECEIVER       PORTSOURCE    PORTRECE    BANDWIDTH(Mbps)       ");
        for(Connexion c : t_connexion){
            c.toString();
            System.out.println(c);
        }

        //Afficher l'utilisation des ressources
        System.out.println("Utilisation ressources EF SLA-A-Coeur: " + SLA_u[0]/SLA[0]*100 + "%");
        System.out.println("Utilisation ressources EF Coeur-SLA-B: " + SLA_u[1]/SLA[1]*100 + "%");

    }


    public static void main(String[] args) {
    	
    	bandwidthbroker b = new bandwidthbroker();
        /*String[] reserv = b.parseString("192.168.1.1,192.168.1.2,6666,7777,0.5");
        System.out.println(reserv[0] + " " + reserv[1] + " " + reserv[2] + " " + reserv[3] + " " + reserv[4]);
        boolean test = b.checkRessourceUtilization(Double.parseDouble(reserv[4]));
        if(test==true){
            InetAddress address1 = InetAddress.getLoopbackAddress();
            InetAddress address2 = InetAddress.getLoopbackAddress();
            b.addRessourcesConnexions(address1, address2, 6666, 7777, 0.5);
            b.printConnexionTable();
            b.RemoveRessourcesConnexions(address1,6666);
            b.printConnexionTable();
        }
        else{
            System.out.println("Le test a échoué.");
        }*/

        //Cas avec une demande trop grande
        /*test = b.checkRessourceUtilization(2.0);
        if(test==true){
            InetAddress address1 = InetAddress.getLoopbackAddress();
            InetAddress address2 = InetAddress.getLoopbackAddress();
            b.updateRessourcesConnexions(address1, address2, 6666, 7777, 2.0, 0);
            b.printConnexionTable();
            b.updateRessourcesConnexions(address1, address2, 6666, 7777, 0.5, 1);
            b.printConnexionTable();
        }
        else{
            System.out.println("C'est normal la demande est trop grande pour un lien");
        }
        */
    }
}