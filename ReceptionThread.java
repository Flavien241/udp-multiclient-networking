import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceptionThread implements Runnable {
    private DatagramSocket dsC1;

    public ReceptionThread(DatagramSocket dsC1) {
        this.dsC1 = dsC1;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] bufferReception = new byte[1024];
                DatagramPacket dpReponse = new DatagramPacket(bufferReception, bufferReception.length);
                dsC1.receive(dpReponse);

                String messageRecu = new String(dpReponse.getData(), 0, dpReponse.getLength());
                System.out.println("📩 Message reçu : " + messageRecu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
