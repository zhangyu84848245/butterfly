package org.fantasy.example;

import java.io.IOException;

import org.fantasy.bean.bootstrap.client.ClientBootstrap;

public class ClientStarter {

	public static void main(String[] args) {
		ClientBootstrap bootstrap = new ClientBootstrap();
		bootstrap.start();
		UserService userService = (UserService)bootstrap.getBeanFactory().getBeanInstance("user");
		User user = userService.getUserById(1);
		userService.deleteUser(user);
		userService.addUser(user);
		Foo foo = (Foo)bootstrap.getBeanFactory().getBeanInstance("foo");
		foo.hello();
		foo.bar("hello");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
