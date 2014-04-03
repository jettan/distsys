package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import distributed.systems.endpoints.EndPoint;

public class Client{
	
	/** 
	 * The allocation got from the central manager.
	 */
	private Allocation allocation;
	
	private final EndPoint centralManager;
	
	public Client(EndPoint centralManager) {
		this.allocation = null;
		this.centralManager = centralManager;
	}
	
	public boolean connect() {
		
		// Try looking up the central manager in the rmi registry.
		try {
			ICentralManager centralManager = (ICentralManager) this.centralManager.connect();
			
			// Request allocation from the central manager.
			allocation = centralManager.requestExecution();
			
			if (allocation != null){
				// Start the heartbeats
				allocation.setCM(centralManager);
				allocation.createHeartbeats();
				return true;
			}
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// Panic! Central manager not available.
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	public boolean disconnect(){
		try {
			if (allocation != null)
				allocation.stopHeartbeats();
		} catch (RemoteException e) {
			return false;
		}
		return true;
	}
	
	public Allocation getAllocation(){
		return allocation;
	}
	
	public void fakeCrash(){
		allocation.fakeCrash();
	}
}
