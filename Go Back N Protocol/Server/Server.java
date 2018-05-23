package Server;

import java.util.*;
import java.io.*;
import java.net.*;

import Student.Student;

public class Server {

	public static Vector<Client> user = new Vector<Client>();

	Student s = new Student();

	public static void main(String[] args) throws IOException {

		File dir = new File("server");
		dir.mkdirs();
		ServerSocket ss = new ServerSocket(5000);
		System.out.println("Waiting for request");
		Socket dummy = null;
		Client dumy = new Client("Dumy", dummy);
		user.add(dumy);
		while (true) {
			Socket client = ss.accept();
			DataInputStream din = new DataInputStream(client.getInputStream());
			DataOutputStream dout = new DataOutputStream(
					client.getOutputStream());

			String stdId = "";
			stdId = din.readUTF();
			for (Client c : user) {
				if (stdId.matches(c.stdId)) {
					dout.writeUTF("yes");
					dout.flush();
					break;
				}
			}
			dout.writeUTF("no");
			dout.flush();
			Client c = new Client(stdId, client);
			int flag = 0;

			for (Client cl : user) {
				if (stdId.matches(cl.stdId)) {
					flag = 1;
					System.out.println("matched");

					break;
				}

			}
			if (flag == 0) {
				user.add(c);
				ServerTest stud = new ServerTest(client, stdId, din, dout);
				Thread x = new Thread(stud);
				x.start();

			}

			stdId = "";

			for (Client cl : user) {
				if (!(cl.stdId.equals("Dumy"))) {
					System.out.println(cl.stdId + " is connected");
				}
			}
		}

	}

	public static Vector<Client> getvec() {
		return user;
	}

	public static class Client {
		String stdId;
		Socket socket;

		Client(String stdId, Socket socket) {
			this.stdId = stdId;
			this.socket = socket;

		}
	}

}
