package distributed.systems.endpoints;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IHeartbeatReceiver extends Remote, Serializable {
	
	/**
	 * Receive a heartbeat with a certain sequence number
	 * 
	 * @param id The sequence number
	 * @throws RemoteException If the message could not be sent
	 */
	public void receiveHeartbeat(byte id) throws RemoteException;
	
	/**
	 * Signal that no further heartbeats will be sent
	 * 
	 * @throws RemoteException If the message could not be sent 
	 */
	public void release() throws RemoteException;
	
}
