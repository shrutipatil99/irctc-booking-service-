package com.irctc.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class IrctcServer {

	// method
	public void run() throws IOException, UnknownHostException {
		int port = 8010;
		ServerSocket socket = new ServerSocket(port); // its class
		socket.setSoTimeout(20000); // setting time in sec

		while (true) { // i din't add try here bcz i thows exception in void method

			System.out.println("Server is listening on port: " + port); // here throws IO eception so need add under try
																		// catch
			Socket acceptedConnection = socket.accept(); // here waiting for client confirmation but time stop then
															// automatically close.

			System.out.println("Connected to client " + acceptedConnection.getRemoteSocketAddress()); // after
																										// connecting
																										// with client
																										// shows messg

			PrintWriter toClient = new PrintWriter(acceptedConnection.getOutputStream(), true);
			// use PrintWriter to write in output stream and later text covereted to byte
			// form and sent to inputstream

			BufferedReader fromClient = new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
			// here inputstream generate byte code and that byte code PrintWriter combine
			// and provide result

			toClient.println("Hello World from the server"); // here output from client side

		}
	}

	public static void main(String[] args) {
		IrctcServer server = new IrctcServer(); // run () method is not static thats why its not memory, so need to
												// create instance here to create memory
		try {
			server.run(); // calling run()
		} catch (Exception ex) {
			ex.printStackTrace(); // printStackTrace means before exception where where our code run properly that
									// one print
		}

	}
}