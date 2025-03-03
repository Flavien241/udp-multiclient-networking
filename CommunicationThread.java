import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CommunicationThread implements Runnable {
    private String message;
    private InetAddress sourceAdresse;
    private int sourcePort;
    private DatagramSocket socket;  // ✅ Ajout du socket

    public CommunicationThread(String message, InetAddress sourceAdresse, int sourcePort, DatagramSocket socket) {
        this.message = message;
        this.sourceAdresse = sourceAdresse;
        this.sourcePort = sourcePort;
        this.socket = socket;  // ✅ Stocker le socket
    }

    @Override
    public void run() {
        try {
            String[] parts = message.split(":", 3);
            if (parts.length < 2) return;

            String typeMessage = parts[0].trim();
            String destinataire = parts[1].trim();
            String contenu = parts.length > 2 ? parts[2].trim() : "";

            if (typeMessage.equals("PUBLIC")) {
                for (String client : UDPServerMulti.clients.keySet()) {
                    if (!UDPServerMulti.clients.get(client).adresse.equals(sourceAdresse) || UDPServerMulti.clients.get(client).port != sourcePort) {
                        envoyerMessage(client, "📢 " + contenu);
                    }
                }
            } else if (typeMessage.equals("PRIVATE") && UDPServerMulti.clients.containsKey(destinataire)) {
                envoyerMessage(destinataire, "💌 " + contenu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void envoyerMessage(String destinataire, String contenu) {
        try {
            ClientInfo client = UDPServerMulti.clients.get(destinataire);
            byte[] bufferReponse = contenu.getBytes();
            DatagramPacket dpReponse = new DatagramPacket(bufferReponse, bufferReponse.length, client.adresse, client.port);
            socket.send(dpReponse);
            System.out.println("📩 Message envoyé à " + destinataire + " : " + contenu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
