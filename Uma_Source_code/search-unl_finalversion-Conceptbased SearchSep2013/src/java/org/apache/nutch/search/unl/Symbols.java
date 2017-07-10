package org.apache.nutch.search.unl;

import org.apache.nutch.util.NutchConfiguration;
import org.apache.hadoop.conf.Configuration;

public class Symbols {	
	//for filtering symbol
	public static final String[] SYMBOLS = {"!", "@", "#", "$", "%", "^", "&", "_", "{", "}", "=", "[", "]", "|", "/", "\\", "\"", ">", "<", ";", ":", "'", ",", ".", "~", "`","?", "-", "*", "+" ,"(", ")"};
	public static final String WHITE_SPACE =" ";
	public static final String SEMICOLON =";";
	public static final String HASH ="#";
	public static final String OPENING_BRACE ="(";
	public static final String SYMBOL_AND ="&";
	public static final String CLOSING_BRACE =")";
	public static final String LN_TAMIL ="ta";
	public static final String LN_ENGLISH ="en";
	//public static final String MULTIWORDLIST = "/CliaIITKGP/resource/unl/multiwords.txt";
	//public static final String DICT_PATH = "/CliaIITKGP/resource/unl/en/Dictionary/";
	//public static final String DICT_LIST_PATH = "/CliaIITKGP/resource/unl/en/Dictionary/dict_list.txt";
	
	public static Configuration conf = NutchConfiguration.create();
	public static final String MULTIWORDLIST = conf.get("MultiwordDict") + "multiwords.txt"; 
	public static final String DICT_PATH = conf.get("EnglishDict");
	public static final String DICT_LIST_PATH = conf.get("EnglishDict") + "dict_list.txt";
	
}
