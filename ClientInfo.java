import java.net.InetAddress;

public class ClientInfo {
    public String nom;
    public InetAddress adresse;
    public int port;

    public ClientInfo(String nom, InetAddress adresse, int port) {
        this.nom = nom;
        this.adresse = adresse;
        this.port = port;
    }
}
