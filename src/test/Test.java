package test;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.example.LocalSocket;

public class Test implements IMessageReceivedHandler, Serializable{

	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) throws RemoteException, AlreadyAssignedIDException, IDNotAssignedException{
		// shared
		Registry reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
		
		// SERVER code
		LocalSocket srv = new LocalSocket();
		srv.register("1234");
		Handler srvHandler = new Handler(srv, "5678"); //Upon receipt of a message, send one back
		srv.addMessageReceivedHandler(srvHandler);
		
		// CLIENT code
		LocalSocket clt = new LocalSocket();
		clt.register("5678");
		clt.addMessageReceivedHandler(new Test());

		Message initMsg = new Message();
		initMsg.put("Hello", "World!");
		clt.sendMessage(initMsg, "1234");
		
		// shared
		clt.unRegister();
		srv.unRegister();
	
		// shared
		java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		System.exit(0);
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		System.out.println("Client received " + message);
	}
	
	public static class Handler implements IMessageReceivedHandler, Serializable{

		private static final long serialVersionUID = 1L;
		
		private LocalSocket returner;
		private String retId;
		
		public Handler(LocalSocket ret, String origin){
			this.returner = ret;
			this.retId = origin;
		}
		
		@Override
		public void onMessageReceived(Message message) throws RemoteException {
			try {
				Message ret = new Message();
				ret.put("Test", "123");
				
				System.out.println("Server received " + message);
				System.out.println("Returning " + ret);
				
				returner.sendMessage(ret, retId);
			} catch (IDNotAssignedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
