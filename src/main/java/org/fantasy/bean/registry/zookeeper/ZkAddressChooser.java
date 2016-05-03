package org.fantasy.bean.registry.zookeeper;

import java.util.Collections;
import java.util.List;

import org.fantasy.net.server.Chooser;
import org.fantasy.util.StringUtils;

/**
 * zookeeper地址的选择器
 * eg: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
 * 先打乱顺序之后在顺序的获取地址
 * @author fantasy
 *
 */
public class ZkAddressChooser implements Chooser<String> {

	private List<String> addressList;
	private int index = -1;

	public ZkAddressChooser(String serverAddress) {
		this.addressList = StringUtils.tokenizeToList(serverAddress, ",");
		Collections.shuffle(addressList);
	}
	public String next() {
		++index;
		if(index == addressList.size())
			index = 0;
		
		return addressList.get(index);
	}

	
}
