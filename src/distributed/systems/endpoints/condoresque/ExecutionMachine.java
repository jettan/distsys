package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.HeartbeatSender;
import distributed.systems.endpoints.RMINamingURLParser;

/**
 * This is an actual Execution Machine implementation that runs on an Execution Machine
 * 
 * TODO This should also receive heartbeats from clients
 */
public class ExecutionMachine extends HeartbeatSender implements IExecutionMachine{

	private static final long serialVersionUID = 1L;
	
	private Collection<EndPoint> clients = new CopyOnWriteArrayList<EndPoint>(); 
	
	private final transient EndPoint endpoint;
	
	/**
	 * Connect to the Central Manager and hook into our personal heartbeat thread.
	 * Also open a port for clients to connect to
	 * 
	 * @param centralManager The central manager to connect to
	 * @param local Our local EndPoint for clients to connect to
	 */
	public ExecutionMachine(EndPoint centralManager, EndPoint local) throws MalformedURLException,
			RemoteException, NotBoundException, InstantiationException, AlreadyBoundException {
		// We have to construct our super on the first line, so yeah
		super(new EndPoint(centralManager.getHostName(), centralManager.getPort(), "REFEXMACHINE_" + ((ICentralManager) centralManager.connect()).requestMachineID()));
		
		endpoint = local;
		endpoint.open((IExecutionMachine) UnicastRemoteObject.exportObject(this, 0));
	}
	
	/**
	 * Register the calling client on this Execution machine.
	 * Communicate the new amount of clients to the Central Manager.
	 * 
	 * @param client The client to add
	 */
	public void addClient() throws RemoteException{
		try {
			clients.add(RMINamingURLParser.fromURL(RemoteServer.getClientHost()));
		} catch (MalformedURLException e) {
			throw new RemoteException();
		} catch (ServerNotActiveException e) {
			throw new RemoteException();
		}
		setPayload(clients.size());
	}
	
	/**
	 * Unregister a client on this Execution machine (for example after a detected drop).
	 * Communicate the new amount of clients to the Central Manager.
	 * 
	 * @param client The client to remove
	 */
	public void removeClient(EndPoint client){
		clients.remove(client);	// If this doesn't remove anything, clients.size() will still be correct!
		setPayload(clients.size());
	}
	
	/**
	 * @return The list of registered clients
	 */
	public Collection<EndPoint> getClients(){
		return clients;
	}

	@Override
	public void missedBeat(long id) {
		// TODO Uh oh, we got disconnected from the Central Manager
	}
	
	

}
