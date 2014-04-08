package distributed.systems.das;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.presentation.BattleFieldViewer;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.condoresque.CentralManager;
import distributed.systems.endpoints.condoresque.Client;
import distributed.systems.endpoints.condoresque.ExecutionMachine;

/**
 * Controller part of the DAS game. Initializes 
 * the viewer, adds 20 dragons and 100 players. 
 * Once every 5 seconds, another player is added
 * to simulate a connecting client.
 *  
 * @author Pieter Anemaet, Boaz Pat-El
 */
public class Core {
	public static final int MIN_PLAYER_COUNT = 10;
	public static final int MAX_PLAYER_COUNT = 10;
	public static final int DRAGON_COUNT = 5;
	public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000; // In milliseconds
	
	public static BattleField battlefield; 
	public static int playerCount;

	public static void main(String[] args) {
		
		Registry reg = null;
		
		try {
			reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			System.err.println("Fatal error: could not create registry!");
			e.printStackTrace();
		}
		
		try {
			battlefield = new BattleField(BattleField.MAP_WIDTH, BattleField.MAP_HEIGHT);
		} catch (RemoteException | AlreadyAssignedIDException e2) {
			e2.printStackTrace();
		}
		
		final EndPoint centralManagerEP = new EndPoint("CENTRAL_MANAGER");
		try {
			CentralManager cm = new CentralManager(centralManagerEP);
		} catch (MalformedURLException | RemoteException
				| InstantiationException | AlreadyBoundException e2) {
			e2.printStackTrace();
		}
		
		for (int i = 0; i < 5; i++){
			EndPoint ep = new EndPoint("EXECUTION_MACHINE_" + i);
			try {
				ExecutionMachine em = new ExecutionMachine(centralManagerEP, ep);
				battlefield = new ServerBattleField(em, BattleField.MAP_WIDTH, BattleField.MAP_HEIGHT); // Viewing through one of two servers
			} catch (MalformedURLException | RemoteException
					| InstantiationException | NotBoundException
					| AlreadyBoundException | AlreadyAssignedIDException e) {
				e.printStackTrace();
			}
		}

		/* All the dragons connect */
		for(int i = 0; i < DRAGON_COUNT; i++) {
			/* Try picking a random spot */
			int x, y, attempt = 0;
			do {
				x = (int)(Math.random() * BattleField.MAP_WIDTH);
				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
				attempt++;
			} while (battlefield.getUnit(x, y) != null && attempt < 10);

			// If we didn't find an empty spot, we won't add a new dragon
			if (battlefield.getUnit(x, y) != null) break;
			
			final int finalX = x;
			final int finalY = y;

			/* Create the new dragon in a separate
			 * thread, making sure it does not 
			 * block the system.
			 */
			new Thread(new Runnable() {
				public void run() {
					try {
						Client c = new Client(centralManagerEP);
						c.connect();
						new Dragon(c, finalX, finalY);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}).start();

		}

		/* Initialize a random number of players (between [MIN_PLAYER_COUNT..MAX_PLAYER_COUNT] */
		playerCount = (int)((MAX_PLAYER_COUNT - MIN_PLAYER_COUNT) * Math.random() + MIN_PLAYER_COUNT);
		for(int i = 0; i < playerCount; i++)
		{
			/* Once again, pick a random spot */
			int x, y, attempt = 0;
			do {
				x = (int)(Math.random() * BattleField.MAP_WIDTH);
				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
				attempt++;
			} while (battlefield.getUnit(x, y) != null && attempt < 10);

			// If we didn't find an empty spot, we won't add a new player
			if (battlefield.getUnit(x, y) != null) break;

			final int finalX = x;
			final int finalY = y;

			/* Create the new player in a separate
			 * thread, making sure it does not 
			 * block the system.
			 */
			new Thread(new Runnable() {
				public void run() {
					try {
						Client c = new Client(centralManagerEP);
						c.connect();
						new Player(c, finalX, finalY);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
		}

		/* Spawn a new battlefield viewer */
		new Thread(new Runnable() {
			public void run() {
				new BattleFieldViewer(battlefield);
			}
		}).start();
		
		/* Add a random player every (5 seconds x GAME_SPEED) so long as the
		 * maximum number of players to enter the battlefield has not been exceeded. 
		 */
		while(GameState.getRunningState()) {
			try {
				Thread.sleep((int)(5000 * GameState.GAME_SPEED));

				// Connect a player to the game if the game still has room for a new player
				if (playerCount >= MAX_PLAYER_COUNT) continue;

				// Once again, pick a random spot
				int x, y, attempts = 0;
				do {
					// If finding an empty spot just keeps failing then we stop adding the new player
					x = (int)(Math.random() * BattleField.MAP_WIDTH);
					y = (int)(Math.random() * BattleField.MAP_HEIGHT);
					attempts++;
				} while (battlefield.getUnit(x, y) != null && attempts < 10);

				// If we didn't find an empty spot, we won't add the new player
				if (battlefield.getUnit(x, y) != null) continue;

				final int finalX = x;
				final int finalY = y;

				if (battlefield.getUnit(x, y) == null) {
					/* Create the new player in a separate
					 * thread, making sure it does not 
					 * block the system.
					 */
					new Thread(new Runnable() {
						public void run() {
							try {
								Client c = new Client(centralManagerEP);
								c.connect();
								new Player(c, finalX, finalY);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					}).start();
					playerCount++;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/* Make sure both the battlefield and
		 * the socketmonitor close down.
		 */
		battlefield.shutdown();
		
		System.out.println("Sleeping for 2 seconds to allow all threads to finish.");
		try { Thread.sleep(2000); } catch (InterruptedException e1) { }
		
		try {
			java.rmi.server.UnicastRemoteObject.unexportObject(reg,true);
		} catch (NoSuchObjectException e) {
			// If it's already released, that's good
		}
		
		System.exit(0); // Stop all running processes
	}

	
}
