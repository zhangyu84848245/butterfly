package org.fantasy.example;

import java.io.IOException;

import org.fantasy.bean.bootstrap.server.ServerBootstrap;

public class ServerStarter {

	public static void main(String[] args) {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.start();
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
