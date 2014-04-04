package distributed.systems.das;

import java.rmi.RemoteException;

import distributed.systems.core.exception.AlreadyAssignedIDException;

/**
 * Trail state of servers
 */
public class ClientBattleField extends BattleField {

	private static final long serialVersionUID = 1L;

	public ClientBattleField(int width, int height) throws RemoteException, AlreadyAssignedIDException{
		super(width, height);
	}
	
}
