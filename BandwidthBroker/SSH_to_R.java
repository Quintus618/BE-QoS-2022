import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;


public class SSH_to_R {
//WARNING autoriser transport input ssh sur les routeurs

//pour faire passer plusieurs commandes, passer en arg "cmd1 ; cmd2"
    public static void askSSH(String host, String commande) throws Exception {
    //possible de passer username et password en argument pour plus de généralité sur le déploiement
      
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
}