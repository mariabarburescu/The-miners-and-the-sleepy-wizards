import java.lang.InterruptedException;
import java.util.LinkedList;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
    private final LinkedList<Message> minerChannel = new LinkedList<>();
    private final LinkedList<Message> wizardChannel = new LinkedList<>();
    private boolean wizardWriting = false;
    private long currentThreadId;
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and wizards
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {
	    synchronized (minerChannel) {
	    	minerChannel.addLast(message);
	    	minerChannel.notify();
        }
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
        synchronized (minerChannel) {
        	while (minerChannel.isEmpty()) {
				try {
					minerChannel.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return minerChannel.removeFirst();
        }
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write to and miners
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageWizardChannel(Message message) {
        synchronized (wizardChannel) {
        	if (message.getData().equals("EXIT") || message.getData().equals("END")) {
        		return;
			}
        	while (wizardWriting && Thread.currentThread().getId() != currentThreadId) {
				try {
					wizardChannel.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
        	if (wizardWriting == false) {
        		wizardWriting = true;
        		currentThreadId = Thread.currentThread().getId();
        		wizardChannel.addLast(message);
			} else {
        		wizardChannel.addLast(message);
        		wizardWriting = false;
        		wizardChannel.notify();
			}
        }
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {
        synchronized (wizardChannel) {
        	while (wizardChannel.isEmpty()) {
				try {
					wizardChannel.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			int currentRoom;
        	int parentRoom;
        	String data;
			Message firstWizardMessage = wizardChannel.removeFirst();
			Message secondWizardMessage = wizardChannel.removeFirst();
			Message wizardMessageToSend;
        	parentRoom = firstWizardMessage.getCurrentRoom();
        	currentRoom = secondWizardMessage.getCurrentRoom();
        	data = secondWizardMessage.getData();
        	wizardMessageToSend = new Message(parentRoom, currentRoom, data);
        	return wizardMessageToSend;
        }
	}
}
