package com.irctc.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class IrctcMultiTreadServer {
	
		//functional interface is consumer
	    public Consumer<Socket> getConsumer() {
	        return (clientSocket) -> {				// clientSocket is lambda
	            try (PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true)) {
	                toSocket.println("Hello from server " + clientSocket.getInetAddress());
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        };
	    }
	    
	public static void main(String[] args) {
		
	        int port = 8010;
	        IrctcMultiTreadServer server = new IrctcMultiTreadServer();
	        
	        try {
	            ServerSocket serverSocket = new ServerSocket(port);
	            serverSocket.setSoTimeout(70000);
	            System.out.println("Server is listening on port " + port);
	            while (true) {
	                Socket clientSocket = serverSocket.accept();		//here new socket created by main server socket for each to rent response to  client  
	                
	                // Create and start a new thread for each client
	                Thread thread = new Thread(() -> server.getConsumer().accept(clientSocket));
	                thread.start();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    
	}
