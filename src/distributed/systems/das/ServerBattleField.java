package distributed.systems.das;

import java.rmi.RemoteException;

import distributed.systems.core.exception.AlreadyAssignedIDException;

public class ServerBattleField extends BattleField {

	public ServerBattleField(int width, int height) throws RemoteException, AlreadyAssignedIDException{
		super(width, height);
	}
	
}
