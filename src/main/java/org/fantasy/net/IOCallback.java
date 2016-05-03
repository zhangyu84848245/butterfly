package org.fantasy.net;

import java.io.IOException;

public interface IOCallback<T> {

	void call(T object);
}
