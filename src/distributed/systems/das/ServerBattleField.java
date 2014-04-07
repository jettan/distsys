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
	public synchronized int getNewUnitID() {
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
	public synchronized int getNewUnitID(String registry) throws RemoteException{
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
	
	/**
	 * TODO Synchronize
	 */
	public boolean rawSetUnit(int x, int y, IUnit unit){
		return super.rawSetUnit(x, y, unit);
	}
	
	/**
	 * The TODO Synchronize
	 */
	public boolean rawMoveUnit(int x, int y, int originalX, int originalY, IUnit unit){
		return super.rawMoveUnit(x, y, originalX, originalY, unit);
	}
	
}
