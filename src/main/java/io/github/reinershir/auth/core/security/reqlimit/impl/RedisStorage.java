package io.github.reinershir.auth.core.security.reqlimit.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.core.security.reqlimit.RequestLimitStorage;
import io.github.reinershir.auth.entity.RequestCount;
import io.github.reinershir.auth.utils.JacksonUtil;

public class RedisStorage implements RequestLimitStorage{
	
	RedisTemplate<String,String> redisTemplate;
	Long reuqestLimitTime;
	public RedisStorage(RedisTemplate<String,String> redisTemplate,Long reuqestLimitTime) {
		this.redisTemplate=redisTemplate;
		this.reuqestLimitTime=reuqestLimitTime+10000;
	}

	@Override
	public int addCount(String key,RequestCount requestCount) {
//		String json = redisTemplate.opsForValue().get(key);
//		RequestCount c;
//		if(!StringUtils.isEmpty(json)) {
//			c = JacksonUtil.readValue(json, RequestCount.class);
//			
//			return c.getCount();
//		}else {
//			c = new RequestCount();
//			c.setCount(1);
//			c.setStartDate(new Date());
//		}
		requestCount.setCount(requestCount.getCount()+1);
		redisTemplate.opsForValue().set(key, JacksonUtil.toJSon(requestCount));
		redisTemplate.expire(key, reuqestLimitTime,TimeUnit.MILLISECONDS);
		return requestCount.getCount();
	}

	@Override
	public RequestCount get(String key) {
		String json = redisTemplate.opsForValue().get(key);
		RequestCount c;
		if(!StringUtils.isEmpty(json)) {
			c = JacksonUtil.readValue(json, RequestCount.class);
		}else {
			c = new RequestCount();
			c.setCount(1);
			c.setStartDate(new Date());
			redisTemplate.opsForValue().set(key, JacksonUtil.toJSon(c));
			redisTemplate.expire(key, reuqestLimitTime,TimeUnit.MILLISECONDS);
		}
		return c;
	}

	@Override
	public void newCount(String key) {
		RequestCount c = new RequestCount();
		c.setCount(1);
		c.setStartDate(new Date());
		redisTemplate.opsForValue().set(key, JacksonUtil.toJSon(c));
		redisTemplate.expire(key, reuqestLimitTime,TimeUnit.MILLISECONDS);
		
	}

}
