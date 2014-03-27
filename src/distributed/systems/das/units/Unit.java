package distributed.systems.das.units;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.das.BattleField;
import distributed.systems.das.GameState;
import distributed.systems.das.MessageRequest;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.example.LocalSocket;

/**
 * Base class for all players whom can 
 * participate in the DAS game. All properties
 * of the units (hitpoints, attackpoints) are
 * initialized in this class.
 *  
 * @author Pieter Anemaet, Boaz Pat-El
 */
public abstract class Unit implements UnitRef {
	private static final long serialVersionUID = -4550572524008491160L;

	// Position of the unit
	protected int x, y;

	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;

	// Identifier of the unit
	private int unitID;

	// The communication socket between this client and the board
	protected Socket clientSocket;
	
	// Map messages from their ids
	private Map<Integer, Message> messageList;
	// Is used for mapping an unique id to a message sent by this unit
	private int localMessageCounter = 0;
	
	// If this is set to false, the unit will return its run()-method and disconnect from the server
	protected boolean running;

	/* The thread that is used to make the unit run in a separate thread.
	 * We need to remember this thread to make sure that Java exits cleanly.
	 * (See stopRunnerThread())
	 */
	protected Thread runnerThread;

	public enum Direction {
		up, right, down, left
	};
	
	public enum UnitType {
		player, dragon, undefined
	};
	
	protected UnitType myType = UnitType.undefined;

