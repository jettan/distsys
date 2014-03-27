package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import distributed.systems.endpoints.EndPoint;


/**
 * This is an actual Central Manager implementation that runs on the Central Manager
 */
public class CentralManager implements ICentralManager{

	private static final long serialVersionUID = 1L;

	public static String serverID = "centralmanager";
	
	private transient List<ReferenceExecutionMachine> machines = new ArrayList<ReferenceExecutionMachine>(); 
	
	private transient AtomicInteger lastid = new AtomicInteger(-1);

	public CentralManager(EndPoint local) throws MalformedURLException, RemoteException, InstantiationException, AlreadyBoundException{
		local.open((ICentralManager) UnicastRemoteObject.exportObject(this, 0));
	}
	
	/**
	 * Called by a client to retrieve a server allocation (main + backup)
	 */
	@Override
	public Allocation requestExecution() throws RemoteException {
		Allocation allocation = null;
		
		//No two clients can ever be deployed at the same time
		synchronized(machines){
			Collections.sort(machines);
			try {
				EndPoint main = machines.get(0).getRemoteHost();
				EndPoint backup = machines.get(1).getRemoteHost();
				allocation = new Allocation(main, backup);
			} catch (Exception e) {
				//Failed to allocate
				// - Not enough machines
				// - Machines not connected
				// - Some other horrible error
			}
		}
		
		return allocation;
	}

	/**
	 * Initialize a certain amount of execution server connections
	 * 
	 * NOTE: This is what the execution machines connect to at their own leisure.
	 * These need to be in place before the execution machines start. 
	 * 
	 * @param amount The amount of servers to deploy
	 */
	public void initializeServerInterfaces(int amount){
		for (int i = 0; i < amount; i++){
			try {
				machines.add(new ReferenceExecutionMachine(new EndPoint("REFEXMACHINE_" + i)));
			} catch (MalformedURLException | RemoteException
					| InstantiationException | AlreadyBoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called by execution machines to get an id assigned
	 */
	@Override
	public int requestMachineID() throws RemoteException {
		int id = lastid.incrementAndGet();
		return id;
	}
}
