package distributed.systems.das.units;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.das.units.Unit.UnitType;

public interface UnitRef extends Remote, Serializable, IMessageReceivedHandler{

	public void adjustHitPoints(int modifier) throws RemoteException;

	public void dealDamage(int x, int y, int damage) throws RemoteException;

	public void healDamage(int x, int y, int healed) throws RemoteException;

	public int getMaxHitPoints() throws RemoteException;

	public int getUnitID() throws RemoteException;

	public void setPosition(int x, int y) throws RemoteException;

	public int getX() throws RemoteException;

	public int getY() throws RemoteException;

	public int getHitPoints() throws RemoteException;

	public int getAttackPoints() throws RemoteException;
	
	public UnitType getMyType() throws RemoteException;

	public void disconnect() throws RemoteException;

	public void stopRunnerThread() throws RemoteException;

}
