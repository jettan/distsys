package distributed.systems.core;

import java.util.ArrayList;
import java.util.Collection;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;

public class Socket {

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
	public void register(String serverid) throws AlreadyAssignedIDException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Release our registered serverid
	 */
	public void unRegister() {
		// TODO Auto-generated method stub
		
	}

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
	 * @param origin The serverid to send to
	 * @throws IDNotAssignedException If the serverid does not exist
	 */
	public void sendMessage(Message reply, String origin) throws IDNotAssignedException {
		// TODO Auto-generated method stub
		
	}

	

}
