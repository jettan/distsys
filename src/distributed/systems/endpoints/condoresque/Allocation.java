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
	
	private EndPoint main;
	private EndPoint backup;
	
	private transient IExecutionMachine mainmachine;
	private transient IExecutionMachine backupmachine;
	
	private transient HeartbeatSender mainHB;
	private transient HeartbeatSender backupHB;
	
	private transient ICentralManager centralManager;
	
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

	public void createHeartbeats() throws MalformedURLException, RemoteException, NotBoundException{
		System.out.println("SETTING UP CONNECTION TO SERVERS:");
		System.out.println(main);
		System.out.println(backup);
		
		mainmachine = (IExecutionMachine) main.connect();
		backupmachine = (IExecutionMachine) backup.connect();
		
		EndPoint mep = mainmachine.addClient(true);
		EndPoint bep = backupmachine.addClient(false);
		
		mainHB = new HeartbeatSender(null, new EndPoint(main.getHostName(), main.getPort(), mep.getRegistryName()), this);
		backupHB = new HeartbeatSender(null, new EndPoint(backup.getHostName(), backup.getPort(), bep.getRegistryName()), this);
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
	
	public void fakeCrash(){
		mainHB.fakeCrash();
		backupHB.fakeCrash();
	}
	
	@Override
	public void missedBeat(EndPoint remote) {
		try {
			if (remote.getURI().startsWith(main.getURI())){
				// Main dropped, request server that is not backup
				mainHB.emergencyStop();
				main = centralManager.requestReplacement(backup);
				mainmachine = (IExecutionMachine) main.connect();
				EndPoint mep = mainmachine.addClient(true);
				mainHB = new HeartbeatSender(null, new EndPoint(main.getHostName(), main.getPort(), mep.getRegistryName()), this);
				System.err.println("MAIN SERVER DROPPED: REPLACING WITH " + main);
			} else if (remote.getURI().startsWith(backup.getURI())) {
				// Backup dropped, request server that is not main
				backupHB.emergencyStop();
				backup = centralManager.requestReplacement(main);
				backupmachine = (IExecutionMachine) backup.connect();
				EndPoint bep = backupmachine.addClient(false);
				backupHB = new HeartbeatSender(null, new EndPoint(backup.getHostName(), backup.getPort(), bep.getRegistryName()), this);
				System.err.println("BACKUP SERVER DROPPED: REPLACING WITH " + backup);
			}
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	public void setCM(ICentralManager centralManager) {
		this.centralManager = centralManager;
	}

}
