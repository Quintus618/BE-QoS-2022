import java.lang.management.ThreadInfo;
import java.net.InetAddress;
import java.util.ArrayList;

public class bandwidthbroker{

    // Premier élément SLA_A->Réseau de coeur
    // Second élément Réseaux de coeur -> SLA-B
    private double[] SLA = {1,2};

    // Utilisation des ressources EF des deux SLAs
    private double[] SLA_u = {0,0};

    // Tableau récapitulatif des connexions établies
    private ArrayList<Connexion> t_connexion = new ArrayList<Connexion>();

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
            t_connexion.add(new Connexion(source, receiver, portSource, portRecei, demand, codecs));

            //Mise à jour des ressources
            for(int i=0;i<SLA_u.length;i++){
                SLA_u[i]+=demand;
            }

            System.out.println("La connexion a été ajoutée.");
        }
        else{

            //Mise à jour du tableau de connexions
            int index = 0;
            boolean trouve = false;
            while (index <= t_connexion.size() && trouve==false){
                if(t_connexion.get(index).getSource().equals(source) && t_connexion.get(index).getReceiver().equals(receiver) && t_connexion.get(index).getPortSource()==portSource && t_connexion.get(index).getPortReceiver()== portRecei){
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

            System.out.println("La connexion a été supprimée.");
        }
    }

    // Connexion TCP avec proxy SIP ENVOI
    //TODO
    // Pierre
    private void TCPSender(){

    }

    // Connexion TCP avec proxy SIP RECEPTION
    //TODO
    // Pierre
    private void TCPReceiver(){

    }

    //Parser une string pour la table des connexions
    //TODO
    //Quentin
    private String[] parseString(){
        String[] parse = new String[6];   //@IPsrc , @IPdest, portsrc, portdest, codec, bandwidth

        return parse;
    }

    // Etablissement d'une connexion ssh avec les routeur de sortie de site pour allouer/supprimer une file pour la connexion
    // Pierre
    private void sshEgressRouterSite(){

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
    }

}