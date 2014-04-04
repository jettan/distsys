package distributed.systems.das;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.condoresque.ExecutionMachine;

/**
 * Synchronize every action with every server in the ring
 */
public class ServerBattleField extends BattleField {

	private static final long serialVersionUID = 1L;
	
	private ExecutionMachine machine;
	private EndPoint registry;
	
	public ServerBattleField(ExecutionMachine machine, int width, int height) throws RemoteException, AlreadyAssignedIDException, MalformedURLException, InstantiationException, AlreadyBoundException{
		super(width, height);
		this.machine = machine;
		
		registry = machine.getBattlefieldReg();
		registry.open(this);
	}
	
}
