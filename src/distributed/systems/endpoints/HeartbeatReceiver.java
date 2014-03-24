package distributed.systems.endpoints;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;

/**
 * A class that checks whether a sequence of heartbeats is received.
 * 
 * Starts when the first heartbeat is received.
 * Ends when the remote end signals a release.
 * 
 * Note that heartbeats do not have to arrive in a FIFO order:
 * as long as they are on time.
 */
public class HeartbeatReceiver implements IHeartbeatReceiver{

	private static final long serialVersionUID = 1L;

	/**
	 * The maximum time between heartbeats
	 * Failing to send the next heartbeat within this amount of millis
	 * will result in missedBeat(long id) being called.
	 */
	private final static long TIMEOUT = 200;
	
	private transient Waiter currentWaiter;
	
	private final transient EndPoint endpoint;
	
	private transient EndPoint remoteHost = null;
	
	private IHeartbeatMonitor monitor;
	
	/**
	 * Bind ourselves to an endpoint
	 * 
	 * @param endpoint The endpoint to bind to
	 * @throws AlreadyBoundException 
	 * @throws InstantiationException 
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 */
	public HeartbeatReceiver(EndPoint endpoint) throws MalformedURLException, RemoteException, InstantiationException, AlreadyBoundException{
		this(endpoint, null);
	}
	
	public HeartbeatReceiver(EndPoint endpoint, IHeartbeatMonitor monitor) throws MalformedURLException, RemoteException, InstantiationException, AlreadyBoundException{	
		setMonitor(monitor);
		
		this.endpoint = endpoint;

		this.endpoint.open((IHeartbeatReceiver) UnicastRemoteObject.exportObject(this, 0));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receiveHeartbeat(EndPoint sender, byte id, Serializable payload) throws RemoteException {
		if (currentWaiter != null)
			currentWaiter.receive(id);
		
		if (payload != null)
			receivePayload(payload);
		
		try {
			if (sender != null)
				remoteHost = new EndPoint(RemoteServer.getClientHost(), sender.getPort(), sender.getRegistryName());
		} catch (ServerNotActiveException e1) {
		}
		
		byte nextid = (byte) ((id+1)%256);
		final HeartbeatReceiver me = this;
		final Waiter w = new Waiter(me, nextid);
		currentWaiter = w;
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				w.start();
				try {
					w.join(TIMEOUT);
				} catch (InterruptedException e) {
				}
				w.kill();
			}
		});
		t.start();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() throws RemoteException {
		currentWaiter.release();
		try {
			endpoint.close();
		} catch (MalformedURLException e) {
		}
	}
	
	/**
	 * Get the EndPoint for the machine sending us heartbeats
	 * 
	 * @return The endpoint for the remote host
	 */
	public EndPoint getRemoteHost(){
		return remoteHost;
	}

	/**
	 * Lost connection to the HeartbeatSender.
	 */
	public void missedBeat(long id){
		if (monitor != null)
			monitor.missedBeat(getRemoteHost());
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
	 * Called if a payload was piggybacked onto a heartbeat
	 * 
	 * @param payload The delivered payload
	 */
	public void receivePayload(Serializable payload){
		System.out.println("Received payload: " + payload);
	}
	
	/**
	 * A thread that waits for a single heartbeat
	 * 
	 * Can do one of three things:
	 *  1. Receive it's expected sequence number
	 *  2. Be killed because of timeout (call missedBeat())
	 *  3. Be released (do NOT call missedBeat())
	 */
	private class Waiter extends Thread{
		
		private final int expected;
		private final HeartbeatReceiver receiver;
		private boolean killed = false;
		private boolean released = false;
		private int gotten = -1;
		
		public Waiter(HeartbeatReceiver receiver, int expected){
			super("HEARTBEAT_THREAD" + expected);
			
			this.expected = expected;
			this.receiver = receiver;
		}
		
		private void kill(){
			killed = true;
		}
		
		public void receive(int value){
			gotten = value;
		}
		
		public void release(){
			released = true;
		}
		
		public void run(){
			while (gotten != expected && !killed && !released){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
			if (killed)
				receiver.missedBeat(expected);
		}
	}
}
