package org.apache.nutch.search.unl;
import java.util.*;
public class CacheManagement extends java.util.ArrayList
{
	
		
		//System.out.println("num of item removed is ="+noOfitemRemoved);
		//System.out.println("cache size after refreshing ="+InvokeUNLSearch.cachetable.size());
		
	public static void removeLeastReqFromcache(Stack lruList)
	{
		
		cacheResult cache=new cacheResult();
		//int numOfitemRemoved=lruList.size()-5;
		for(int i=0;i<5;i++)
		{
			String key=(String)lruList.get(i);
			int value=CacheManagement.getMinReqValue(key);
			cache.setMinvalue(value,key);
		}
		String rmKey=cache.getKey();
		
		InvokeUNLSearch.cachetable.remove(rmKey);
		lruList.remove(rmKey);
		
		}
	public static int getMinReqValue(String key)
	{
		cacheResult res=(cacheResult)InvokeUNLSearch.cachetable.get(key);
		int value=res.getCounter();
		return value;
	}
}
