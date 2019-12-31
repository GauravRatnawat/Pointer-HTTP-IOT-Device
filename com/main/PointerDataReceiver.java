package com.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.tools.ant.taskdefs.condition.Socket;


public class PointerDataReceiver extends Thread {  

	// public static DataQueue queue = null;
	String strTempMsg = "";
	boolean tflag = false;
	int fInsert = 0;
	public String day, month, year, FileName1 = "";
	private InetAddress hostAddress;
	private int port;
	int portNum;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	public static ArrayList<SocketChannel> newSocketList;
//	public static HashMap<String, SocketChannel> socketList;

	public PointerDataReceiver(int Port) 
	{
		this.port = Port;
		try 
		{
			this.selector = this.initSelector();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*private String toHex(String arg) {
		System.out.println("received in toHex: " + arg);
		String out = String.format("%01x", new BigInteger(1, arg.getBytes()));
		System.out.println("return from toHex: " + out);
		return out;
	}*/

	@Override
	@SuppressWarnings("SleepWhileHoldingLock")
	public void run() 
	{
		System.out.println("Pointer start for " + port);
		newSocketList = new ArrayList<>();
//		socketList = new HashMap<>();
		while (true) 
		{

			try 
			{
				this.selector.selectNow();
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) 
				{
					SelectionKey key = (SelectionKey) selectedKeys.next();

					selectedKeys.remove();
					if (!key.isValid()) 
					{
						continue;
					}
					if (key.isAcceptable()) 
					{
						this.accept(key);
					} 
					else if (key.isReadable()) 
					{
						this.read(key);
					}
				}
				Thread.sleep(1000);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}



	private void accept(SelectionKey key) 
	{
		SocketChannel socketChannel = null;
		try 
		{
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);

			System.out.println("Socket channel remote address : " + socketChannel.getRemoteAddress());
			System.out.println("Socket channel local address : " + socketChannel.getLocalAddress());
			socketChannel.register(this.selector, SelectionKey.OP_READ);

			if (!newSocketList.contains(socketChannel)) 
			{
				newSocketList.add(socketChannel);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			// Main.gc();
			// System.out.println("Inside accept method " + e);
		}
	}



	
	private void read(SelectionKey key) 
	{

		System.out.println("IN read");
		try 
		{
			SocketChannel socketChannel = (SocketChannel) key.channel();
			portNum = socketChannel.socket().getLocalPort();
			this.readBuffer.clear();
			int numRead;
			try 
			{
				numRead = socketChannel.read(this.readBuffer);

			} 
			catch (Exception e1) 
			{
				socketChannel.socket().setSoLinger(true, 0);
				socketChannel.close();
				key.cancel();
				newSocketList.remove(socketChannel);
//				errorfilecreation("Data Listener Read initial " + e1.toString());
				return;
			}
			if (numRead == -1) 
			{
				try 
				{
					if (newSocketList.contains(socketChannel)) 
					{
						socketChannel.socket().setSoLinger(true, 0);
						socketChannel.close();
						newSocketList.remove(socketChannel);
					}
					key.cancel();

				} 
				catch (Exception ee) 
				{
					ee.printStackTrace();
				}
			} 
			else 
			{
				if (readBuffer != null) 
				{
					readBuffer.flip();
					byte[] array = new byte[10000];
					while (readBuffer.hasRemaining()) 
					{
						String stOutput = "";
						String rawOutputData = "";
						String[] packets;
						String strSimNo = "";
						try 
						{
							int n = readBuffer.remaining();
							readBuffer.get(array, 0, n);
							for (int i = 0; i < n; i++) 
							{
								String hex = "" + Integer.toHexString(array[i]);
								byte rawDataa = array[i];
								if (hex.length() > 2) 
								{
									hex = hex.substring(6, hex.length());
								}
								if (hex.length() < 2) 
								{
									hex = "0" + hex;
								}
								stOutput += hex.toUpperCase();
								rawOutputData += rawDataa;
							}
						} 
						catch (Exception ee) 
						{

						}

						stOutput = stOutput.trim();

						System.out.println("Packet received: "+stOutput);
						int index = 0;
						while (index < stOutput.length()) {
							String substring=null;
							substring = stOutput.substring(index, Math.min(index + 140,stOutput.length()));
							index += 140;
							if (substring.length() == 140) 
							{
							if (substring.indexOf("4D434750") != -1) 
							{

								GenerateAckCommand ack = new GenerateAckCommand();
								String commandACK = ack.createAckCommand(substring,
										Integer.parseInt(substring.substring(22, 24), 16));
								System.out.println("Ack gen: "+commandACK);
								String da = getData(commandACK);
								System.out.println("After converting in INT: "+da);
								socketChannel.register(this.selector, SelectionKey.OP_WRITE);

								if (!newSocketList.contains(socketChannel)) 
								{
									newSocketList.add(socketChannel);
								}
								ByteBuffer buf=null;
								 buf = ByteBuffer.wrap((da).getBytes("UTF8"));
//								System.out.println(new String(buf.array(), "Raw"));
								socketChannel.write(buf);
								System.out.println("Data send");
								socketChannel.register(this.selector, SelectionKey.OP_READ);
							}
						}
					}
					}
				}
			}

		} 
		catch (Exception e) 
		{
			try 
			{
//				errorfilecreation("Data Listener Read " + e.toString());
			} 
			catch (Exception ex) 
			{
				Logger.getLogger(PointerDataReceiver.class.getName()).log(Level.SEVERE, null, ex);
			}
			return;
		}

	}



	public static String getData(String hex) 
	{

		StringBuilder output = new StringBuilder();
		for (int i = 0; i < hex.length(); i += 2) 
		{
			String str = hex.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}

		return output.toString();
	}



	public void filecreation(String StrResponse) 
	{
		try 
		{
			Calendar calendar = Calendar.getInstance();
			day = "" + calendar.get(5);
			month = "" + (calendar.get(2) + 1);
			year = "" + calendar.get(1);
			if (day.length() == 1) 
			{
				day = "0" + day;
			}
			if (month.length() == 1) 
			{ 
				month = "0" + month;
			}
			String currDate = day + month + year;
			File file1 = new File("E:/log_TECL/" + currDate);
			if (!file1.exists()) 
			{
				file1.mkdirs();
			}
			FileName1 = "Trinity" + portNum + "-" + month + day + "Complete.log";
			FileOutputStream DebugFile = new FileOutputStream(file1.getAbsolutePath() + "/" + FileName1, true);
			PrintStream DebugDataStream = new PrintStream(DebugFile);
			if (!StrResponse.equals("")) 
			{
				DebugDataStream.println("Data Recieved :" + StrResponse + " at : "
						+ new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
			}
			DebugDataStream.close();
			DebugFile.close();
		} 
		catch (IOException ie) 
		{
			ie.printStackTrace();
			// Main.gc();
		}
	}



//	public void errorfilecreation(String StrResponse) 
//	{
//		try 
//		{
//			Calendar calendar = Calendar.getInstance();
//			day = "" + calendar.get(5);
//			month = "" + (calendar.get(2) + 1);
//			year = "" + calendar.get(1);
//			if (day.length() == 1) 
//			{
//				day = "0" + day;
//			}
//			if (month.length() == 1) 
//			{
//				month = "0" + month;
//			}
//			String currDate = day + month + year;
//			File file1 = new File("/log_TECL/" + currDate);
//			if (!file1.exists()) 
//			{
//				file1.mkdirs();
//			}
//			FileName1 = "TrinityError-" + month + day + ".log";
//			FileOutputStream DebugFile = new FileOutputStream(file1.getAbsolutePath() + "/" + FileName1, true);
//
//			PrintStream DebugDataStream = new PrintStream(DebugFile);
//			if (!StrResponse.equals("")) 
//			{
//				DebugDataStream.println(StrResponse);
//			}
//			DebugDataStream.close();
//			DebugFile.close();
//		} 
//		catch (IOException ie) 
//		{
//			ie.printStackTrace();
//			// Main.gc();
//		}
//	}
//	
//	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private Selector initSelector() 
	{
		Selector socketSelector = null;
		try 
		{
			socketSelector = SelectorProvider.provider().openSelector();
			// System.out.println("Create a new server socket channel ");
			this.serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			InetSocketAddress isa = new InetSocketAddress( this.port);
			serverChannel.socket().bind(isa);
			hostAddress = InetAddress.getLocalHost();
			System.out.println(hostAddress);
			serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);  

		} 
		catch (Exception e) 
		{
			e.printStackTrace();

			try  
			{
				serverChannel.close();
			} 
			catch (Exception ee) 
			{
				ee.printStackTrace();
			}
		}
		return socketSelector;
	}



}