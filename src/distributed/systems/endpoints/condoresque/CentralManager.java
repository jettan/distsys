package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import distributed.systems.das.IServerBattleField;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.IHeartbeatMonitor;


/**
 * This is an actual Central Manager implementation that runs on the Central Manager
 */
public class CentralManager implements ICentralManager, IHeartbeatMonitor{

	private static final long serialVersionUID = 1L;

	public static String serverID = "centralmanager";
	
	/**
	 * The list of all the known execution machines
	 */
	private transient List<ReferenceExecutionMachine> machines = new ArrayList<ReferenceExecutionMachine>();
	
	/**
	 * The ring mapping for all machines we know of
	 * 
	 * For example, if machine 2 dropped:
	 *  REFEXMACHINE_0 points to execution machine 1
	 *  REFEXMACHINE_1 points to execution machine 3
	 *  REFEXMACHINE_3 points to execution machine 0
	 */
	private transient ConcurrentHashMap<String, ReferenceExecutionMachine> ringMap = new ConcurrentHashMap<String, ReferenceExecutionMachine>();
	
	/**
	 * The atomic id we use to assign ids to machines
	 */
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
			ArrayList<ReferenceExecutionMachine> al = new ArrayList<ReferenceExecutionMachine>(machines);
			Collections.sort(al);
			try {
				EndPoint main = al.get(0).getRemoteHost();
				EndPoint backup = al.get(1).getRemoteHost();
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
			ArrayList<ReferenceExecutionMachine> al = new ArrayList<ReferenceExecutionMachine>(machines);
			Collections.sort(al);
			try {
				EndPoint first = al.get(0).getRemoteHost();
				if (!first.equals(ep))
					return first;
				return al.get(1).getRemoteHost();
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
	 * Called by execution machines to get the next machine in the ring
	 */
	@Override
	public EndPoint nextMachine(String myName) throws RemoteException {
		return ringMap.get(myName).getRemoteHost();
	}

	/**
	 * Called by execution machines to get an id assigned
	 */
	@Override
	public int requestMachineID() throws RemoteException {
		int id = lastid.incrementAndGet();
		try {
			ReferenceExecutionMachine rem = new ReferenceExecutionMachine(id);
			rem.setMonitor(this);
			machines.add(rem);
			addLastMachineToRing();
			System.out.println("REGISTERED EXECUTION MACHINE " + rem.getRemoteName());
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
		ReferenceExecutionMachine delme = null;
		synchronized(machines){
			for (ReferenceExecutionMachine rem : machines){
				if (remote.getRegistryName().equals(rem.getRemoteName())){
					delme = rem;
					break;
				}	
			}
			if (delme == null)
				return;
			machines.remove(delme);
			removeMachineFromRing(delme);
		}
		
	}
	
	/**
	 * Link up the last machine added to the machine list to the ring
	 * 
	 * WARNING: NEEDS TO BE EXECUTED IN A synchronized(machines) STATEMENT 
	 */
	private void addLastMachineToRing(){
		synchronized(ringMap){
			ReferenceExecutionMachine machine = machines.get(machines.size()-1);
			ringMap.put(machine.getRemoteName(), machines.get(0)); // Loop back to beginning of list/ring
			if (machines.size() > 1){
				ReferenceExecutionMachine predecessor = machines.get(machines.size()-2);
				ringMap.put(predecessor.getRemoteName(), machine);
			}
		}
	}
	
	/**
	 * Remove an execution machine from the ring
	 * 
	 * WARNING: NEEDS TO BE EXECUTED IN A synchronized(machines) STATEMENT 
	 * 
	 * @param rem The machine to remove
	 */
	private void removeMachineFromRing(ReferenceExecutionMachine rem){
		synchronized(ringMap){
			ReferenceExecutionMachine next = ringMap.remove(rem.getRemoteName());
			for (String name : ringMap.keySet()){
				if (ringMap.get(name).getId() == rem.getId()){
					ringMap.put(name, next);
				}
			}
		}
	}
	
	/**
	 * Create a String representation of the current ring for debugging purposes
	 */
	public String ringToString(){
		String out = "";
		synchronized(machines){
			Iterator<ReferenceExecutionMachine> it = machines.iterator();
			while (it.hasNext()){
				ReferenceExecutionMachine rem = it.next();
				if ("".equals(out)){
					out += rem.getId();
				} else {
					out += " -> " + rem.getId();
				}
			}
		}
		return out;
	}
	
	/**
	 * Create a String representation of the current ringMap for debugging purposes
	 */
	public String ringMapToString(){
		String out = "";
		synchronized(ringMap){
			for (String name : ringMap.keySet()){
				ReferenceExecutionMachine rem = ringMap.get(name);
				if ("".equals(out)){
					out += name + " -> " + rem.getId();
				} else {
					out += "\n" + name + " -> " + rem.getId();
				}
			}
		}
		return out;
	}
	
	/**
	 * Do a safe system shutdown
	 */
	public void shutDown(){
		synchronized(machines){
			Iterator<ReferenceExecutionMachine> it = machines.iterator();
			while (it.hasNext()){
				ReferenceExecutionMachine rem = it.next();
				EndPoint rep = rem.getRemoteHost();
				EndPoint rbep = new EndPoint(rep.getHostName(), rep.getPort(), rep.getRegistryName()+"/BATTLEFIELD");
				/*
				 * Release all clients
				 */
				try{
					IServerBattleField isbf = (IServerBattleField) rbep.connect();
					isbf.shutdown();
				} catch (RemoteException | MalformedURLException | NotBoundException e){
					e.printStackTrace();
				}
				/*
				 * Release this server
				 */
				try{
					IExecutionMachine iem = (IExecutionMachine) rep.connect();
					iem.kill();
				} catch (RemoteException | MalformedURLException | NotBoundException e){
					e.printStackTrace();
				}
			}
		}
	}
}
