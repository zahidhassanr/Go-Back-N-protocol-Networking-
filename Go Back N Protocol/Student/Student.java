package Student;

import java.io.*;
import java.util.*;
import java.net.*;

import Server.Server;
import Server.Server.Client;

public class Student {

	public static Server server;

	public static void main(String[] args) throws IOException {

		Socket ss = new Socket("localhost", 5000);
		DataInputStream din = new DataInputStream(ss.getInputStream());
		DataOutputStream dout = new DataOutputStream(ss.getOutputStream());
		String id;
		System.out.println("Enter you student id");
		Scanner input = new Scanner(System.in);
		id = input.nextLine();
	
		
		dout.writeUTF(id);
		dout.flush();
		
		if(din.readUTF().matches("yes"))
		{
			System.out.println("you have already logged in");
			System.exit(0);
		}
		else {
		System.out.println("You are logged in as " + id);
		File dir = new File(id);
		dir.mkdirs();

		
		StudentThread std = new StudentThread(ss, din, dout, id);
		Thread x = new Thread(std);
		x.start();
		}

	}

}
