import java.lang.management.ThreadInfo;
import java.net.InetAddress;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class bandwidthbroker{

    // Premier élément SLA_A->Réseau de coeur
    // Second élément Réseaux de coeur -> SLA-B
    private double[] SLA = {1,2};

    // Utilisation des ressources EF des deux SLAs
    private double[] SLA_u = {0,0};

    // Tableau récapitulatif des connexions établies
    private ArrayList<Connexion> t_connexion = new ArrayList<Connexion>();

    private int listening_port = 12121;//TODO se mettre d'accord avec le SIP

    private String CE_A;
    private String CE_B;

    private int id_branch;

    private static String BB_IP = "193.168.1.1";


    public bandwidthbroker(){//TODO décommenter

        //initialisation des tc avec les SLAs
        String TC_A_init = "tc qdisc del dev eth0 root;tc qdisc add dev eth0 root handle 1: htb default 20;tc class add dev eth0 parent 1: classid 1:1 htb rate 1000kbit ceil 1100kbit;tc class add dev eth0 parent 1: classid 1:2 htb rate 2000kbit ceil 2100kbit;tc filter add dev eth0 parent 1: protocol ip prio 1 handle 20 fw flowid 1:2";
        //1: voix, 2:parasite ->handle 20 va en parasite?
        String TC_B_init = "tc qdisc del dev eth0 root;tc qdisc add dev eth0 root handle 1: htb default 20;tc class add dev eth0 parent 1: classid 1:1 htb rate 2000kbit ceil 2100kbit;tc class add dev eth0 parent 1: classid 1:2 htb rate 3000kbit ceil 3100kbit;tc filter add dev eth0 parent 1: protocol ip prio 1 handle 20 fw flowid 1:2";
              
        this.CE_A = "193.168.1.254";
        this.CE_B = "193.168.2.254";

        askSSH(this.CE_A, TC_A_init);
        askSSH(this.CE_B, TC_B_init);
        //pour plus de clients possible de faire un for avec un dictionnaire associant host et commandes

        this.id_branch = 1;

        //this.listenTCP();


    }

    //communication ssh
    //pour faire passer plusieurs commandes, passer en arg "cmd1 ; cmd2" !!
    private static void askSSH(String host, String commande) throws Exception {
        //possible de modifier pour faire passer username et password en argument pour plus de généralité sur le déploiement
          
          Session session = null;
          ChannelExec channel = null;
          String password="blanshneij";//faux bien sûr, à modifier
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
              
              String responseString = new String(responseStream.toByteArray());
              System.out.println(responseString);
          } finally {
              if (session != null || channel!=null) {
                  session.disconnect();
              }
          }
      }


    // Vérifier si la connexion demandée peut être établie ou non
    // Quentin
    private boolean checkRessourceUtilization(double demand){
        boolean conclusion_feasibility = false;

        if(SLA[0] >= demand && SLA[1] >= demand && demand >= 0.0){
            System.out.println("La connexion est faisable.");
            conclusion_feasibility = true;
        }

        // Dans les autres cas, l'établissement de la connexion reste infaisable
        return conclusion_feasibility;
    }


    // Si connexion autorisée / fermée mise à jour de la table des connexions et utilisation des ressources
    // Quentin
    private void updateRessourcesConnexions(InetAddress source, InetAddress receiver, int portSource, int portRecei, String[] codecs, double demand, int closeConnection){

        if(demand <=0){
            System.out.println("Erreur: La demande est négative");
        }
        else if(closeConnection==0){

            //Mise à jour du tableau de connexion
            t_connexion.add(new Connexion(source, receiver, portSource, portRecei, demand, codecs, id_branch));
            id_branch++;

            //Mise à jour des ressources
            for(int i=0;i<SLA_u.length;i++){
                SLA_u[i]+=demand;
            }
            qdisc_EF_branch_update(True,(int)1000*(float)demand, source, portSource, id_branch-1);
            System.out.println("La connexion a été ajoutée.");
        }
        else{

            //Mise à jour du tableau de connexions
            int index = 0;
            int id = 0;
            boolean trouve = false;
            while (index <= t_connexion.size() && trouve==false){
                //possible de remplacer tout ça par une vérification de l'id_branch?
                if(t_connexion.get(index).getSource().equals(source) && t_connexion.get(index).getReceiver().equals(receiver) && t_connexion.get(index).getPortSource()==portSource && t_connexion.get(index).getPortReceiver()== portRecei){
                    id=t_connexion.get(index).getId_branch();
                    t_connexion.remove(index);
                    trouve=true;
                }
                else{
                    index++;
                }           
            }

            //Mise à jour des ressources
            for(int i=0;i<SLA_u.length;i++){
                SLA_u[i]-=demand;
            }

            qdisc_EF_branch_update(False,(int)1000*(float)demand, source, portSource,id);
            System.out.println("La connexion a été supprimée.");
        }
    }


    private void qdisc_EF_branch_update(boolean creatrue_removalse, int debit_asked, String ipsource, String portsource, int indice){
       String update_tc = "";
       String indc=Integer.toString(indice+10);//on incrémente de 10 pour être sûr qu'il n'y ait pas d'interférences avec 1:2 qui est le best effort, vérifier le fonctionnement de tc
        if (creatrue_removalse){
            String dba=Integer.toString(debit_asked);        
            update_tc = "tc class add dev eth0 parent 1:1 classid 1:"+indc+" htb rate "+dba+"kbit ceil "+dba+"kbit";
            update_tc = update_tc+";tc filter add dev eth0 parent 1:1 protocol ip prio 1 u32 match ip src "+ipsource+"/32 match ip dport "+Integer.toString(portsource)+" 0xffff flowid 1:"+indc;
       
        }else{//note: il est obligatoire de del tous les filters et classes filles avant de supprimer une classe
            update_tc = "tc filter del dev eth0 parent 1:1 protocol ip prio 1 u32 match ip src "+ipsource+"/32 match ip dport "+Integer.toString(portsource)+" 0xffff flowid 1:"+indc;
            update_tc = update_tc+";tc class del dev eth0 classid 1:"+indc;
        }

        askSSH(this.CE_A, update_tc);
        askSSH(this.CE_B, update_tc);
        //ATTENTION, plus il y aura de CE plus id_branch augmentera rapidement et risquera d'atteindre intMAX !

        System.out.println("QDisc mis à jour");
    }


    // Connexion TCP avec proxy SIP
    //TODO finish
    // Pierre
    private void listenTCP(){
        try (ServerSocket servSock = new ServerSocket(listening_port)) {
    
            System.out.println("Attente de connexion sur le port " + listening_port);

            while (true) {
                Socket socket = servSock.accept();
                System.out.println("Connexion d'un client TCP");

                new ThreadTCP(socket).start();
            }

        } catch (IOException ex) {
            System.out.println("Exception (serveur TCP): " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Parser une string pour la table des connexions
    //TODO
    //Quentin
    private String[] parseString(){
        String[] parse = new String[6];   //@IPsrc , @IPdest, portsrc, portdest, codec, bandwidth

        return parse;
    }


    // Affichage du tableau des connexions et de l'utilisation des ressources en %
    //TODO
    // Quentin
    private void printConnexionTable(){

        //Afficher la table des connexion;
        System.out.println("SOURCE       RECEIVER       PORTSOURCE    PORTRECE    BANDWIDTH(Mbps)       CODECS");
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
/*
        boolean test = b.checkRessourceUtilization(0.5);
        if(test==true){
            InetAddress address1 = InetAddress.getLoopbackAddress();
            InetAddress address2 = InetAddress.getLoopbackAddress();
            String[] codec = {"toto", "tata"};
            b.updateRessourcesConnexions(address1, address2, 6666, 7777, codec, 0.5, 0);
            b.printConnexionTable();
            b.updateRessourcesConnexions(address1, address2, 6666, 7777, codec, 0.5, 1);
            b.printConnexionTable();
        }
        else{
            System.out.println("Le test a échoué.");
        }

        //Cas avec une demande trop grande
        test = b.checkRessourceUtilization(2.0);
        if(test==true){
            InetAddress address1 = InetAddress.getLoopbackAddress();
            InetAddress address2 = InetAddress.getLoopbackAddress();
            String[] codec = {"toto", "tata"};
            b.updateRessourcesConnexions(address1, address2, 6666, 7777, codec, 2.0, 0);
            b.printConnexionTable();
            b.updateRessourcesConnexions(address1, address2, 6666, 7777, codec, 0.5, 1);
            b.printConnexionTable();
        }
        else{
            System.out.println("C'est normal la demande est trop grande pour un lien");
        }
        */
    }
}