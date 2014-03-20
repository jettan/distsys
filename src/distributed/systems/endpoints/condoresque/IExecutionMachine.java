package distributed.systems.endpoints.condoresque;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IExecutionMachine extends Remote, Serializable {
	
	/**
	 * Register the calling client on this Execution machine.
	 * Communicate the new amount of clients to the Central Manager.
	 */
	public void addClient() throws RemoteException;
	
}