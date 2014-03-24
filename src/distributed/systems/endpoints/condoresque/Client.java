package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import distributed.systems.das.units.Unit;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.IHeartbeatMonitor;

public class Client implements IHeartbeatMonitor{

	/// The unit corresponding to this client.
	private Unit unit;
	
	/// The allocation got from the central manager.
	private Allocation allocation;
	
	private final EndPoint centralManager;
	
	public Client(Unit u, EndPoint centralManager) {
		this.unit       = u;
		this.allocation = null;
		this.centralManager = centralManager;
	}
	
	public boolean connect() {
		
		// Try looking up the central manager in the rmi registry.
		try {
			ICentralManager centralManager = (ICentralManager) this.centralManager.connect();
			
			// Request allocation from the central manager.
			this.allocation = centralManager.requestExecution();
			
			if (this.allocation != null){
				// Start the heartbeats
				this.allocation.createHeartbeats(this);
				return true;
			}
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// Panic! Central manager not available.
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
	
	public Unit getUnit() {
		return this.unit;
	}

	@Override
	public void missedBeat(EndPoint remote) {
		if (allocation.getMain().equals(remote)){
			// TODO lost connection to main server
		} else {
			// TODO lost connection to backup server
		}
	}
}
