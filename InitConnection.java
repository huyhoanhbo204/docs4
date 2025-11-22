import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class InitConnection {
    private ServerSocket serverSocket;
    private boolean running = true;

    public InitConnection(int port, String password) {
        new Thread(() -> startServer(port, password)).start();
    }
    
    //RUN
    private void startServer(int port, String password) {
        try {
            serverSocket = new ServerSocket(port);
            Robot robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

                String receivedPassword = input.readUTF();
                //VALID
                if (receivedPassword.equals(password)) {
                    output.writeUTF("valid");
                    output.writeUTF(String.valueOf(screenSize.width));
                    output.writeUTF(String.valueOf(screenSize.height));
                    new SendScreen(clientSocket, robot, screenRect);
                    new ReceiveEvents(clientSocket, robot);
                } else {
                    output.writeUTF("Invalid");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //STOP
    public void stopServer() {
        try {
            running = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}