public class bandwidthbroker{

    // Premier élément SLA_A->Réseau de coeur
    // Second élément Réseaux de coeur -> SLA-B
    private int[] SLA = {1,2};

    // Utilisation des ressources EF des deux SLAs
    private int[] SLA_u = {0,0};

    // Tableau récapitulatif des connexions établies
    private String[] t_connexion;

    // Vérifier si la connexion demandée peut être établie ou non
    //TODO
    // Quentin
    private bool checkRessourceUtilization(){

    }

    // Si connexion autorisée / fermée mise à jour de la table des connexions et utilisation des ressources
    //TODO
    // Quentin
    private void updateRessourcesConnexions(){

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

    }

}