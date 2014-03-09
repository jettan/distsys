package distributed.systems.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRMIReceiver extends Remote  {

	public void receiveMessage(Message reply) throws RemoteException;
	
}
