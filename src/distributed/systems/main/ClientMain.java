package distributed.systems.main;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.condoresque.Client;

public class ClientMain {

	public static void main(String[] args){
		Registry reg = null;
		
		try {
			reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			System.err.println("Fatal error: could not create registry!");
			e.printStackTrace();
		}
		
		String type = args[0];
		String centralManagerIP = args[1];
		
		final EndPoint centralManagerEP = new EndPoint(centralManagerIP, 1099, "CENTRAL_MANAGER");
		
		Client c = new Client(centralManagerEP);
		c.connect();
		boolean placed = false;
		for (int i = 0; i < 200; i++){
			try{
				if ("DRAGON".equals(type))
					new Dragon(c, (int)(Math.random() * BattleField.MAP_WIDTH), (int)(Math.random() * BattleField.MAP_HEIGHT));
				else if ("PLAYER".equals(type))
					new Player(c, (int)(Math.random() * BattleField.MAP_WIDTH), (int)(Math.random() * BattleField.MAP_HEIGHT));
				placed = true;
				break;
			} catch (RemoteException e){
				continue;
			}
		}
		
		if (!placed){
			c.disconnect();
			System.err.println("Failed to place unit, aborting");
		}
		
		try {
			java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		} catch (NoSuchObjectException e) {
			// If it's already released, that's good
		}
		
		System.exit(0); // Stop all running processes
	}
	
}
