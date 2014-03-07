package distributed.systems.core;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;

/**
 * A serversocket implementation
 */
public class SynchronizedSocket extends Socket {
	public SynchronizedSocket() {
		handlers = new ArrayList<IMessageReceivedHandler>();
		registeredSockets = new ConcurrentHashMap<String, Socket>();
	}

	public SynchronizedSocket(Socket localSocket) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void register(String serverid) throws AlreadyAssignedIDException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unRegister() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(Message reply, String origin)
			throws IDNotAssignedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveMessage(Message reply) {
		// TODO Auto-generated method stub
		
	}
}
