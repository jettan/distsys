package distributed.systems.das;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServerBattleField extends Remote {

	public String getName() throws RemoteException;
	public int getNewUnitID(String registry) throws RemoteException;
	
}
