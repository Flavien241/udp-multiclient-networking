import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class UDPServerMulti {
    private static final int PORT_SERVEUR = 1024;
    
    // Table de correspondance { Nom -> (Adresse, Port) }
    public static ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (DatagramSocket dsS = new DatagramSocket(PORT_SERVEUR)) {
            System.out.println("Serveur démarré sur le port " + PORT_SERVEUR);

            while (true) {
                byte[] bufferReception = new byte[1024];
                DatagramPacket dpS = new DatagramPacket(bufferReception, bufferReception.length);
                dsS.receive(dpS);

                String messageRecu = new String(dpS.getData(), 0, dpS.getLength());
                InetAddress adresseClient = dpS.getAddress();
                int portClient = dpS.getPort();

                // 🔹 Gestion de l'enregistrement du client
                if (messageRecu.startsWith("REGISTER:")) {
                    String nom = messageRecu.split(":")[1].trim();

                    // Vérification si le nom est déjà utilisé
                    if (clients.containsKey(nom)) {
                        String messageErreur = "ERROR: Ce nom est déjà pris. Veuillez choisir un autre nom.";
                        envoyerMessage(dsS, messageErreur, adresseClient, portClient);
                        System.out.println("Tentative d'enregistrement avec un nom déjà pris : " + nom);
                        continue;
                    }

                    // Ajout du client s'il n'existe pas
                    clients.put(nom, new ClientInfo(nom, adresseClient, portClient));
                    System.out.println(nom + " est enregistré avec " + adresseClient + ":" + portClient);
                    envoyerMessage(dsS, "SUCCESS: Enregistrement réussi.", adresseClient, portClient);
                    continue;
                }

                // 🔹 Gestion des messages privés et publics
                new Thread(new CommunicationThread(messageRecu, adresseClient, portClient, dsS)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 📌 Fonction pour envoyer un message à un client
    private static void envoyerMessage(DatagramSocket socket, String message, InetAddress adresse, int port) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, adresse, port);
            socket.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
