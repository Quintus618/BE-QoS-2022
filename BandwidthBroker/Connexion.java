import java.net.InetAddress;
import java.util.*;

public class Connexion{

    private InetAddress source;
    private InetAddress receiver;
    private int portSource;
    private int portReceiver;
    private double bandWidth;
    private String[] codecs;
    private int id_branch;

    public Connexion(InetAddress source, InetAddress receiver, int portSource, int portRecei, double bandWidth, String[] codecs, int id_branch){
        this.source = source;
        this.receiver = receiver;
        this.portSource = portSource;
        this.portReceiver = portRecei;
        this.bandWidth = bandWidth;
        this.codecs = new String[codecs.length];
        this.id_branch = id_branch;
        System.arraycopy(codecs, 0, this.codecs, 0, this.codecs.length);
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

    public void setCodecs(String[] codecs) {
        this.codecs = codecs;
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

    public String[] getCodecs() {
        return codecs;
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
        + portSource + "       " + portReceiver + "       " + bandWidth + "                 " + Arrays.toString(codecs);
    }

}