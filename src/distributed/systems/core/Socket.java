package distributed.systems.core;

import java.util.ArrayList;
import java.util.Collection;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;

public abstract class Socket {

	/**
	 * The registered handlers that can receive messages
	 */
	private Collection<IMessageReceivedHandler> handlers;
	
	/**
	 * Create a new Socket for communicating messages to
	 * serverids
	 */
	public Socket(){
		handlers = new ArrayList<IMessageReceivedHandler>();
	}
	
	/**
	 * Register this socket for a certain serverid
	 * 
	 * @param serverid The serverid to bind to
	 * @throws AlreadyAssignedIDException If the serverid was already claimed
	 */
	public abstract void register(String serverid) throws AlreadyAssignedIDException;
	
	/**
	 * Release our registered serverid
	 */
	public abstract void unRegister();

	/**
	 * Register a handler for received Messages
	 * 
	 * @param handler The handler to pass received Messages to
	 */
	public void addMessageReceivedHandler(IMessageReceivedHandler handler) {
		handlers.add(handler);
	}

	/**
	 * Send a Message to a certain serverid
	 * 
	 * @param reply The message to send
	 * @param origin The URI of the host to send to
	 * @throws IDNotAssignedException If the serverid does not exist
	 */
	public abstract void sendMessage(Message reply, String origin) throws IDNotAssignedException;
}
