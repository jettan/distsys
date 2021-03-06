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
	private final transient EndPoint centralManager;
	private final transient int ourId;
	
	/**
	 * Connect to the Central Manager and hook into our personal heartbeat thread.
	 * Also open a port for clients to connect to
	 * 
	 * @param centralManager The central manager to connect to
	 * @param local Our local EndPoint for clients to connect to
	 */
	public ExecutionMachine(EndPoint centralManager, EndPoint local) throws MalformedURLException,
			RemoteException, NotBoundException, InstantiationException, AlreadyBoundException {
		super(local);
		
		this.centralManager = centralManager;
		
		endpoint = local;
		endpoint.open((IExecutionMachine) UnicastRemoteObject.exportObject(this, 0));
		
		ourId = ((ICentralManager) centralManager.connect()).requestMachineID();
		String ourName = "REFEXMACHINE_" + ourId;
		super.connect(new EndPoint(centralManager.getHostName(), centralManager.getPort(), ourName));
	}
	
	public int getOurId(){
		return ourId;
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
	public synchronized EndPoint addClient(boolean main) throws RemoteException{
		ReferenceClient out = null;
		String localname = endpoint.getRegistryName() + "_REFCLIENT_" + clients.size();
		if (!main)
			localname = endpoint.getRegistryName() + "_REFCLIENT_b" + backupclients.size();
		
		if (main)
			out = addClientToList(clients, localname);
		else
			out = addClientToList(backupclients, localname);
		
		setPayload(getTotalClients());
		if (out != null)
			return new EndPoint(endpoint.getHostName(), endpoint.getPort(), localname);
		else
			return null;
	}
	
	/**
	 * Return non-null on success
	 */
	private ReferenceClient addClientToList(List<ReferenceClient> list, String regname) throws RemoteException{
		ReferenceClient out = null;
		try {
			list.add(new ReferenceClient(new EndPoint(endpoint.getHostName(), endpoint.getPort(), regname), this));
			out = list.get(list.size()-1);
		} catch (Exception e) {
			e.printStackTrace();
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
	public synchronized void removeClient(EndPoint client){
		clients.remove(client);	// If this doesn't remove anything, clients.size() will still be correct!
		backupclients.remove(client);
		setPayload(getTotalClients());
	}
	
	/**
	 * @return The total amount of registered (backup) clients
	 */
	public int getTotalClients(){
		return clients.size() + backupclients.size();
	}
	
	@Override
	public void fakeCrash(){
		super.fakeCrash();
		for (ReferenceClient rc : clients){
			try {
				rc.release();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		for (ReferenceClient rc : backupclients){
			try {
				rc.release();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public EndPoint getBattlefieldReg(){
		return new EndPoint(endpoint.getHostName(), endpoint.getPort(), endpoint.getRegistryName()+"/BATTLEFIELD");
	}
	
	public EndPoint nextServerBattlefield(){
		EndPoint raw = getBattlefieldReg();
		try {
			raw = ((ICentralManager) centralManager.connect()).nextMachine("EXECUTION_MACHINE_" + ourId);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new EndPoint(raw.getHostName(), raw.getPort(), raw.getRegistryName()+"/BATTLEFIELD");
	}

	@Override
	public void missedBeat(long id) {
		// TODO Uh oh, we got disconnected from the Central Manager
	}

	@Override
	public void missedBeat(EndPoint remote) {
		// TODO Uh oh, a client dropped
		System.err.println("DROPPED CLIENT " + remote);
		removeClient(remote);
	}
	
	

}
