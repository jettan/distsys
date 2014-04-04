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
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.endpoints.EndPoint;
import distributed.systems.example.LocalSocket;

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

	/* Primary socket of the battlefield */ 
	//private Socket serverSocket;
	
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
		//LocalSocket local = new LocalSocket();
		
		synchronized (this) {
			map = new IUnit[width][height];
			units = new ArrayList<IUnit>();
		}
		
	}

	/**
	 * Singleton method which returns the sole 
	 * instance of the battlefield.
	 * 
	 * @return the battlefield.
	 */
	/*public static BattleField getBattleField() {
		if (battlefield == null)
			try {
				battlefield = new BattleField(MAP_WIDTH, MAP_HEIGHT);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (AlreadyAssignedIDException e) {
				e.printStackTrace();
			}
		return battlefield;
	}*/
	
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
		synchronized (this) {
			if (map[x][y] != null)
				return false;
	
			map[x][y] = unit;
			try {
				unit.setPosition(x, y);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		units.add(unit);

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
	private synchronized boolean putUnit(IUnit unit, int x, int y)
	{
		if (map[x][y] != null)
			return false;

		map[x][y] = unit;
		try {
			unit.setPosition(x, y);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
	private synchronized boolean moveUnit(IUnit unit, int newX, int newY)
	{
		int originalX = -1;
		try {
			originalX = unit.getX();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int originalY = -1;
		try {
			originalY = unit.getY();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (unit.getHitPoints() <= 0)
				return false;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null) {
					if (putUnit(unit, newX, newY)) {
						map[originalX][originalY] = null;
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
	private synchronized void removeUnit(int x, int y)
	{
		IUnit unitToRemove = map[x][y];
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		try {
			unitToRemove.disconnect();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		units.remove(unitToRemove);
	}

	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

	public void onMessageReceived(Message msg) {
		Message reply = new Message();
		String origin = (String)msg.get("origin");
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
					// TODO Auto-generated catch block
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
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
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
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
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

		try {
			if (reply != null) {
				final EndPoint ep = new EndPoint(RemoteServer.getClientHost(), 1099, (String) msg.get("origin"));
				final Message freply = reply;
				Thread t = new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							IMessageReceivedHandler imrh = (IMessageReceivedHandler) ep.connect();
							imrh.onMessageReceived(freply);
						} catch (RemoteException | MalformedURLException | NotBoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				t.start();
			}
		}
		catch(ServerNotActiveException idnae)  {
			// Could happen if the target already logged out
		}
	}

	/**
	 * Close down the battlefield. Unregisters
	 * the serverSocket so the program can 
	 * actually end.
	 */ 
	public synchronized void shutdown() {
		// Remove all units from the battlefield and make them disconnect from the server
		for (IUnit unit : units) {
			try {
				unit.disconnect();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				unit.stopRunnerThread();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
