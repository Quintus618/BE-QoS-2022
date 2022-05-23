import java.net.InetAddress;

public class Connexion{

    private InetAddress source;
    private InetAddress receiver;
    private int portSource;
    private int portReceiver;
    private double bandWidth;
    private int id_branch;

    public Connexion(InetAddress source, InetAddress receiver, int portSource, int portRecei, double bandWidth, int id_branch){
        this.source = source;
        this.receiver = receiver;
        this.portSource = portSource;
        this.portReceiver = portRecei;
        this.bandWidth = bandWidth;
        this.id_branch = id_branch;
        //codecs non retenus car inutiles à connaître par le BB
    }

/*
 * 
 *  SETTER
 * 
 */

    public void setSource(InetAddress source) {
        this.source = source;
    }

    public void setReceiver(InetAddress receiver) {
        this.receiver = receiver;
    }

    public void setPortSource(int portSource) {
        this.portSource = portSource;
    }

    public void setPortReceiver(int portReceiver) {
        this.portReceiver = portReceiver;
    }

    public void setBandWidth(int bandWidth) {
        this.bandWidth = bandWidth;
    }

/*
 * 
 *  GETTER
 * 
 */

    public double getBandWidth() {
        return bandWidth;
    }

    public int getPortReceiver() {
        return portReceiver;
    }

    public int getPortSource() {
        return portSource;
    }

    public InetAddress getReceiver() {
        return receiver;
    }

    public InetAddress getSource() {
        return source;
    }

    public int getId_branch() {
        return id_branch;
    }

    public String toString() {
        return source.getHostAddress() + "       " + receiver.getHostAddress() + "       "
        + portSource + "       " + portReceiver + "       " + bandWidth + "                 ";
    }

}