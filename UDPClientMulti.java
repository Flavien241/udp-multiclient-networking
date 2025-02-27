import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClientMulti {
    private static final String SERVEUR_ADRESSE = "127.0.0.1";
    private static final int SERVEUR_PORT = 1024;
    private static String nomUtilisateur;

    public static void main(String[] args) {
        try (DatagramSocket dsC1 = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVEUR_ADRESSE);
            Scanner scanner = new Scanner(System.in);

            // Demande du nom d'utilisateur
            System.out.print("👤 Entrez votre nom : ");
            nomUtilisateur = scanner.nextLine().trim();

            // Enregistrement auprès du serveur
            String messageRegister = "REGISTER:" + nomUtilisateur;
            DatagramPacket dpRegister = new DatagramPacket(messageRegister.getBytes(), messageRegister.length(), serverAddress, SERVEUR_PORT);
            dsC1.send(dpRegister);

            // Thread pour recevoir les messages
            Thread receptionThread = new Thread(new ReceptionThread(dsC1));
            receptionThread.start();

            // Envoi des messages
            while (true) {
                System.out.println("\n🔹 Commandes : ");
                System.out.println("  [1] Envoyer un message PUBLIC");
                System.out.println("  [2] Envoyer un message PRIVÉ");
                System.out.print("Votre choix : ");
                String choix = scanner.nextLine();

                String message = "";
                if (choix.equals("1")) {
                    System.out.print("Message à envoyer à tous : ");
                    String contenu = scanner.nextLine();
                    message = "PUBLIC:ALL:" + contenu;
                } else if (choix.equals("2")) {
                    System.out.print("Nom du destinataire : ");
                    String destinataire = scanner.nextLine();
                    System.out.print("Message : ");
                    String contenu = scanner.nextLine();
                    message = "PRIVATE:" + destinataire + ":" + contenu;
                }

                byte[] bufferEnvoi = message.getBytes();
                DatagramPacket dp1 = new DatagramPacket(bufferEnvoi, bufferEnvoi.length, serverAddress, SERVEUR_PORT);
                dsC1.send(dp1);
                System.out.println("Message envoyé !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
