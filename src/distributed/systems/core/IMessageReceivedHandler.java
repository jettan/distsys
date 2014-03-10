package distributed.systems.core;

import java.rmi.*;

/**
 * Interface for classes that can handle incoming Message
 * objects.
 */
public interface IMessageReceivedHandler extends Remote {

	/**
	 * Receive a message
	 * 
	 * @param message The message that was sent to us
	 */
	public void onMessageReceived(Message message) throws java.rmi.RemoteException;
	
}
