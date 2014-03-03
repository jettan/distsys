package distributed.systems.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.example.LocalSocket;

public abstract class Socket {

	/**
	 * The registered handlers that can receive messages
	 */
	private Collection<IMessageReceivedHandler> handlers;
	
	/**
	 * All of the bound sockets
	 */
	private static ConcurrentHashMap<String, Socket> registeredSockets;
	
	/**
	 * Create a new Socket for communicating messages to
	 * serverids
	 */
	public Socket(){
		handlers = new ArrayList<IMessageReceivedHandler>();
		registeredSockets = new ConcurrentHashMap<String, Socket>();
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
	public void sendMessage(Message reply, String origin) throws IDNotAssignedException{
		if ("localsocket".equals(getProtocol(origin))){
			new LocalSocket().sendMessage(reply, origin);
		} else {
			throw new IDNotAssignedException();
		}
	}
	
	public abstract void receiveMessage(Message reply);
	
	/**
	 * Decompoase a uri into the uri protocol
	 * 
	 * @param uri The uri to decompose
	 * @return The corresponding protocol
	 */
	public String getProtocol(String uri){
		return uri.substring(0, uri.indexOf(':'));
	}
	
	/**
	 * Decompoase a uri into the server id
	 * 
	 * @param uri The uri to decompose
	 * @return The corresponding server id
	 */
	public String getServerID(String uri){
		return uri.substring(uri.lastIndexOf('/')+1, uri.length());
	}
	
	/**
	 * Try to register ourselves as a certain server
	 * 
	 * @param server The serverid to bind to
	 * @param s The socket to register
	 * @throws AlreadyAssignedIDException If this id was already registered
	 */
	protected void claim(String server, Socket s) throws AlreadyAssignedIDException{
		if (registeredSockets.contains(server))
			throw new AlreadyAssignedIDException();
		registeredSockets.put(server, s);
	}
	
	/**
	 * Release a local lease on a server name
	 * 
	 * @param server The server id to release
	 */
	protected void release(String server){
		registeredSockets.remove(server);
	}
	
}
