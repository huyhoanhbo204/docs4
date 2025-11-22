import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Random;

public class Start {
	private static InitConnection server;
	private static JTextField textID, textPass;

	public static void main(String[] args) {
		JFrame frame = new JFrame("Remote Desktop App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);

		JPanel panel = new JPanel(new GridLayout(5, 2));
		JLabel labelID = new JLabel("ID của bạn:");
		textID = new JTextField();
		JLabel labelPass = new JLabel("Mật khẩu:");
		textPass = new JTextField();
		JLabel labelPartnerID = new JLabel("ID đối tác:");
		JTextField textPartnerID = new JTextField();
		JButton btnStartClient = new JButton("Điều khiển máy tính khác");
		JButton btnResetServer = new JButton("Reset Server");

		panel.add(labelID);
		panel.add(textID);
		panel.add(labelPass);
		panel.add(textPass);
		panel.add(labelPartnerID);
		panel.add(textPartnerID);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btnStartClient);
		buttonPanel.add(btnResetServer);

		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// Lấy địa chỉ IP và khởi động server ban đầu
		initializeServer();

		// Bắt sự kiện Reset Server
		btnResetServer.addActionListener(e -> resetServer());

		// Khởi động client khi nhấn nút
		btnStartClient.addActionListener(e -> startClient(textPartnerID.getText()));
	}

	private static void initializeServer() {
		String ipAddress = getRealIPAddress();
		System.out.println("LOG:" + ipAddress);
		textID.setText(ipAddress);
		resetServer();
	}

	// IP RANDOM
	private static String getRealIPAddress() {
		try {
			for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (!ni.isUp() || ni.isLoopback() || ni.isVirtual())
					continue;

				// lọc ra card WiFi hoặc Ethernet thật
				String name = ni.getDisplayName().toLowerCase();
				if (!(name.contains("wi-fi") || name.contains("wireless") || name.contains("ethernet")))
					continue;

				for (InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
					if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
						String ip = addr.getHostAddress();
						// Bỏ qua các IP ảo như 172.16.x.x, 192.168.56.x (VM, WSL,...)
						if (ip.startsWith("172.") || ip.startsWith("192.168.56.") || ip.startsWith("169.254."))
							continue;
						return ip;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "127.0.0.1"; // fallback
	}

	// RESET CLIENT
	private static void resetServer() {
		if (server != null)
			server.stopServer(); // Dừng server cũ nếu tồn tại
		String password = generateRandomPassword();
		textPass.setText(password);
		server = new InitConnection(6789, password);
		JOptionPane.showMessageDialog(null, "Server đã khởi động lại với mật khẩu: " + password);
	}

	// BẮT ĐẦU CLIENT
	private static void startClient(String partnerID) {
		new Thread(() -> {
			try {
				Socket socket = new Socket(partnerID, 6789);
				Authentication auth = new Authentication(socket);
				auth.setSize(300, 150);
				auth.setVisible(true);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Không thể kết nối đến đối tác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	// TẠO MK RANDOM
	private static String generateRandomPassword() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder password = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 8; i++) {
			password.append(characters.charAt(random.nextInt(characters.length())));
		}
		return password.toString();
	}
}
