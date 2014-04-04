package distributed.systems.das;

import java.rmi.RemoteException;

import distributed.systems.core.exception.AlreadyAssignedIDException;

public class ClientBattleField extends BattleField {

	public ClientBattleField(int width, int height) throws RemoteException, AlreadyAssignedIDException{
		super(width, height);
	}
	
}
