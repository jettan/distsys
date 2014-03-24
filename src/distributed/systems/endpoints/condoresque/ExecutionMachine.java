package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.HeartbeatSender;
import distributed.systems.endpoints.IHeartbeatMonitor;

/**
 * This is an actual Execution Machine implementation that runs on an Execution Machine
 * 
 */
public class ExecutionMachine extends HeartbeatSender implements IExecutionMachine, IHeartbeatMonitor{

	private static final long serialVersionUID = 1L;
	
	/**
	 * The list of clients for which we are the main server
	 */
	private List<ReferenceClient> clients = new CopyOnWriteArrayList<ReferenceClient>();
	
	/**
	 * The list of clients for which we are the backup
	 */
	private List<ReferenceClient> backupclients = new CopyOnWriteArrayList<ReferenceClient>(); 
	
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
	 * @param main Whether we are the main server for this client
	 * @throws AlreadyBoundException 
	 * @throws InstantiationException 
	 * @returns The heartbeat endpoint for this client
	 */
	public EndPoint addClient(boolean main) throws RemoteException{
		ReferenceClient out = null;
		String localname = "REFCLIENT_" + clients.size();
		if (main)
			out = addClientToList(clients, localname);
		else
			out = addClientToList(backupclients, localname);
		setPayload(clients.size());
		if (out != null)
			return new EndPoint(localname);
		else
			return null;
	}
	
	/**
	 * Return non-null on success
	 */
	private ReferenceClient addClientToList(List<ReferenceClient> list, String regname) throws RemoteException{
		ReferenceClient out = null;
		try {
			list.add(new ReferenceClient(new EndPoint(regname), this));
			out = list.get(list.size()-1);
		} catch (MalformedURLException e) {
			throw new RemoteException();
		} catch (InstantiationException e) {
			throw new RemoteException();
		} catch (AlreadyBoundException e) {
			throw new RemoteException();
		}
		return out;
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
	public List<ReferenceClient> getClients(){
		return clients;
	}

	@Override
	public void missedBeat(long id) {
		// TODO Uh oh, we got disconnected from the Central Manager
	}

	@Override
	public void missedBeat(EndPoint remote) {
		// TODO Uh oh, a client dropped
		
	}
	
	

}
