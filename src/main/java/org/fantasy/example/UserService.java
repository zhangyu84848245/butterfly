package org.fantasy.example;

import org.fantasy.bean.annotation.Consumer;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.annotation.RpcMethod;

@Provider(id="user", refClass="org.fantasy.example.UserServiceImpl")
@Consumer(id="user")
public interface UserService {
	@RpcMethod
	public User getUserById(int userId);
	@RpcMethod
	public void addUser(User user);
	@RpcMethod
	public boolean deleteUser(User user);
	
}
