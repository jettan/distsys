package test;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.example.LocalSocket;

public class Test implements IMessageReceivedHandler, Serializable{

	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) throws RemoteException, AlreadyAssignedIDException, IDNotAssignedException{
		// shared
		Registry reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
		
		// SERVER code
		//Socket srv = new SynchronizedSocket(new LocalSocket());
		Handler srvHandler = new Handler("5678"); //Upon receipt of a message, send one back
		//srv.addMessageReceivedHandler(srvHandler);
		
		// CLIENT code
		Socket clt = new SynchronizedSocket(new LocalSocket());
		clt.register("5678");
		clt.addMessageReceivedHandler(new Test());

		Message initMsg = new Message();
		initMsg.put("Hello", "World!");
		clt.sendMessage(initMsg, "1234");
		
		// shared
		clt.unRegister();
		//srv.unRegister();
	
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
		
		private Socket returner;
		private String retId;
		
		public Handler(String origin) throws AlreadyAssignedIDException{
			try {
				this.returner = new LocalSocket();
				returner.register("1234");

			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.retId = origin;
			returner.addMessageReceivedHandler(this);
			
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
