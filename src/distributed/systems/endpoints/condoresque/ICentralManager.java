package distributed.systems.endpoints.condoresque;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import distributed.systems.endpoints.EndPoint;

public interface ICentralManager extends Remote, Serializable {
	
	/**
	 * A client requests deployment on an execution server.
	 * It received a server allocation (main + backup) or null
	 * if no allocation could be made.
	 */
	public Allocation requestExecution() throws RemoteException;
	
	/**
	 * A client has lost connection to a certain Execution Machine.
	 * Feed him a replacement, make sure he doesn't already have
	 * this machine assigned to him though (new machine != ep)
	 */
	public EndPoint requestReplacement(EndPoint ep) throws RemoteException;
	
	/**
	 * Called by Execution machines that want to get an ID
	 * 
	 * @return A new id
	 */
	public int requestMachineID() throws RemoteException;
	
}