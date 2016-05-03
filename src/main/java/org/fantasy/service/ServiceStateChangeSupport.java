package org.fantasy.service;

import java.util.ArrayList;
import java.util.List;

public final class ServiceStateChangeSupport {

	private Service service;
	private List<ServiceStateChangeListener> listeners = new ArrayList<ServiceStateChangeListener>();
//	private ReentrantLock lock = new ReentrantLock();
	private Object objectLock = new Object();
	
	
	public ServiceStateChangeSupport(Service service) {
		this.service = service;
	}
	
	
	public void addStateChangeListener(ServiceStateChangeListener listener) {
		synchronized(objectLock) {
			if(!listeners.contains(listener))
				listeners.add(listener);
		}
//		try {
//			lock.lock();
//			if(!listeners.contains(listener))
//				listeners.add(listener);
//		} finally {
//			lock.unlock();
//		}
	}
	
	public boolean removeStateChangeListener(ServiceStateChangeListener listener) {
		synchronized(objectLock) {
			return listeners.remove(listener);
		}
//		try {
//			lock.lock();
//			return listeners.remove(listener);
//		} finally {
//			lock.unlock();
//		}
	}
	
	public List<ServiceStateChangeListener> getStateChangeListeners() {
		return listeners;
	}
	
	public void reset() {
		synchronized(objectLock) {
			listeners.clear();
		}
//		try {
//			lock.lock();
//			listeners.clear();
//		} finally {
//			lock.unlock();
//		}
	}
	
	
	public void fireStateChangeListener(Service service, STATE state) {
		ServiceStateChangeListener[] callbacks;
		synchronized(objectLock) {
			callbacks = listeners.toArray(new ServiceStateChangeListener[listeners.size()]);
		}
		ServiceStateChangeEvent event = new ServiceStateChangeEvent(service, state);
		for(ServiceStateChangeListener listener : callbacks) {
			listener.stateChange(event);
		}
	}
}