	/**
	 * Create a new unit and specify the 
	 * number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints. 
	 * 
	 * @param maxHealth is the maximum health of 
	 * this specific unit.
	 * @throws RemoteException 
	 */
	public Unit(int maxHealth, int attackPoints) throws RemoteException {
		Socket localSocket = new LocalSocket();

		messageList = new ConcurrentHashMap<Integer, Message>();

		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;

		// Get a new unit id
		unitID = BattleField.getBattleField().getNewUnitID();

		// Create a new socket
		clientSocket = new SynchronizedSocket(localSocket);

		try {
			// Try to register the socket
			clientSocket.register("D" + unitID);
		}
		catch (AlreadyAssignedIDException e) {
			System.err.println("Socket \"D" + unitID + "\" was already registered.");
		}

		IMessageReceivedHandler stub;
		try {
			stub = (IMessageReceivedHandler) java.rmi.server.UnicastRemoteObject.exportObject(this, 0);
			clientSocket.addMessageReceivedHandler(stub);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adjust the hitpoints to a certain level. 
	 * Useful for healing or dying purposes.
	 * 
	 * @param modifier is to be added to the
	 * hitpoint count.
	 */
	public synchronized void adjustHitPoints(int modifier) {
		if (hitPoints <= 0)
			return;

		hitPoints += modifier;

		if (hitPoints > maxHitPoints)
			hitPoints = maxHitPoints;

		if (hitPoints <= 0)
			removeUnit(x, y);
	}
	
	public void dealDamage(int x, int y, int damage) {
		/* Create a new message, notifying the board
		 * that a unit has been dealt damage.
		 */
		int id;
		Message damageMessage;
		synchronized (this) {
			id = localMessageCounter++;
		
			damageMessage = new Message();
			damageMessage.put("request", MessageRequest.dealDamage);
			damageMessage.put("x", x);
			damageMessage.put("y", y);
			damageMessage.put("damage", damage);
			damageMessage.put("id", id);
			damageMessage.put("origin", "D" + unitID);
		}
		
		// Send a spawn message
		try {
			clientSocket.sendMessage(damageMessage, "localsocket://" + BattleField.serverID);
		} catch (IDNotAssignedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void healDamage(int x, int y, int healed) {
		/* Create a new message, notifying the board
		 * that a unit has been healed.
		 */
		int id;
		Message healMessage;
		synchronized (this) {
			id = localMessageCounter++;

			healMessage = new Message();
			healMessage.put("request", MessageRequest.healDamage);
			healMessage.put("x", x);
			healMessage.put("y", y);
			healMessage.put("healed", healed);
			healMessage.put("id", id);
			healMessage.put("origin", "D" + unitID);
		}

		// Send a spawn message
		try {
			clientSocket.sendMessage(healMessage, "localsocket://" + BattleField.serverID);
		} catch (IDNotAssignedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints() {
		return maxHitPoints;		
	}

	/**
	 * @return the unique unit identifier.
	 */
	public int getUnitID() {
		return unitID;
	}

	/**
	 * Set the position of the unit.
	 * @param x is the new x coordinate
	 * @param y is the new y coordinate
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x position
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y position
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints() {
		return hitPoints;
	}

	/**
	 * @return the attack points
	 */
	public int getAttackPoints() {
		return attackPoints;
	}
	
	/**
	 * @return our unit type
	 */
	public UnitType getMyType() throws RemoteException{
		return myType;
	}

	/**
	 * Tries to make the unit spawn at a certain location on the battlefield
	 * @param x x-coordinate of the spawn location
	 * @param y y-coordinate of the spawn location
	 * @return true iff the unit could spawn at the location on the battlefield
	 */
	protected boolean spawn(int x, int y) {
		/* Create a new message, notifying the board
		 * the unit has actually spawned at the
		 * designated position. 
		 */
		
		int id = localMessageCounter++;
		Message spawnMessage = new Message();
		spawnMessage.put("request", MessageRequest.spawnUnit);
		spawnMessage.put("x", x);
		spawnMessage.put("y", y);
		spawnMessage.put("unit", (UnitRef) this);
		spawnMessage.put("id", id);
		spawnMessage.put("origin", "D" + unitID);
		
		// Send a spawn message
		try {
			clientSocket.sendMessage(spawnMessage, "localsocket://" + BattleField.serverID);
		} catch (IDNotAssignedException e) {
			System.err.println("No server found while spawning unit at location (" + x + ", " + y + ")");
			return false;
		}
		
		// Wait for the unit to be placed
		getUnit(x, y);
		
		return true;
	}
	
	/**
	 * Returns whether the indicated square contains a player, a dragon or nothing. 
	 * @param x: x coordinate
	 * @param y: y coordinate
	 * @return UnitType: the indicated square contains a player, a dragon or nothing.
	 */
	protected UnitType getType(int x, int y) {
		Message getMessage = new Message(), result;
		int id = localMessageCounter++;
		getMessage.put("request", MessageRequest.getType);
		getMessage.put("x", x);
		getMessage.put("y", y);
		getMessage.put("id", id);
		getMessage.put("origin", "D" + unitID);
		
		// Send the getUnit message
		try {
			clientSocket.sendMessage(getMessage, "localsocket://" + BattleField.serverID);
		} catch (IDNotAssignedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Wait for the reply
		while(!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return UnitType.undefined;
		}

		result = messageList.get(id);
		if (result == null) // Could happen if the game window had closed
			return UnitType.undefined;
		messageList.remove(id);
		
		return UnitType.values()[(Integer)result.get("type")];	
		
	}

	protected UnitRef getUnit(int x, int y)
	{
		Message getMessage = new Message(), result;
		int id = localMessageCounter++;
		getMessage.put("request", MessageRequest.getUnit);
		getMessage.put("x", x);
		getMessage.put("y", y);
		getMessage.put("id", id);
		getMessage.put("origin", "D" + unitID);

		// Send the getUnit message
		try {
			clientSocket.sendMessage(getMessage, "localsocket://" + BattleField.serverID);
		} catch (IDNotAssignedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Wait for the reply
		while(!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return null;
		}

		result = messageList.get(id);
		messageList.remove(id);

		return (UnitRef) result.get("unit");	
	}

	protected void removeUnit(int x, int y)
	{
		Message removeMessage = new Message();
		int id = localMessageCounter++;
		removeMessage.put("request", MessageRequest.removeUnit);
		removeMessage.put("x", x);
		removeMessage.put("y", y);
		removeMessage.put("id", id);
		removeMessage.put("origin", "D" + unitID);

		// Send the removeUnit message
		try {
			clientSocket.sendMessage(removeMessage, "localsocket://" + BattleField.serverID);
		} catch (IDNotAssignedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void moveUnit(int x, int y)
	{
		Message moveMessage = new Message();
		int id = localMessageCounter++;
		moveMessage.put("request", MessageRequest.moveUnit);
		moveMessage.put("x", x);
		moveMessage.put("y", y);
		moveMessage.put("id", id);
		moveMessage.put("unit", (UnitRef) this);
		moveMessage.put("origin", "D" + unitID);

		// Send the getUnit message
		try {
			clientSocket.sendMessage(moveMessage, "localsocket://" + BattleField.serverID);
		} catch (IDNotAssignedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Wait for the reply
		while(!messageList.containsKey(id))
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return;
		}

		// Remove the result from the messageList
		messageList.remove(id);
	}

	public void onMessageReceived(Message message) {
		messageList.put((Integer)message.get("id"), message);
	}
	
	// Disconnects the unit from the battlefield by exiting its run-state
	public void disconnect() {
		running = false;
	}

	/**
	 * Stop the running thread. This has to be called explicitly to make sure the program 
	 * terminates cleanly.
	 */
	public void stopRunnerThread() {
		try {
			if (runnerThread != null)
				runnerThread.join();
		} catch (InterruptedException ex) {
			assert(false) : "Unit stopRunnerThread was interrupted";
		}
		
	}
}
