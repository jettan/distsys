package distributed.systems.endpoints.condoresque;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import distributed.systems.endpoints.EndPoint;

public interface IExecutionMachine extends Remote, Serializable {
	
	/**
	 * Register the calling client on this Execution machine.
	 * Communicate the new amount of clients to the Central Manager.
	 * Return the heartbeat thread to the client
	 */
	public EndPoint addClient(boolean main) throws RemoteException;

}