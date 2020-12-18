package io.github.reinershir.auth.core.security.reqlimit.impl;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.github.reinershir.auth.core.security.reqlimit.RequestLimitStorage;
import io.github.reinershir.auth.entity.RequestCount;

public class MemoryStorage implements RequestLimitStorage{
	
	Cache<String,RequestCount> cache;
	public MemoryStorage(Long reuqestLimitTime) {
		cache = CacheBuilder.newBuilder().expireAfterAccess(10000+reuqestLimitTime,TimeUnit.MILLISECONDS).maximumSize(5000).build();
	}

	@Override
	public int addCount(String key,RequestCount requestCount) {
//		RequestCount reqCount = null;
//		try {
//			reqCount = this.cache.get(key, ()->{
//				RequestCount c = new RequestCount();
//				c.setCount(0);
//				c.setStartDate(new Date());
//				return c;
//			});
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//			return -1;
//		}
		if(requestCount!=null) {
			requestCount.setCount(requestCount.getCount()+1);
			this.cache.put(key, requestCount);
			return requestCount.getCount();
		}
		return -1;
	}

	@Override
	public RequestCount get(String key) {
		RequestCount reqCount = null;
		try {
			reqCount = this.cache.get(key, ()->{
				RequestCount c = new RequestCount();
				c.setCount(1);
				c.setStartDate(new Date());
				return c;
			});
			cache.put(key, reqCount);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
		return reqCount;
	}

	@Override
	public void newCount(String key) {
		RequestCount c = new RequestCount();
		c.setCount(1);
		c.setStartDate(new Date());
		this.cache.put(key, c);
	}

	
}
