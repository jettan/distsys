package distributed.systems.das;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.units.IUnit;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.condoresque.ExecutionMachine;

/**
 * Synchronize every action with every server in the ring
 */
public class ServerBattleField extends BattleField implements IServerBattleField {

	private static final long serialVersionUID = 1L;
	
	private ExecutionMachine machine;
	private EndPoint registry;
	
	private int unitid = 0;
	
	public ServerBattleField(ExecutionMachine machine, int width, int height) throws RemoteException, AlreadyAssignedIDException, MalformedURLException, InstantiationException, AlreadyBoundException{
		super(width, height);
		this.machine = machine;
		
		registry = machine.getBattlefieldReg();
		registry.open(this);
	}
	
	public String getName(){
		return registry.getRegistryName();
	}
	
	/**
	 * Returns a new globally unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public int getNewUnitID() {
		try {
			return getNewUnitID(registry.getRegistryName());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Contact all servers in the server ring to provide a globally unique id
	 */
	public int getNewUnitID(String registry) throws RemoteException{
		/**
		 * Get the next server in the ring
		 */
		IServerBattleField remote;
		try {
			remote = (IServerBattleField) machine.nextServerBattlefield().connect();
		} catch (MalformedURLException | NotBoundException e) {
			throw new RemoteException();
		}
		/**
		 * If we have gone full circle, give our own suggestion
		 */
		if (registry.equals(remote.getName())){
			unitid++;
			return unitid;
		}
		/**
		 * If someone else has a higher unitid, we need to adapt to
		 * it (Scalar Clock technique)
		 */
		int maxid = remote.getNewUnitID(registry);
		if (maxid > unitid + 1)
			unitid = maxid;
		else
			unitid++;
		return unitid;
	}
	
	public boolean rawSetUnit(int x, int y, IUnit unit){
		try {
			return rawSetUnit(registry.getRegistryName(), x, y, unit);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean rawSetUnit(String registry, int x, int y, IUnit unit) throws RemoteException{
		/**
		 * Get the next server in the ring
		 */
		IServerBattleField remote;
		try {
			remote = (IServerBattleField) machine.nextServerBattlefield().connect();
		} catch (MalformedURLException | NotBoundException e) {
			throw new RemoteException();
		}
		/**
		 * If we have gone full circle, give our own suggestion
		 */
		if (registry.equals(remote.getName())){
			if (isEmpty(x,y)){
				super.rawSetUnit(x, y, unit);
				return true;
			} else
				return false;
		}
		/**
		 * If we cannot allow this move, roll back
		 */
		if (!isEmpty(x,y))
			return false;
		/**
		 * If everyone allows this move, commit
		 */
		if (remote.rawSetUnit(registry, x, y, unit)){
			super.rawSetUnit(x, y, unit);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean rawMoveUnit(int x, int y, int originalX, int originalY, IUnit unit){
		try {
			return rawMoveUnit(registry.getRegistryName(), x, y, originalX, originalY, unit);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean rawMoveUnit(String registry, int x, int y, int originalX, int originalY, IUnit unit) throws RemoteException{
		/**
		 * Get the next server in the ring
		 */
		IServerBattleField remote;
		try {
			remote = (IServerBattleField) machine.nextServerBattlefield().connect();
		} catch (MalformedURLException | NotBoundException e) {
			throw new RemoteException();
		}
		/**
		 * If we have gone full circle, give our own suggestion
		 */
		if (registry.equals(remote.getName())){
			if (isEmpty(x,y)){
				super.rawMoveUnit(x, y, originalX, originalY, unit);
				return true;
			}else
				return false;
		}
		/**
		 * If we cannot allow this move, roll back
		 */
		if (!isEmpty(x,y))
			return false;
		/**
		 * If everyone allows this move, commit
		 */
		if (remote.rawMoveUnit(registry, x, y, originalX, originalY, unit)){
			super.rawMoveUnit(x, y, originalX, originalY, unit);
			return true;
		} else {
			return false;
		}
	}

	public void addUnit(IUnit unit){
		try {
			addUnit(registry.getRegistryName(), unit);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addUnit(String registry, IUnit unit) throws RemoteException{
		super.addUnit(unit);
		/**
		 * Get the next server in the ring
		 */
		IServerBattleField remote;
		try {
			remote = (IServerBattleField) machine.nextServerBattlefield().connect();
		} catch (MalformedURLException | NotBoundException e) {
			System.err.println("Could not reach server " + machine.nextServerBattlefield());
			throw new RemoteException();
		}
		/**
		 * If we have gone full circle, stop
		 * Else propagate
		 */
		if (!registry.equals(remote.getName())){
			remote.addUnit(registry, unit);
		}
	}

	public void removeUnit(IUnit unit){
		try {
			removeUnit(registry.getRegistryName(), unit);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void removeUnit(String registry, IUnit unit) throws RemoteException{
		super.removeUnit(unit);
		/**
		 * Get the next server in the ring
		 */
		IServerBattleField remote;
		try {
			remote = (IServerBattleField) machine.nextServerBattlefield().connect();
		} catch (MalformedURLException | NotBoundException e) {
			throw new RemoteException();
		}
		/**
		 * If we have gone full circle, stop
		 * Else propagate
		 */
		if (!registry.equals(remote.getName())){
			remote.removeUnit(registry, unit);
		}
	}

	
}
