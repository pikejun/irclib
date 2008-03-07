package org.schwering.irc.manager;

import org.schwering.irc.manager.event.ConnectionAdapter;
import org.schwering.irc.manager.event.NumericEvent;

/**
 * Provides a mechanism to create a buffer in an IRC event handler that
 * collects subsequent IRC events and then firesa manager event. 
 * @author Christoph Schwering &lt;schwering@gmail.com&gt;
 * @since 2.00
 * @version 1.00
 */
abstract class NumericEventWaiter extends ConnectionAdapter implements Runnable {
	public static int MILLIS_SLEEP = 500;
	public static int MILLIS_STEP = 50;
	
	private Connection owner;
	private Thread thread;
	private boolean interrupt = false;
	private boolean sleepAgain = false;
	private long millis = MILLIS_SLEEP;
	
	public NumericEventWaiter(Connection owner) {
		this.owner = owner;
		owner.addConnectionListener(this);
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void numericErrorReceived(NumericEvent event) {
		if (handle(event)) {
			sleepAgain = true;
		} else {
			interrupt = true;
		}
	}

	public synchronized void numericReplyReceived(NumericEvent event) {
		if (handle(event)) {
			sleepAgain = true;
		} else {
			interrupt = true;
		}
	}
	
	protected abstract boolean handle(NumericEvent event);
	
	protected abstract void fire();

	public void run() {
		do {
			while (!interrupt) {
				try {
					Thread.sleep(millis);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		} while (getAndSetSleepAgain(false));
		synchronized (this) {
			owner.removeConnectionListener(this);
			fire();
		}
	}
	
	private synchronized boolean getAndSetSleepAgain(boolean v) {
		boolean b = sleepAgain;
		sleepAgain = v;
		return b;
	}
}