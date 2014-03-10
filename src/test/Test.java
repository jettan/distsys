package test;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.example.LocalSocket;

public class Test implements IMessageReceivedHandler{

	public static void main(String[] args) throws RemoteException, AlreadyAssignedIDException, IDNotAssignedException{
		Registry reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
		
		LocalSocket ls = new LocalSocket();
		ls.register("1234");
		ls.addMessageReceivedHandler(new Test());
		
		ls.sendMessage(new Message(), "1234");
		ls.unRegister();
		
		java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		
		System.exit(0);
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		System.out.println("I received a message! :)");
	}
	
}
