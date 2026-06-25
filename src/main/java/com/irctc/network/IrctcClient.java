package com.irctc.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class IrctcClient {

	public void run() throws UnknownHostException, IOException {
		int port = 8010; // same port
		InetAddress address = InetAddress.getByName("localhost"); // local address means IP address of my system
		// here posibility to throw UnknownHostException

		Socket socket = new Socket(address, port); // here thorw IOException

		PrintWriter toSocket = new PrintWriter(socket.getOutputStream(), true);

		BufferedReader fromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		toSocket.println("Hello World from client " + socket.getLocalSocketAddress());
		String line = fromSocket.readLine();

		System.out.println("Response from the socket is :" + line);
		toSocket.close();
		fromSocket.close();
		socket.close();
	}

	public static void main(String[] args) {
		IrctcClient singleThreadedWebServer_Client = new IrctcClient();
		try {
			singleThreadedWebServer_Client.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
