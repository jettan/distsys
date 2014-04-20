package distributed.systems.das;

import java.rmi.Remote;
import java.rmi.RemoteException;

import distributed.systems.das.units.IUnit;

public interface IServerBattleField extends Remote {

	public String getName() throws RemoteException;
	public int getNewUnitID() throws RemoteException;
	public int getNewUnitID(String registry) throws RemoteException;
	public boolean rawSetUnit(String registry, int x, int y, IUnit unit) throws RemoteException;
	public boolean rawMoveUnit(String registry, int x, int y, int originalX, int originalY, IUnit unit) throws RemoteException;
	public void addUnit(String registry, IUnit unit) throws RemoteException;
	public void removeUnit(String registry, IUnit unit) throws RemoteException;
	public void shutdown() throws RemoteException;
}
