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
		
		// CLIENT code
		LocalSocket clt = new LocalSocket();
		clt.register("5678");
		clt.addMessageReceivedHandler(new Test());
		
		// SERVER code
		srv.sendMessage(new Message(), "5678");
		
		// shared
		clt.unRegister();
		srv.unRegister();
	
		// shared
		java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		System.exit(0);
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		System.out.println("I received a message! :)");
	}
	
}
