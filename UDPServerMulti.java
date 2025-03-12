import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe UDPServerMulti représentant un serveur UDP multi-clients.
 * Ce serveur permet :
 * - L'enregistrement des clients avec un port unique.
 * - La gestion de groupes de discussion.
 * - L'envoi et la réception de messages UDP.
 *
 * @author Abderrahim Flavien Ahdi
 */
public class UDPServerMulti {
    /** Port sur lequel le serveur écoute pour les nouvelles connexions */
    private static final int PORT_SERVEUR = 1024;

    /** Table de correspondance { Nom d'utilisateur -> Informations client (Adresse, Port) } */
    public static ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();
    
    /** Liste des groupes { Nom du groupe -> Liste des membres } */
    public static ConcurrentHashMap<String, List<String>> groupes = new ConcurrentHashMap<>();

    /**
     * Méthode principale du serveur qui gère les connexions des clients et leurs requêtes.
     * 
     * @param args Arguments de ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        try (DatagramSocket dsS = new DatagramSocket(PORT_SERVEUR)) {
            System.out.println("✅ Serveur démarré sur le port " + PORT_SERVEUR);

            while (true) {
                byte[] bufferReception = new byte[1024];
                DatagramPacket dpS = new DatagramPacket(bufferReception, bufferReception.length);
                dsS.receive(dpS); // 🔹 Attente d'un message d'un client

                String messageRecu = new String(dpS.getData(), 0, dpS.getLength());
                InetAddress adresseClient = dpS.getAddress();
                int portClient = dpS.getPort();

                // 🔹 Gestion de l'enregistrement du client
                if (messageRecu.startsWith("REGISTER:")) {
                    String nom = messageRecu.split(":")[1].trim();

                    // Vérification si le nom est déjà utilisé
                    if (clients.containsKey(nom)) {
                        envoyerMessage(dsS, "ERROR: Ce nom est déjà pris. Veuillez choisir un autre nom.", adresseClient, portClient);
                        System.out.println("⚠️ Tentative d'enregistrement avec un nom déjà pris : " + nom);
                        continue;
                    }

                    // ✅ Création d'un socket unique pour le client
                    DatagramSocket new_socket = new DatagramSocket(); 
                    int new_port = new_socket.getLocalPort(); // Récupération du port attribué

                    // ✅ Ajout du client à la table des correspondances
                    clients.put(nom, new ClientInfo(nom, adresseClient, new_port));
                    System.out.println("✅ " + nom + " est enregistré avec " + adresseClient + ":" + new_port);

                    // ✅ Informer le client du port à utiliser après enregistrement
                    envoyerMessage(dsS, "SUCCESS: Enregistrement réussi. Utilisez le port " + new_port, adresseClient, portClient);

                    // ✅ Création d'un thread dédié avec un socket propre
                    new Thread(new CommunicationThread(new_socket, nom)).start();
                    continue;
                }

                // 🔹 Gestion de la création de groupe
                if (messageRecu.startsWith("CREATE_GROUP:")) {
                    String nomGroupe = messageRecu.split(":")[1].trim();
                    if (groupes.containsKey(nomGroupe)) {
                        envoyerMessage(dsS, "ERROR: Ce groupe existe déjà.", adresseClient, portClient);
                    } else {
                        groupes.put(nomGroupe, new ArrayList<>());
                        envoyerMessage(dsS, "SUCCESS: Groupe " + nomGroupe + " créé.", adresseClient, portClient);
                        System.out.println("✅ Groupe créé : " + nomGroupe);
                    }
                    continue;
                }

                // 🔹 Gestion de l'adhésion à un groupe
                if (messageRecu.startsWith("JOIN_GROUP:")) {
                    String[] parts = messageRecu.split(":", 2);
                    if (parts.length < 2) continue;
                    
                    String nomGroupe = parts[1].trim();
                    String clientNom = getClientName(adresseClient, portClient);

                    if (!groupes.containsKey(nomGroupe)) {
                        envoyerMessage(dsS, "ERROR: Ce groupe n'existe pas.", adresseClient, portClient);
                    } else {
                        groupes.get(nomGroupe).add(clientNom);
                        envoyerMessage(dsS, "SUCCESS: Vous avez rejoint " + nomGroupe, adresseClient, portClient);
                        System.out.println("👥 " + clientNom + " a rejoint le groupe " + nomGroupe);
                    }
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envoie un message UDP à un client spécifique.
     * 
     * @param socket   Socket UDP utilisé pour l'envoi.
     * @param message  Message à envoyer.
     * @param adresse  Adresse IP du client.
     * @param port     Port UDP du client.
     */
    private static void envoyerMessage(DatagramSocket socket, String message, InetAddress adresse, int port) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, adresse, port);
            socket.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère le nom d'un client à partir de son adresse et de son port.
     * 
     * @param adresse Adresse IP du client.
     * @param port    Port UDP du client.
     * @return Le nom du client ou `null` s'il n'est pas trouvé.
     */
    private static String getClientName(InetAddress adresse, int port) {
        for (String nom : clients.keySet()) {
            ClientInfo client = clients.get(nom);
            if (client.adresse.equals(adresse) && client.port == port) {
                return nom;
            }
        }
        return null;
    }
}
