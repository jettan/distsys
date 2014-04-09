package distributed.systems.main;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import distributed.systems.endpoints.EndPoint;

public class CentralManagerMain {

	public static void main(String[] args) throws InterruptedException{
		Registry reg = null;
		
		try {
			reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			System.err.println("Fatal error: could not create registry!");
			e.printStackTrace();
		}
		
		final EndPoint centralManagerEP = new EndPoint("CENTRAL_MANAGER");
		try {
			new distributed.systems.endpoints.condoresque.CentralManager(centralManagerEP);
		} catch (MalformedURLException | RemoteException
				| InstantiationException | AlreadyBoundException e2) {
			e2.printStackTrace();
		}
		
		if (args.length > 0)
			try {
				Thread.sleep(Long.parseLong(args[0]));
			} catch (NumberFormatException | InterruptedException e1) {
				Thread.sleep(300000);	// Sleep for 5 minutes by default
			}
		else
			Thread.sleep(300000);	// Sleep for 5 minutes by default
		
		try {
			java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		} catch (NoSuchObjectException e) {
			// If it's already released, that's good
		}
		
		System.exit(0); // Stop all running processes
	}
	
}
