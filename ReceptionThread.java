import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Classe `ReceptionThread` permettant d'écouter les messages reçus par un client UDP.
 * Ce thread fonctionne en parallèle et attend en permanence les messages envoyés par le serveur.
 *
 * @author Abderrahim Flavien Ahdi
 */
public class ReceptionThread implements Runnable {
    /** Socket UDP utilisé pour recevoir les messages */
    private DatagramSocket dsC1;

    /**
     * Constructeur de la classe `ReceptionThread`.
     * 
     * @param dsC1 Le socket UDP du client utilisé pour la réception des messages.
     */
    public ReceptionThread(DatagramSocket dsC1) {
        this.dsC1 = dsC1;
    }

    /**
     * Méthode exécutée par le thread.
     * - Attend les messages entrants du serveur.
     * - Affiche les messages reçus sur la console.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // 🔹 Préparation du buffer de réception
                byte[] bufferReception = new byte[1024];
                DatagramPacket dpReponse = new DatagramPacket(bufferReception, bufferReception.length);

                // 🔹 Attente et réception d'un message UDP
                dsC1.receive(dpReponse);

                // 🔹 Conversion du message en texte
                String messageRecu = new String(dpReponse.getData(), 0, dpReponse.getLength());

                // 🔹 Affichage du message reçu
                System.out.println("📩 Message reçu : " + messageRecu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
