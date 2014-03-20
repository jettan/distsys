package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import distributed.systems.das.units.Unit;

public class Client {

	/// The unit corresponding to this client.
	private Unit unit;
	
	/// The allocation got from the central manager.
	private Allocation allocation;
	
	public Client(Unit u) {
		this.unit       = u;
		this.allocation = null;
	}
	
	public boolean connect() {
		
		// Try looking up the central manager in the rmi registry.
		try {
			ICentralManager centralManager = (ICentralManager)java.rmi.Naming.lookup(CentralManager.serverID);
			
			// Request allocation from the central manager.
			this.allocation = centralManager.requestExecution();
			
			if (this.allocation != null)
				return true;
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// Panic! Central manager not available.
			return false;
		}
		return false;
	}
	
	public Unit getUnit() {
		return this.unit;
	}
}
