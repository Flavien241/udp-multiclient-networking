import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Classe UDPClientMulti permettant à un client de communiquer avec un serveur UDP.
 * Ce client peut :
 * - S'enregistrer auprès du serveur et recevoir un port dédié.
 * - Envoyer des messages publics, privés et à des groupes.
 * - Recevoir des messages via un thread dédié.
 * - Se déconnecter proprement en envoyant une commande "EXIT".
 * 
 * @author Abderrahim Flavien Ahdi
 */
public class UDPClientMulti {
    /** Adresse IP du serveur */
    private static final String SERVEUR_ADRESSE = "127.0.0.1";
    
    /** Port initial utilisé pour l'enregistrement auprès du serveur */
    private static final int SERVEUR_PORT = 1024;
    
    /** Nom d'utilisateur du client */
    private static String nomUtilisateur;
    
    /** Port attribué au client après enregistrement */
    private static int clientPort;

    /**
     * Méthode principale exécutant le client.
     * - S'enregistre auprès du serveur et récupère son port dédié.
     * - Lance un thread pour écouter les messages entrants.
     * - Permet d'envoyer des messages publics, privés et à des groupes.
     * 
     * @param args Arguments de ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        try (DatagramSocket dsC1 = new DatagramSocket()) { // Création du socket client
            InetAddress serverAddress = InetAddress.getByName(SERVEUR_ADRESSE);
            Scanner scanner = new Scanner(System.in);

            // 🔹 Demande du nom d'utilisateur
            while (true) {
                System.out.print("👤 Entrez votre nom : ");
                nomUtilisateur = scanner.nextLine().trim();

                // Enregistrement auprès du serveur
                String messageRegister = "REGISTER:" + nomUtilisateur;
                DatagramPacket dpRegister = new DatagramPacket(messageRegister.getBytes(), messageRegister.length(), serverAddress, SERVEUR_PORT);
                dsC1.send(dpRegister);

                // 🔹 Après enregistrement, on récupère le nouveau port attribué
                byte[] bufferResponse = new byte[1024];
                DatagramPacket dpResponse = new DatagramPacket(bufferResponse, bufferResponse.length);
                dsC1.receive(dpResponse);
                String reponse = new String(dpResponse.getData(), 0, dpResponse.getLength()).trim();

                if (reponse.startsWith("ERROR")) {
                    System.out.println("❌ " + reponse);
                } else if (reponse.startsWith("SUCCESS")) {
                    System.out.println("✅ " + reponse);
                    String[] parts = reponse.split(" ");
                    clientPort = Integer.parseInt(parts[parts.length - 1]);
                    dsC1.connect(serverAddress, clientPort);  // ✅ Le client envoie maintenant à `new_socket`
                    break;
                }
            }

            // 🔹 Thread pour recevoir les messages
            Thread receptionThread = new Thread(new ReceptionThread(dsC1));
            receptionThread.start();

            // 🔹 Envoi des messages et gestion des groupes
            while (true) {
                afficherMenu();
                String choix = scanner.nextLine();

                String message = genererMessage(choix, scanner);
                if (message == null) continue;
                if (message.equals("EXIT")) break;

                // 🔹 Envoi du message au serveur
                byte[] bufferEnvoi = message.getBytes();
                DatagramPacket dp1 = new DatagramPacket(bufferEnvoi, bufferEnvoi.length, serverAddress, clientPort);
                dsC1.send(dp1);
                System.out.println("📤 Message envoyé !");
            }

            // 🔹 Fermeture propre du scanner et du socket
            scanner.close();
            dsC1.close();
            System.out.println("✅ Client déconnecté avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche le menu des commandes disponibles.
     */
    private static void afficherMenu() {
        System.out.println("\n🔹 Commandes : ");
        System.out.println("  [1] Envoyer un message PUBLIC");
        System.out.println("  [2] Envoyer un message PRIVÉ");
        System.out.println("  [3] Créer un groupe");
        System.out.println("  [4] Rejoindre un groupe");
        System.out.println("  [5] Envoyer un message à un groupe");
        System.out.println("  [6] Quitter");
        System.out.print("Votre choix : ");
    }

    /**
     * Génère un message formaté en fonction du choix de l'utilisateur.
     * 
     * @param choix La commande choisie par l'utilisateur.
     * @param scanner Scanner utilisé pour lire l'entrée utilisateur.
     * @return Le message formaté à envoyer au serveur.
     */
    private static String genererMessage(String choix, Scanner scanner) {
        String message = "";

        switch (choix) {
            case "1": // Message public
                System.out.print("💬 Message à envoyer à tous : ");
                String contenuPublic = scanner.nextLine();
                message = "PUBLIC:ALL:" + contenuPublic;
                break;
            
            case "2": // Message privé
                System.out.print("👤 Nom du destinataire : ");
                String destinataire = scanner.nextLine();
                System.out.print("💌 Message : ");
                String contenuPrive = scanner.nextLine();
                message = "PRIVATE:" + destinataire + ":" + contenuPrive;
                break;

            case "3": // Création d'un groupe
                System.out.print("📌 Nom du groupe à créer : ");
                String nomGroupe = scanner.nextLine();
                message = "CREATE_GROUP:" + nomGroupe;
                break;

            case "4": // Rejoindre un groupe
                System.out.print("👥 Nom du groupe à rejoindre : ");
                String nomGroupeJoin = scanner.nextLine();
                message = "JOIN_GROUP:" + nomGroupeJoin;
                break;

            case "5": // Message de groupe
                System.out.print("👥 Nom du groupe : ");
                String nomGroupeMsg = scanner.nextLine();
                System.out.print("💌 Message : ");
                String contenuGroupe = scanner.nextLine();
                message = "GROUP:" + nomGroupeMsg + ":" + contenuGroupe;
                break;

            case "6": // Déconnexion
                System.out.println("👋 Déconnexion...");
                message = "EXIT:" + nomUtilisateur;
                return "EXIT";

            default:
                System.out.println("❌ Choix invalide. Réessayez.");
                return null;
        }

        return message;
    }
}
