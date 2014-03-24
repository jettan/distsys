package distributed.systems.endpoints.condoresque;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.HeartbeatSender;
import distributed.systems.endpoints.IHeartbeatMonitor;

public class Allocation implements Serializable, IHeartbeatMonitor {

	private static final long serialVersionUID = 1L;
	
	private final EndPoint main;
	private final EndPoint backup;
	
	private transient IExecutionMachine mainmachine;
	private transient IExecutionMachine backupmachine;
	
	private transient HeartbeatSender mainHB;
	private transient HeartbeatSender backupHB;
	private transient IHeartbeatMonitor monitor;
	
	public Allocation(EndPoint main, EndPoint backup){
		this.main = main;
		this.backup = backup;
	}

	public EndPoint getMain() {
		return main;
	}

	public EndPoint getBackup() {
		return backup;
	}

	public void createHeartbeats(IHeartbeatMonitor monitor) throws MalformedURLException, RemoteException, NotBoundException{
		this.monitor = monitor;
		
		mainmachine = (IExecutionMachine) main.connect();
		backupmachine = (IExecutionMachine) backup.connect();
		
		EndPoint mep = mainmachine.addClient(true);
		EndPoint bep = backupmachine.addClient(false);
		
		mainHB = new HeartbeatSender(new EndPoint(main.getHostName(), main.getPort(), mep.getRegistryName()), this);
		backupHB = new HeartbeatSender(new EndPoint(backup.getHostName(), backup.getPort(), bep.getRegistryName()), this);
	}

	public void stopHeartbeats() throws RemoteException{
		RemoteException out = null;
		try {
			mainHB.kill();
		} catch (RemoteException e) {
			out = e;
		}
		try {
			backupHB.kill();
		} catch (RemoteException e) {
			out = e;
		}
		if (out != null)
			throw out;
	}
	
	@Override
	public void missedBeat(EndPoint remote) {
		if (monitor != null)
			monitor.missedBeat(remote);
	}

}
