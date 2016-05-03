package org.fantasy.bean.registry.zookeeper;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.fantasy.bean.registry.RegistryKey;

public class ZkPath implements RegistryKey {
	
	private String path;
	private List<ACL> acls;
	private CreateMode createMode;
	private int version = -1;
	
	public ZkPath(String path) {
		this.path = path;
	}

	public ZkPath(String path, CreateMode createMode) {
		this(path, Ids.OPEN_ACL_UNSAFE, createMode);
	}

	public ZkPath(String path, List<ACL> acls, CreateMode createMode) {
		this(path, acls, createMode, -1);
	}
	
	public ZkPath(String path, List<ACL> acls, CreateMode createMode, int version) {
		this.path = path;
		this.acls = acls;
		this.createMode = createMode;
		this.version = version;
	}

	public List<ACL> getAcls() {
		return acls;
	}

	public void setAcls(List<ACL> acls) {
		this.acls = acls;
	}

	public CreateMode getCreateMode() {
		return createMode;
	}

	public void setCreateMode(CreateMode createMode) {
		this.createMode = createMode;
	}

	public String getKey() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	
}
