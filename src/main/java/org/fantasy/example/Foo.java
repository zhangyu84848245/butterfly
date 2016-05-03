package org.fantasy.example;

import org.fantasy.bean.annotation.Consumer;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.annotation.RpcMethod;

/**
 * 
 * @author fantasy
 *
 */
@Provider(id= "foo", refClass="org.fantasy.example.FooImpl")
@Consumer(id="foo")
public interface Foo {

	@RpcMethod
	public String bar(String hello);
	@RpcMethod
	public void hello();
}
