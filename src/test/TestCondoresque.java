package test;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.condoresque.CentralManager;
import distributed.systems.endpoints.condoresque.Client;
import distributed.systems.endpoints.condoresque.ExecutionMachine;

public class TestCondoresque {
	public static Registry reg = null;

	public static void main(String[] args) throws MalformedURLException, RemoteException, InstantiationException, AlreadyBoundException, NotBoundException{
		setUp();
		
		int eMachineAmount = 8;
		int clientAmount = 32;
		
		/**
		 * Initialize all of the endpoints
		 */
		EndPoint centralManagerEP = new EndPoint("CENTRAL_MANAGER");
		ArrayList<EndPoint> execMachinesEPs = new ArrayList<EndPoint>();
		for (int i = 0; i < eMachineAmount; i++)
			execMachinesEPs.add(new EndPoint("EXECUTION_MACHINE_" + i));
		ArrayList<EndPoint> clientEPs = new ArrayList<EndPoint>();
		for (int i = 0; i < clientAmount; i++)
			clientEPs.add(new EndPoint("CLIENT_" + i));
		
		/**
		 * Bind the central manager
		 * (Normally done on different device)
		 */
		CentralManager centralManager = new CentralManager(centralManagerEP); 
		centralManager.initializeServerInterfaces(eMachineAmount);
		
		/**
		 * Bind every execution machine
		 * (Normally done on multiple different devices)
		 */
		ArrayList<ExecutionMachine> eMachines = new ArrayList<ExecutionMachine>();
		for (int i = 0; i < eMachineAmount; i++)
			eMachines.add(new ExecutionMachine(centralManagerEP, execMachinesEPs.get(i)));
		
		System.out.println("SERVER SETUP FINISHED, STARTING SLEEP");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		} 
		
		/**
		 * Bind every client
		 * (Normally done on multiple different devices)
		 */
		ArrayList<Client> clients = new ArrayList<Client>();
		for (int i = 0; i < clientAmount; i++){
			Client client = new Client(null, centralManagerEP);
			if (!client.connect())
				System.err.println("ALLOCATION FAILED FOR CLIENT " + i);
			clients.add(client);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			} 
		}
		
		System.out.println("CLIENT SETUP FINISHED, STARTING SLEEP");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		} 
		
		System.out.println("SHUTTING DOWN");
		for (int i = 0; i < clientAmount; i++)
			try{
				clients.get(i).disconnect();
			} catch (Exception e){}
		
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
