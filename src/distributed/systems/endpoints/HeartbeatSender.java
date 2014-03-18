package distributed.systems.endpoints;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class HeartbeatSender implements Runnable{

	private final static long BEAT_TIME = 100;

	private final IHeartbeatReceiver rmInterface;
	
	private boolean alive = true;
	
	public HeartbeatSender(EndPoint remote) throws MalformedURLException, RemoteException, NotBoundException{
		rmInterface = (IHeartbeatReceiver) remote.connect();
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public void kill() throws RemoteException{
		alive = false;
		rmInterface.release();
	}
	
	public void missedBeat(long id){
		System.out.println("Failed to send heartbeat " + id);
	}

	@Override
	public void run() {
		int id = 0;
		while (alive){
			try {
				rmInterface.receiveHeartbeat((byte) id);
			} catch (RemoteException e) {
				missedBeat(id);
			}
			try {
				Thread.sleep(BEAT_TIME);
			} catch (InterruptedException e) {
			}
			id++;
		}
	}
}
