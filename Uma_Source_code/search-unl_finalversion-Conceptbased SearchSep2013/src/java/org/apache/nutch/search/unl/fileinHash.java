package org.apache.nutch.search.unl;
import java.io.*;
import java.util.*;

public class fileinHash{
void getfile(){
try{
File f=new File("/root/furl_list1.txt");
File f1=new File("/root/20120904120738.txt");
FileWriter fw=new FileWriter(f1);
BufferedReader br=new BufferedReader(new FileReader(f));
String str=null;
int j=0;
while((str=br.readLine())!=null)
{
//System.out.println(str);
String s_arr[]=str.split(";");
//System.out.println(s_arr[1]);
if(s_arr[1].contains("20120904120738")){
++j;
System.out.println(s_arr[0]);
fw.append(s_arr[0]+"\n");
}
}
System.out.println(j);
fw.close();
}catch(Exception e){}
}

public static void main(String args[]){
fileinHash fiH=new fileinHash();
fiH.getfile();
}
}
