package org.apache.nutch.search.unl;
import java.util.*;
class cacheResult
{
	private ArrayList result=new ArrayList();
	private int counter=0;
	private int minvalue=0;
	private int temp=0;
	private String removekey=null;
	//private ArrayList lruList=new ArrayList();
	//private Hashtable cacheTable=new Hashtable();
	public void setResult(ArrayList res)
	{
		result=res;
	}
	public ArrayList getResult()
	{
		return result;
	}
	public void setCounter(int count)
	{
		counter=count;
		counter++;
	}
	public int getCounter()
	{
		return counter;
	}
	/*public void setLRU(ArrayList list)
	{
		lruList=list;
	}
	public ArrayList getLRU()
	{
		return lruList;
	}
	public void setCachetable()
	{
		//cacheTable.put()
	}*/
	public void setMinvalue(int value,String key)
	{
		if(temp==minvalue)
		{
			minvalue=value;
			removekey=key;
			System.out.println("key "+key+"\tvalue"+minvalue);
		}
		if(minvalue>value)
		{
			minvalue=value;
			removekey=key;
			System.out.println("key "+key+"\tvalue"+minvalue);
		}
	}
	public String getKey()
	{
		return removekey;
	}
} 
