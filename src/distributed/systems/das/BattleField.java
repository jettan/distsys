package distributed.systems.das;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import distributed.systems.das.units.Unit.UnitType;
import distributed.systems.das.units.IUnit;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.endpoints.EndPoint;

/**
 * The actual battlefield where the fighting takes place.
 * It consists of an array of a certain width and height.
 * 
 * It is a singleton, which can be requested by the 
 * getBattleField() method. A unit can be put onto the
 * battlefield by using the putUnit() method.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public class BattleField extends UnicastRemoteObject implements IMessageReceivedHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* The array of units */
	private IUnit[][] map;
	
	/* The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0;

	public final static String serverID = "server";
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private ArrayList <IUnit> units; 

	/**
	 * Initialize the battlefield to the specified size 
	 * @param width of the battlefield
	 * @param height of the battlefield
	 * @throws AlreadyAssignedIDException 
	 */
	public BattleField(int width, int height) throws RemoteException, AlreadyAssignedIDException {
		map = new IUnit[width][height];
		units = new ArrayList<IUnit>();
	}
	
	/**
	 * Puts a new unit at the specified position. First, it
	 * checks whether the position is empty, if not, it
	 * does nothing.
	 * In addition, the unit is also put in the list of known units.
	 * 
	 * @param unit is the actual unit being spawned 
	 * on the specified position.
	 * @param x is the x position.
	 * @param y is the y position.
	 * @return true when the unit has been put on the 
	 * specified position.
	 */
	private boolean spawnUnit(IUnit unit, int x, int y)
	{
		if (map[x][y] != null)
			return false;

		rawSetUnit(x, y, unit);

		try {
			unit.setPosition(x, y);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		addUnit(unit);

		return true;
	}

	/**
	 * Put a unit at the specified position. First, it
	 * checks whether the position is empty, if not, it
	 * does nothing.
	 * 
	 * @param unit is the actual unit being put 
	 * on the specified position.
	 * @param x is the x position.
	 * @param y is the y position.
	 * @return true when the unit has been put on the 
	 * specified position.
	 */
	private boolean putUnit(IUnit unit, int x, int y)
	{
		if (map[x][y] != null)
			return false;

		rawSetUnit(x, y, unit);
		try {
			unit.setPosition(x, y);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Get a unit from a position.
	 * 
	 * @param x position.
	 * @param y position.
	 * @return the unit at the specified position, or return
	 * null if there is no unit at that specific position.
	 */
	public IUnit getUnit(int x, int y)
	{
		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;

		try {
			if (map[x][y] != null && map[x][y].getHitPoints() <= 0)
				removeUnit(x, y);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return map[x][y];
	}

	/**
	 * Move the specified unit a certain number of steps.
	 * 
	 * @param unit is the unit being moved.
	 * @param deltax is the delta in the x position.
	 * @param deltay is the delta in the y position.
	 * 
	 * @return true on success.
	 */
	private boolean moveUnit(IUnit unit, int newX, int newY)
	{
		int originalX = -1;
		try {
			originalX = unit.getX();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		int originalY = -1;
		try {
			originalY = unit.getY();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		try {
			if (unit.getHitPoints() <= 0)
				return false;
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null) {
					if (rawMoveUnit(newX, newY, originalX, originalY, unit)){
						try {
							unit.setPosition(newX, newY);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						return true;
					}
				}

		return false;
	}

	/**
	 * Remove a unit from a specific position and makes the unit disconnect from the server.
	 * 
	 * @param x position.
	 * @param y position.
	 */
	private void removeUnit(int x, int y)
	{
		IUnit unitToRemove = map[x][y];
		if (unitToRemove == null)
			return; // There was no unit here to remove
		rawSetUnit(x, y, null);
		try {
			unitToRemove.disconnect();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		removeUnit(unitToRemove);
	}

	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

	public void onMessageReceived(Message msg) throws RemoteException{
		try {
			final EndPoint client = new EndPoint(RemoteServer.getClientHost(), 1099, (String)msg.get("origin"));
			final Message fwd = msg;
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					onMessageReceived(fwd, client);
				}
			});
			t.start();
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
			throw new RemoteException();
		}
	}
	
	public void onMessageReceived(final Message msg, final EndPoint ep) {
		Message reply = new Message();
		MessageRequest request = (MessageRequest)msg.get("request");
		IUnit unit;
		switch(request)
		{
			case spawnUnit:
			{
				reply.put("id", msg.get("id"));
				reply.put("committed", spawnUnit((IUnit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y")));
				break;
			}
			case putUnit:
			{
				reply.put("committed", putUnit((IUnit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y")));
				break;
			}
			case getUnit:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				// Get the unit at the specific location
				reply.put("unit", getUnit(x, y));
				break;
			}
			case getType:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				try {
					IUnit unt = getUnit(x, y);
					if (unt != null){
						UnitType ret = unt.getMyType();
						reply.put("type", ret.ordinal());
					} else
						reply.put("type", UnitType.undefined.ordinal());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			}
			case dealDamage:
			{
				reply = null;
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null)
					try {
						unit.adjustHitPoints( -(Integer)msg.get("damage") );
						adjustedHitpoints(unit.getUnitID(), -(Integer)msg.get("damage"));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				break;
			}
			case healDamage:
			{
				reply = null;
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null)
					try {
						unit.adjustHitPoints( (Integer)msg.get("healed") );
						adjustedHitpoints(unit.getUnitID(), (Integer)msg.get("healed"));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				break;
			}
			case moveUnit:
			{
				reply.put("committed", moveUnit((IUnit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y")));
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				break;
			}
			case removeUnit:
			{
				reply = null;
				this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
				return;
			}
		}

		if (reply != null) {
			final Message freply = reply;
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						IMessageReceivedHandler imrh = (IMessageReceivedHandler) ep.connect();
						imrh.onMessageReceived(freply);
					} catch (RemoteException | MalformedURLException | NotBoundException e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
	}

	/**
	 * Close down the battlefield. Unregisters
	 * the serverSocket so the program can 
	 * actually end.
	 */ 
	public void shutdown() {
		// Remove all units from the battlefield and make them disconnect from the server
		synchronized(units){
			for (IUnit unit : units) {
				try {
					unit.disconnect();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				try {
					unit.stopRunnerThread();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected boolean isEmpty(int x, int y){
		try {
			return map[x][y] == null || map[x][y].getHitPoints() <= 0;
		} catch (RemoteException e) {
			return map[x][y] == null;
		}
	}
	
	/**
	 * The raw setting of a Unit on a certain position
	 */
	public boolean rawSetUnit(int x, int y, IUnit unit){
		map[x][y] = unit;
		return true;
	}
	
	/**
	 * The moving of a Unit on a certain position
	 */
	public boolean rawMoveUnit(int x, int y, int originalX, int originalY, IUnit unit){
		map[originalX][originalY] = null;
		map[x][y] = unit;
		return true;
	}
	
	public void addUnit(IUnit unit){
		synchronized(units){
			units.add(unit);
		}
	}
	
	public void removeUnit(IUnit unit){
		synchronized(units){
			units.remove(unit);
		}
	}
	
	public void adjustedHitpoints(int unit, int amount){
		
	}

	public IUnit rawGetUnit(int x, int y) {
		return map[x][y];
	}
}
