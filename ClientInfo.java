import java.net.InetAddress;

/**
 * Classe `ClientInfo` représentant un client enregistré sur le serveur.
 * Cette classe stocke les informations essentielles d'un client :
 * - Son nom d'utilisateur.
 * - Son adresse IP.
 * - Son port UDP attribué pour la communication.
 * 
 * @author Abderrahim Flavien Ahdi
 */
public class ClientInfo {
    /** Nom du client */
    public String nom;

    /** Adresse IP du client */
    public InetAddress adresse;

    /** Port UDP du client utilisé pour la communication */
    public int port;

    /**
     * Constructeur de la classe `ClientInfo`.
     * 
     * @param nom     Nom du client.
     * @param adresse Adresse IP du client.
     * @param port    Port UDP attribué au client.
     */
    public ClientInfo(String nom, InetAddress adresse, int port) {
        this.nom = nom;
        this.adresse = adresse;
        this.port = port;
    }
}
