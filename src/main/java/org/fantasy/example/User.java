package org.fantasy.example;

import java.io.Serializable;

public class User implements Serializable {

	private static final long serialVersionUID = -1037883824526548605L;

	private int userId;
	private String userName;
	private String password;
	private UserProfile userProfile;
	
	public User() {
		
	}
	
	public User(int userId, String userName, String password) {
		this.userName = userName;
		this.password = password;
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public int getUserId() {
		return userId;
	}
	
	
}
