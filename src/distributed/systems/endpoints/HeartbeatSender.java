package distributed.systems.endpoints;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class HeartbeatSender implements Runnable{

	private final static long BEAT_TIME = 500;

	private final EndPoint local;
	
	private EndPoint remote;
	private IHeartbeatReceiver rmInterface;
	
	private boolean alive = true;
	
	private Serializable payload;
	
	private IHeartbeatMonitor monitor;
	
	/**
	 * Construct a new HeartBeat sender without an endpoint.
	 * Use the local EndPoint for responses.
	 */
	public HeartbeatSender(EndPoint local){
		this.local = local;
	}
	
	/**
	 * Construct a new HeartbeatSender and immediately start the heartbeat:
	 * same as calling connect() after construction.
	 * Use the local EndPoint for responses.
	 * 
	 * @param remote
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public HeartbeatSender(EndPoint local, EndPoint remote) throws MalformedURLException, RemoteException, NotBoundException{
		this.local = local;
		connect(remote);
	}
	
	/**
	 * Construct a new HeartbeatSender and immediately start the heartbeat:
	 * same as calling setMonitor() and connect() after construction.
	 * 
	 * @param remote
	 * @param monitor
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public HeartbeatSender(EndPoint local, EndPoint remote, IHeartbeatMonitor monitor) throws MalformedURLException, RemoteException, NotBoundException{
		this.local = local;
		setMonitor(monitor);
		connect(remote);
	}
			
	/**
	 * Connect to the remote endpoint and set up the heartbeat
	 * 
	 * @param remote
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void connect(EndPoint remote) throws MalformedURLException, RemoteException, NotBoundException{
		this.remote = remote;
		rmInterface = (IHeartbeatReceiver) remote.connect();
		
		Thread t = new Thread(this);
		t.start();
	}
	
	/**
	 * Stop the heartbeat legally on the HeartbeatSender and the HeartbeatReceiver.
	 * missedBeat() will not be called on either end.
	 * 
	 * @throws RemoteException
	 */
	public void kill() throws RemoteException{
		alive = false;
		rmInterface.release();
	}
	
	/**
	 * Lost connection to the HeartbeatReceiver.
	 */
	public void missedBeat(long id){
		if (monitor != null)
			monitor.missedBeat(remote);
	}
	
	/**
	 * Set the monitor for this heartbeat
	 * 
	 * @param monitor
	 */
	public void setMonitor(IHeartbeatMonitor monitor){
		this.monitor = monitor;
	}
	
	/**
	 * Set the payload that will biggyback on the next heartbeat
	 * 
	 * NOTE: If you set the payload before isPayloadDelivered() the previous
	 * payload will be OVERWRITTEN.
	 * 
	 * @param payload
	 */
	public void setPayload(Serializable payload){
		if (this.payload != null){
			synchronized(this.payload){
				this.payload = payload;
			}
		} else {
			this.payload = payload;
		}
	}
	
	/**
	 * Check if a previously set payload was delivered
	 * 
	 * @return
	 */
	public boolean isPayloadDelivered(){
		return payload == null;
	}
	
	/**
	 * Perform a 'crash'
	 */
	public void fakeCrash(){
		alive = false;
	}
	
	/**
	 * Stop without notifying the other endpoint
	 */
	public void emergencyStop(){
		alive = false;
	}
	
	public EndPoint getCallbackEndpoint(){
		return local;
	}
	
	public boolean isAlive(){
		return alive;
	}

	/**
	 * Send heartbeats periodically and check for drops
	 */
	@Override
	public void run() {
		int id = 0;
		while (alive){
			try {
				if (payload != null){
					synchronized(payload){
						rmInterface.receiveHeartbeat(local, (byte) id, payload);
						payload = null;
					}
				} else {
					rmInterface.receiveHeartbeat(local, (byte) id, null);
				}
			} catch (RemoteException e) {
				missedBeat(id);
			}
			try {
				Thread.sleep(BEAT_TIME);
			} catch (InterruptedException e) {
			}
			id++;
			id = id % 256;
		}
	}
}
