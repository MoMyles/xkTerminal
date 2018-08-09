package com.cetcme.xkterminal.netty.utils;

import java.util.concurrent.ConcurrentHashMap;

public class CacheUtil {

	private static ConcurrentHashMap<Object, ConcurrentHashMap<String, Object>> cacheMap = new ConcurrentHashMap<Object, ConcurrentHashMap<String, Object>>();

	public static ConcurrentHashMap<String, Object> getAll(Object cacheType) {
		return cacheMap.get(cacheType);
	}

	public static Object get(Object cacheType, String key) {
		synchronized (cacheMap) {
			if (cacheMap.containsKey(cacheType)) {
				return cacheMap.get(cacheType).get(key);
			}
		}
		return null;
	}

	public static ConcurrentHashMap<String, Object> get(Object cacheType) {
		synchronized (cacheMap) {
			if (cacheMap.containsKey(cacheType)) {
				return cacheMap.get(cacheType);
			}
		}
		return null;
	}

	public static void addCache(Object cacheType) {
		synchronized (cacheMap) {
			if (!cacheMap.containsKey(cacheType)) {
				ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();
				cacheMap.put(cacheType, cache);
			}
		}
	}
	
	public static void addCacheMap(Object cacheType, ConcurrentHashMap<String, Object> cache) {
		synchronized (cacheMap) {
			if (!cacheMap.containsKey(cacheType)) {
				CacheUtil.clear(cacheType);
				cacheMap.put(cacheType, cache);
			}
		}
	}
	
	public static String getKey(Object cacheType) {
		String key = null;
		synchronized (cacheMap) {
			if (cacheMap.containsKey(cacheType)) {
				ConcurrentHashMap<String, Object> cache = cacheMap.get(cacheType);
				if(cache.size() == 1){
					for(String cacheKey : cache.keySet()){
						key = cacheKey;
					}
				}
			}
		}
		return key;
	}

	public static void put(Object cacheType, String key, Object value) {
		synchronized (cacheMap) {
			if (!cacheMap.containsKey(cacheType)) {
				addCache(cacheType);
			}
			cacheMap.get(cacheType).put(key, value);
		}
	}

	public static void clearAll() {
		cacheMap.clear();
	}

	public static void clear(Object cacheType) {
		synchronized (cacheMap) {
			if (cacheMap.containsKey(cacheType)) {
				cacheMap.get(cacheType).clear();
			}
		}
	}

	public static void remove(Object cacheType, String key) {
		if (!cacheMap.containsKey(cacheType)) {
			return;
		}
		ConcurrentHashMap<String, Object> cache = cacheMap.get(cacheType);
		synchronized (cacheMap) {
			if (cache.containsKey(key)) {
				cache.remove(key);
			}
		}
	}

	public static void remove(Object cacheType) {
		if (!cacheMap.containsKey(cacheType)) {
			return;
		}
		
		synchronized (cacheMap) {
			if (cacheMap.containsKey(cacheType)) {
				cacheMap.remove(cacheType);
			}
		}
	}
}
