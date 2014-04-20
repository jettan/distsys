package distributed.systems.main;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.ServerBattleField;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.condoresque.ExecutionMachine;

public class ExecutionMachineMain {

	public static void main(String[] args) throws InterruptedException{
		Registry reg = null;
		
		try {
			reg = java.rmi.registry.LocateRegistry.createRegistry(1100);
		} catch (RemoteException e) {
			System.err.println("Fatal error: could not create registry!");
			e.printStackTrace();
		}
		
		String name = args[0];
		String centralManagerIP = args[1];
		
		final EndPoint centralManagerEP = new EndPoint(centralManagerIP, 1099, "CENTRAL_MANAGER");
		EndPoint ep = new EndPoint("localhost", 1100, name);
		
		System.out.println("STARTING EXECUTION MACHINE");
		System.out.println("CENTRAL MANAGER: " + centralManagerEP);
		System.out.println("LOCAL REGISTRY: " + ep);
		
		ExecutionMachine em = null;
		
		try {
			em = new ExecutionMachine(centralManagerEP, ep);
			new ServerBattleField(em, BattleField.MAP_WIDTH, BattleField.MAP_HEIGHT); // Viewing through one of two servers
		} catch (MalformedURLException | RemoteException
				| InstantiationException | NotBoundException
				| AlreadyBoundException | AlreadyAssignedIDException e) {
			e.printStackTrace();
		}
		
		if (args.length > 2)
			try {
				Thread.sleep(Long.parseLong(args[2]));
			} catch (NumberFormatException | InterruptedException e1) {
				Thread.sleep(240000);	// Sleep for 4 minutes by default
			}
		else {
			while (em.isAlive()){
				Thread.sleep(2000);	// Sleep for two seconds and try to shut down again
			}
			System.out.println("We have been shut down");
		}
		
		try {
			java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		} catch (NoSuchObjectException e) {
			// If it's already released, that's good
		}
		
		System.exit(0); // Stop all running processes
	}
	
}
