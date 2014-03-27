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
import distributed.systems.endpoints.IHeartbeatMonitor;


/**
 * This is an actual Central Manager implementation that runs on the Central Manager
 */
public class CentralManager implements ICentralManager, IHeartbeatMonitor{

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
				//Randomly distribute main and backup
				if (Math.random() <= 0.5)
					allocation = new Allocation(main, backup);
				else
					allocation = new Allocation(backup, main);
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
	 * Called by a client to retrieve a new partial allocation where
	 * the new host is not equal to ep.
	 */
	@Override
	public EndPoint requestReplacement(EndPoint ep) throws RemoteException {
		//No two clients can ever be deployed at the same time
		synchronized(machines){
			Collections.sort(machines);
			try {
				EndPoint first = machines.get(0).getRemoteHost();
				if (!first.equals(ep))
					return first;
				return machines.get(1).getRemoteHost();
			} catch (Exception e) {
				//Failed to allocate
				// - Not enough machines
				// - Machines not connected
				// - Some other horrible error
			}
		}
		
		return null;
	}

	/**
	 * Called by execution machines to get an id assigned
	 */
	@Override
	public int requestMachineID() throws RemoteException {
		int id = lastid.incrementAndGet();
		try {
			ReferenceExecutionMachine rem = new ReferenceExecutionMachine(new EndPoint("REFEXMACHINE_" + id));
			rem.setMonitor(this);
			machines.add(rem);
		} catch (MalformedURLException | InstantiationException
				| AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}

	@Override
	public void missedBeat(EndPoint remote) {
		System.err.println("DROPPED EXECUTION MACHINE " + remote);
		synchronized(machines){
			ReferenceExecutionMachine delme = null;
			for (ReferenceExecutionMachine rem : machines){
				if (remote.equals(rem.getRemoteHost())){
					delme = rem;
					break;
				}	
			}
			machines.remove(delme);
		}
	}
}
