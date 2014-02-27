package distributed.systems.core;

/**
 * Interface for classes that can handle incoming Message
 * objects.
 */
public interface IMessageReceivedHandler {

	/**
	 * Receive a message
	 * 
	 * @param message The message that was sent to us
	 */
	public void onMessageReceived(Message message);
	
}
