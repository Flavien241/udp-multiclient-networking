import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe `CommunicationThread` gérant la communication entre le serveur et un client spécifique.
 * Chaque client a son propre thread et son propre socket pour recevoir et envoyer des messages.
 * 
 * Fonctionnalités :
 * - Écoute des messages entrants du client.
 * - Gestion des messages publics, privés et de groupe.
 * - Transmission des messages aux destinataires appropriés.
 * - Gestion de la déconnexion du client.
 *
 * @author Abderrahim Flavien Ahdi
 */
public class CommunicationThread implements Runnable {
    /** Socket UDP dédié à ce client */
    private DatagramSocket socket;

    /** Nom du client géré par ce thread */
    private String clientNom;

    /**
     * Constructeur de la classe `CommunicationThread`.
     * 
     * @param socket    Socket UDP unique pour ce client.
     * @param clientNom Nom d'utilisateur du client associé.
     */
    public CommunicationThread(DatagramSocket socket, String clientNom) {
        this.socket = socket;
        this.clientNom = clientNom;
    }

    /**
     * Méthode principale du thread.
     * - Écoute en boucle les messages envoyés par le client.
     * - Gère les différentes actions : messages publics, privés, groupes et déconnexion.
     */
    @Override
    public void run() {
        try {
            System.out.println("🔹 CommunicationThread démarré pour " + clientNom + " sur le port " + socket.getLocalPort());

            while (true) {
                // 🔹 Réception d'un message du client
                byte[] bufferReception = new byte[1024];
                DatagramPacket dpReception = new DatagramPacket(bufferReception, bufferReception.length);
                socket.receive(dpReception);

                // 🔹 Extraction et affichage du message reçu
                String messageRecu = new String(dpReception.getData(), 0, dpReception.getLength());
                InetAddress sourceAdresse = dpReception.getAddress();
                int sourcePort = dpReception.getPort();

                System.out.println("📩 Message reçu de " + clientNom + " : " + messageRecu);

                // 🔹 Vérification du type de message
                String[] parts = messageRecu.split(":", 3);
                if (parts.length < 2) continue;

                String typeMessage = parts[0].trim();  // PUBLIC, PRIVATE, GROUP ou EXIT
                String destinataire = parts[1].trim();
                String contenu = parts.length > 2 ? parts[2].trim() : "";

                // 🔹 Gestion des messages publics
                if (typeMessage.equals("PUBLIC")) {
                    System.out.println("📢 Message PUBLIC envoyé à tous : " + contenu);
                    for (String client : UDPServerMulti.clients.keySet()) {
                        if (!client.equals(clientNom)) {
                            envoyerMessage(client, "🔹 PUBLIC de " + clientNom + " : " + contenu);
                        }
                    }
                }
                // 🔹 Gestion des messages privés
                else if (typeMessage.equals("PRIVATE") && UDPServerMulti.clients.containsKey(destinataire)) {
                    System.out.println("📩 Message PRIVÉ de " + clientNom + " à " + destinataire + " : " + contenu);
                    envoyerMessage(destinataire, "💌 PRIVÉ de " + clientNom + " : " + contenu);
                }
                // 🔹 Gestion des messages de groupe
                else if (typeMessage.equals("GROUP") && UDPServerMulti.groupes.containsKey(destinataire)) {
                    System.out.println("👥 Message GROUPE (" + destinataire + ") de " + clientNom + " : " + contenu);
                    for (String membre : UDPServerMulti.groupes.get(destinataire)) {
                        if (!membre.equals(clientNom)) {
                            envoyerMessage(membre, "👥 [Groupe " + destinataire + "] " + clientNom + " : " + contenu);
                        }
                    }
                }
                // 🔹 Gestion de la déconnexion du client
                else if (typeMessage.equals("EXIT")) {
                    System.out.println("❌ Déconnexion de " + clientNom);
                    UDPServerMulti.clients.remove(clientNom);
                    socket.close();  // ✅ Fermeture du socket
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envoie un message à un client spécifique.
     * 
     * @param destinataire Nom du client destinataire.
     * @param contenu      Contenu du message à envoyer.
     */
    private void envoyerMessage(String destinataire, String contenu) {
        try {
            ClientInfo client = UDPServerMulti.clients.get(destinataire);
            byte[] bufferReponse = contenu.getBytes();
            DatagramPacket dpReponse = new DatagramPacket(bufferReponse, bufferReponse.length, client.adresse, client.port);
            socket.send(dpReponse);
            System.out.println("📤 Message envoyé à " + destinataire + " : " + contenu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
