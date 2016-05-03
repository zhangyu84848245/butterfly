package org.fantasy.example;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {

	private List<User> userList = new ArrayList<User>();
	public User getUserById(int userId) {
		UserProfile profile = new UserProfile();
		profile.setSex((byte)1);
		profile.setAge((short)33);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			profile.setBirthday(format.parse("1983-02-26"));
		} catch (ParseException e) {
			
		}
		User user = new User(1, "fantasy", "123456");
		user.setUserProfile(profile);
		return user;
	}

	public void addUser(User user) {
		userList.add(user);
	}

	public boolean deleteUser(User user) {
		userList.remove(user);
		return true;
	}

	
}
