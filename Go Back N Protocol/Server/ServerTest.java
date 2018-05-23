package Server;

import java.io.*;
import java.net.*;
import java.util.*;

import Server.Server.Client;

public class ServerTest implements Runnable {

	Socket sock;
	String stdId;
	DataInputStream din;
	DataOutputStream dout;

	Vector<Client> user;
	String str;

	ServerTest(Socket sock, String stdId, DataInputStream din,
			DataOutputStream dout) {
		this.sock = sock;
		this.stdId = stdId;
		this.din = din;
		this.dout = dout;

	}

	public void run() {
		try {
			try {

				while (true) {
					din = new DataInputStream(sock.getInputStream());
					dout = new DataOutputStream(sock.getOutputStream());
					String str = "";
					str = din.readUTF();
					String reciever_id;
					if (str.matches("send")) {
						int fl = 0;
						do{
						reciever_id = din.readUTF();

						
						for (Client c : Server.user) {
							if (reciever_id.matches(c.stdId)) {
								fl = 1;
								dout.writeUTF("yes");
								dout.flush();
								break;
							}
						}

						if (fl == 0) {
							dout.writeUTF("no");
							dout.flush();
						}
						}while(fl !=1);
						
						
						String filename = "";
						filename = din.readUTF();
						System.out.println(stdId + " is sending file :"
								+ filename);

						System.out.println("Receiving file: " + filename);
						filename = stdId + "-" + filename;
						System.out.println("Saving as file: " + filename);
						//
						long sz = Long.parseLong(din.readUTF());
						System.out.println("File Size: " + (sz / (1024 * 1024))
								+ " MB");

						byte b[] = new byte[4];
						System.out.println("Receiving file..");
//						File dir = new File("server");
//						File actualFile = new File(dir, filename);
//						FileOutputStream fos = new FileOutputStream(actualFile,
					//			true);
//						long bytesRead, var = sz;
//						int num_bit=(int)var*8;
//						int k=0;
//						String bit1="";
						String bit2="";
						String[] chunks=new String[1000];
						String[] server_seq=new String[1000];
						String[] All_data=new String[1000];
						String[] All_real_data=new String[1000];
						int error=0;
						int ack=0;
						int frame_num=1;
						int temp_it=1;
						do {
							
//							fos.write(b, 0, (int) Math.min(b.length, var));
//							fos.flush();
							//var = var - bytesRead;
							//k=(int) (k+bytesRead);
							

							bit2=din.readUTF();
							System.out.println("got it yes "+bit2);
							chunks[frame_num]=bit2;
							//num_bit=num_bit-bit2.length();
							//bit1=bit1+bit2;
							
							
							String frame=DeFraming(bit2);
							System.out.println(frame);
							System.out.println("passed frame");
							String type=frame.substring(0, 8);
							System.out.println(type);
							System.out.println("passed type");
							
							String SeqNo=frame.substring(8,16 );
							System.out.println(SeqNo);
							server_seq[frame_num]=SeqNo;
							int index=Integer.parseInt(SeqNo,2);
							System.out.println("Seq no: "+index);
							temp_it=Math.min(temp_it, index);
							frame_num=Math.min(frame_num,index);
							
							
							String ackNo=frame.substring(16, 24);
							System.out.println(ackNo);
							
							ack=Integer.parseInt(ackNo,2);
							
							String data=frame.substring(24,frame.length()-8);
							System.out.println(data);
							All_data[index]=data;
							
							String real_data=DeStuffing(data);
							All_real_data[index]=real_data;
							System.out.println(real_data);
							System.out.println("passed destuff");
							String checksum=frame.substring(frame.length()-8);
							System.out.println(checksum);
							
							Check(data,checksum);
							System.out.println("passed chcksum");
							if(din.readUTF().matches("send"))
							{
								String frame_send="";
								int i;
								int for_it=0;
								for(i=temp_it;i<=frame_num;i++)
								{
									for_it++;
									error++;
									String acknowledge=Integer.toBinaryString(i);
									System.out.println("sending acknowledgent");
									if(acknowledge.length()<8)
									{
										int req=8-acknowledge.length();
										for(int v=0;v<req;v++)
										{
											acknowledge="0"+acknowledge;
										}
										
										
									}
									
									if(error==2) {acknowledge="00000011";}
									frame_send="00000001"+server_seq[i]+acknowledge+All_data[i];
									dout.writeUTF(frame_send);
									dout.flush();
								}
								if(for_it < 4)
								{
									int remaining_frame=4-for_it;
									
									for(int f=1;f<=remaining_frame;f++)
									{
										String acknowledge=Integer.toBinaryString(i+f-1);
										System.out.println("sending dummy acknowledgent");
										if(acknowledge.length()<8)
										{
											int req=8-acknowledge.length();
											for(int v=0;v<req;v++)
											{
												acknowledge="0"+acknowledge;
											}
											
											
										}
										frame_send="00000001"+"01111111"+acknowledge+"0000000110010101010101";
										dout.writeUTF(frame_send);
										dout.flush();
									}
								}
								
								temp_it=i;
							}
							System.out.println("recieving");
							
							System.out.println("frame_no"+frame_num);
							frame_num++;

						} while (ack !=1);

						System.out.println("Complete");
						//System.out.println(bit1);
						//String bit=din.readUTF();
						
						int total=0;
						String file="";
						for(int data_it=1;data_it<frame_num;data_it++)
						{
							total+=All_real_data[data_it].length();
							file=file+All_real_data[data_it];
						}
						System.out.println(file);
						System.out.println(total);
						byte a[]=new byte[1000];
						int m=0,n=0,i=0,j;
						
						for(j=0;j<(total/8);j++)
						{
							n=m+8;
							a[j]=Byte.parseByte(file.substring(m,n), 2);
							m=m+8;
						}
						
						File dir2= new File("server");
						dir2.mkdirs();
						File actualFile2 = new File(dir2, filename);
						FileOutputStream fos1 = new FileOutputStream(actualFile2,
								true);
						fos1.write(a, 0,(total/8) );
						
						fos1.flush();
						
						
						user = Server.getvec();
						Socket sock1 = new Socket();
						for (Client c : user) {
							if (reciever_id.matches(c.stdId)) {
								sock1 = this.sock;
								this.sock = c.socket;

								din = new DataInputStream(sock.getInputStream());
								dout = new DataOutputStream(
										sock.getOutputStream());
								System.out.println("Im in");
								// String str1=din.readUTF();
								// if(str1.matches("recieve")){
								dout.writeUTF("start");
								dout.flush();

								// while(!(din.readUTF() != null));
								System.out.println(stdId + " is sending file :"
										+ filename);

								dout.writeUTF(stdId);
								dout.flush();

								dout.writeUTF(filename);
								dout.flush();
								System.out.println("Sending file: " + filename);

								//
								dout.writeUTF(Long.toString(sz));
								dout.flush();

								System.out.println("File Size: "
										+ (sz / (1024 * 1024)) + " MB");

								byte b1[] = new byte[10];
								System.out.println("Sending file..");
								File dir1 = new File("server");
								File actualFile1 = new File(dir1, filename);
								FileInputStream fin = new FileInputStream(
										actualFile1);
								int read;

							    //while(!(din.readUTF().matches("begin")));
								while ((read = fin.read(b1)) != -1) {
									dout.write(b1, 0, read);
									dout.flush();
									System.out.println(read);
								}
								System.out.println("Complete servertest");
								break;
							}

						}
						this.sock = sock1;

					}

				}
			} finally {
				sock.close();
			}

		} catch (Exception X) {
			System.out.println(X);
		}
	}
	
	
	public String DeFraming(String frame)
	{
		 
		String[] temp=frame.split("01111110");
		
		return temp[1];
	}
	
	public String DeStuffing(String data)
	{
		
		String real_data="";
		int count=0;
		for(int i=0;i<data.length();i++)
		{
			real_data=real_data+data.charAt(i);
			if(data.charAt(i)=='1')
			{
				count++;
			}
			else{
				count=0;
			}
			if(count==5)
			{
				i++;
				count=0;
			}
		}
		return real_data;
		
	}
	
	
	public void Check(String data,String check)
	{
		int checksum;
		checksum=Integer.parseInt(check,2);
		
		int count=0;
		
		for(int i=0;i<data.length();i++)
		{
			
			if(data.charAt(i)=='1')
			{
				count++;
			}
			
		}
		
		if(count==checksum)
		{
			System.out.println("Thre is no error");
			
		}
		else{
			System.out.println("Data is not right");
		}
	}
	
}
