package distributed.systems.endpoints.condoresque;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICentralManager extends Remote, Serializable {
	
	/**
	 * A client requests deployment on an execution server.
	 * It received a server allocation (main + backup) or null
	 * if no allocation could be made.
	 */
	public Allocation requestExecution() throws RemoteException;
	
	/**
	 * Called by Execution machines that want to get an ID
	 * 
	 * @return A new id
	 */
	public int requestMachineID() throws RemoteException;
	
}