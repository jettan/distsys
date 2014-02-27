package distributed.systems.core;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * We use a Serializable here instead of an Object
	 * to benefit from static type checking (instead of
	 * the NotSerializable runtime exception).
	 */
	private HashMap<String, Serializable> messageContent;
	
	/**
	 * Construct a new Message keystore
	 */
	public Message(){
		messageContent = new HashMap<String, Serializable>();
	}
	
	/**
	 * Get the value corresponding to a certain key
	 * 
	 * @param string The key to retrieve
	 * @return The value corresponding to the key
	 */
	public Serializable get(String string) {
		return messageContent.get(string);
	}

	/**
	 * Add a new key->value pair to our Message content
	 * 
	 * @param string The key
	 * @param object The corresponding value
	 */
	public void put(String string, Serializable object) {
		messageContent.put(string, object);
	}

}
