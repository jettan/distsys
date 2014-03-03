package distributed.systems.example;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;

/**
 * A localhost socket
 */
public class LocalSocket extends Socket {
	
	private java.net.Socket socket = null;
	private int servernum = -1;
	private final static int BASE_PORT = 26000;
	
	/**
	 * Claim a serverid name for ourselves
	 * 
	 * @param serverid The id to claim (expected to be an integer in String form)
	 * @throws AlreadyAssignedIDException if the socket could not be bound or the id already exists
	 */
	public void register(String serverid) throws AlreadyAssignedIDException{
		servernum = Integer.parseInt(serverid);
		java.net.Socket socket = null;
		try {
			socket = new java.net.Socket();
			socket.setKeepAlive(true);
			socket.setReuseAddress(true);
			//Bind later, otherwise reuseAddress and keepAlive are ignored
			socket.bind(new InetSocketAddress("127.0.0.1", BASE_PORT+servernum));
			claim(serverid, this);
		} catch (UnknownHostException e) {
			throw new AlreadyAssignedIDException();
		} catch (IOException e) {
			throw new AlreadyAssignedIDException();
		} catch (AlreadyAssignedIDException e){
			unRegister();
			throw new AlreadyAssignedIDException();
		}
	}

	/**
	 * Release our lease on the server id
	 */
	@Override
	public void unRegister() {
		release("" + servernum);
		servernum = -1;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message over our socket
	 * 
	 * DOES NOT CLOSE THE SOCKET <- Kind of important, everything else depends on this
	 */
	@Override
	public void sendMessage(Message reply, String origin)
			throws IDNotAssignedException {
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(reply);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(Message reply) {
		// TODO Auto-generated method stub
		
	}

}
