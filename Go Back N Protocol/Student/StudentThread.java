package Student;

import java.io.*;
import java.util.*;
import java.net.*;

public class StudentThread implements Runnable {
	Socket ss;
	DataInputStream din;
	DataOutputStream dout;
	String id;
	Scanner input = new Scanner(System.in);

	StudentThread(Socket ss, DataInputStream din, DataOutputStream dout,
			String id) {
		this.ss = ss;
		this.din = din;
		this.dout = dout;
		this.id = id;
	}

	public void run() {

		try {
			try {
				while (true) {

					din = new DataInputStream(ss.getInputStream());
					dout = new DataOutputStream(ss.getOutputStream());

					int flag = 0;

					String str1 = "";
					System.out
							.println("Type send to send files or receive  to receive files");

					str1 = input.nextLine();
					String receiver_id;
					if (str1.matches("send")) {
						String confirmation;
						dout.writeUTF(str1);
						dout.flush();
						do{
						System.out.println("Enter  receiver id :");
						receiver_id = input.nextLine();

						dout.writeUTF(receiver_id);
						dout.flush();
						confirmation = din.readUTF();
						if(!(confirmation.matches("yes")))
						{
							System.out.println(receiver_id+" is not connected");
						}
						}while(!(confirmation.matches("yes")));
						
						if (confirmation.equals("yes")) {
							flag = 1;
							String filename;
							System.out.println("Enter File Name: ");

							filename = input.nextLine();

							dout.writeUTF(filename);
							dout.flush();

							File file = new File(filename);
							FileInputStream fin = new FileInputStream(file);
							long sz = (int) file.length();

							byte b[] = new byte[4];

							int read;

							dout.writeUTF(Long.toString(sz));
							dout.flush();
							String bit="";
							String bit1="";
							System.out.println("Size: " + sz);
							System.out.println("Buf size: "
									+ ss.getReceiveBufferSize());
							int i=0,k=0,l=0;;
							int j;
							
							int count1=0,extra1=0,checksum=0;
							String bitcpy1="";
							String frame="";
							int seqNo=0;
							int frame_no=0;
							String ack;
							String[] frames=new String[1000];
							
							while ((read = fin.read(b)) != -1) {
//								dout.write(b, 0, read);
//								dout.flush();
								frame_no++;
								seqNo++;
								for(j=0;j<read;j++)
								{
									i=i+8;
									l=l+8;
									bit=bit+String.format("%8s", Integer.toBinaryString(b[j] & 0xFF)).replace(' ', '0');
									bit1=bit1+String.format("%8s", Integer.toBinaryString(b[j] & 0xFF)).replace(' ', '0');

								}
								System.out.println(bit1);
								for(int c=0;c<l;c++)
								{
									bitcpy1=bitcpy1+bit1.charAt(c);
									if(bit1.charAt(c)=='1')
									{
										checksum++;
										count1++;
										
									}
									else
									{
										count1=0;
									}
									
									
									if(count1==5)
									{
										bitcpy1=bitcpy1+"0";
										extra1++;
										count1=0;
									}
									//bitcpy1=bitcpy1+bit1.charAt(c);
									
								}
								String check=Integer.toBinaryString(checksum);
								String SeqNo=Integer.toBinaryString(seqNo);
								if(check.length()<8)
								{
									int req=8-check.length();
									for(int v=0;v<req;v++)
									{
										check="0"+check;
									}
									
									
								}
								
								if(SeqNo.length()<8)
								{
									int req=8-SeqNo.length();
									for(int v=0;v<req;v++)
									{
										SeqNo="0"+SeqNo;
									}
									
									
								}
								System.out.println(check);
								frame="01111110"+"00000000"+SeqNo+"00000000"+bitcpy1+check+"01111110";
								frames[frame_no]=frame;
								

								
								System.out.println(bitcpy1);
								bitcpy1="";
								bit1="";
								l=0;
								k=k+read;
								checksum=0;
								System.out.println(read);
								System.out.println("sending");
								
								
							}
							
							System.out.println("frame_no: "+frame_no);
							
							char[] temp=new char[100];
							System.out.println(frames[frame_no].length());
							for(int inc=0;inc<frames[frame_no].length();inc++)
							{
								if(inc==31)
								{
									temp[inc]='1';
								}
								else{
									temp[inc]=frames[frame_no].charAt(inc);
								}
							}
							System.out.println("Im passed");
							int temp_it=0;
							int fr_it=0;
							int minimum=99999999;
							String pointer=new String(temp);
							frames[frame_no]=pointer;
							for(int frame_it=1;frame_it<=frame_no;frame_it++)
							{
								dout.writeUTF(frames[frame_it]);
								dout.flush();
								System.out.println("sending frame");
								temp_it++;
								if(temp_it==4)
								{
									dout.writeUTF("send");
									dout.flush();
									for(int inner=1;inner<=4;inner++){
										String server_frame=din.readUTF();
										System.out.println("receiving acknowledgment");
										String server_seq=server_frame.substring(8,16);
										
										String server_ack=server_frame.substring(16, 24);
										
										if(!(server_seq.equals(server_ack)))
										{
											System.out.println("There is an error");
											int real_seq=Integer.parseInt(server_seq,2);
											int real_ack=Integer.parseInt(server_ack,2);
											fr_it=Math.min(real_seq, real_ack);
											minimum=Math.min(minimum,fr_it);
											
											
										}
									}
									if(fr_it !=0){
										frame_it=minimum-1;
										fr_it=0;
										minimum=99999;
										} 
									temp_it=0;
									
								}
								else
								{
									dout.writeUTF("No");
									dout.flush();
								}
								
								
							}
							

							System.out.println("complete sending");
						}
						if (flag == 0) {
							System.out.println(receiver_id
									+ " is not connected");
						}
					}

					if (str1.matches("receive")) {

						
						while (!(din.readUTF().matches("start")));
//						 dout.writeUTF("start");
//						 dout.flush();
						// System.out.println("halt");
						String sender_id = din.readUTF();
						String filename = din.readUTF();
						System.out.println(sender_id + " is sending "
								+ filename);

						filename = id + "-" + filename;
						System.out.println("Saving as file: " + filename);

						long sz = Long.parseLong(din.readUTF());
						byte b1[] = new byte[10];
						File dir1 = new File(id);
						File actualFile1 = new File(dir1, filename);
						FileOutputStream fout = new FileOutputStream(
								actualFile1, true);
						long bytes, var = sz;
//						 dout.writeUTF("begin");
//						 dout.flush();
						do {
							bytes = din.read(b1, 0,
									(int) Math.min(b1.length, var));
							fout.write(b1, 0, (int) Math.min(b1.length, var));
							fout.flush();
							System.out.println(bytes);
							var = var - bytes;

						} while (bytes > 0);

						System.out.println("Complete writing");

					}
				}
			}

			finally {
				System.out.println(id + " closed");
				ss.close();
			}

		} catch (Exception X) {
			System.out.println(X);
		}

	}

}
