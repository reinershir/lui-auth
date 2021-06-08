package io.github.reinershir.auth.core.security.reqlimit;

import io.github.reinershir.auth.entity.RequestCount;

public interface RequestLimitStorage {

	public int addCount(String key,RequestCount requestCount);
	
	public RequestCount get(String key);
	
	public void newCount(String key);
}
