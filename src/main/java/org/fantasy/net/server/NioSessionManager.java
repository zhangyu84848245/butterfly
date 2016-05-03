package org.fantasy.net.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class NioSessionManager implements Closeable {

	private List<NioSession> sessionList = new CopyOnWriteArrayList<NioSession>();
	private int numOfSession;
//	private Object lock = new Object();

	public NioSessionManager() {
		
	}

	public void removeSession(NioSession session) {
		sessionList.remove(session);
		synchronized(sessionList) {
			numOfSession--;
		}
	}

	public void addSession(NioSession session) {
		sessionList.add(session);
		synchronized(sessionList) {
			numOfSession++;
		}
	}
	
	public List<NioSession> getSessions() {
		return sessionList;
	}

	public int numOfSession() {
		return numOfSession;
	}

	public void close() throws IOException {
		for(Iterator<NioSession> iterator = sessionList.iterator();iterator.hasNext();) {
			NioSession session = iterator.next();
			session.close();
			session = null;
		}
	}
	
	
}
