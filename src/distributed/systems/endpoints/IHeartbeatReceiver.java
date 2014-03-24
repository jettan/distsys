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
	public void receiveHeartbeat(byte id, Serializable payload) throws RemoteException;
	
	/**
	 * Signal that no further heartbeats will be sent
	 * Should only be called through RMI from a HeartbeatSender
	 * 
	 * @throws RemoteException If the message could not be sent 
	 */
	public void release() throws RemoteException;
	
}
