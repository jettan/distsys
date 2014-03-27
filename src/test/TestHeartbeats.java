package test;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.HeartbeatReceiver;
import distributed.systems.endpoints.HeartbeatSender;

public class TestHeartbeats {
	
	public static Registry reg = null;

	public static void main(String[] args) throws MalformedURLException, RemoteException, InstantiationException, AlreadyBoundException, NotBoundException{
		setUp();
		
		// Serve heartbeats on the HB_RECEIVER registry
		EndPoint srv = new EndPoint("HB_RECEIVER");
		// Connect to the localhost server on the HB_RECEIVER registry
		EndPoint clt = new EndPoint("localhost", 1099, "HB_RECEIVER");
		
		// Set up a server side heartbeat receiver and detect client drop
		new HeartbeatReceiver(srv);
		// Set up a client side heartbeat sender and detect server drop
		HeartbeatSender hbs = new HeartbeatSender(null, clt);
		
		System.out.println("RUNNING TEST FOR 5 SECONDS");
		System.out.println("EXPECTING DELIVERY OF 2 MESSAGES:");
		
		hbs.setPayload("Message (1/2)");
		
		try {
			Thread.sleep(2500);
			hbs.setPayload("Message (2/2)");
			Thread.sleep(2500);
		} catch (InterruptedException e) {
		} 
		System.out.println("TEST FINISHED");
		
		// Stop sending heartbeats
		hbs.kill();
		
		tearDown();
	}
	
	public static void setUp(){
		try {
			reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			System.err.println("Fatal error: could not create registry!");
			e.printStackTrace();
		}
	}
	
	public static void tearDown(){
		try {
			java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		} catch (NoSuchObjectException e) {
			// If it's already released, that's good
		}
		
		System.exit(0);
	}
}
