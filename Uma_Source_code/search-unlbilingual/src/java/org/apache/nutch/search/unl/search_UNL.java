
/**
 * Module Name :UNL based Searching and Ranking
 * 
 *------------------------------------------------------------------------------
 * Version History...
 * Version     :3.0
 * -----------------------------------------------------------------------------
 * Date                          : 13 march 2012
 * Search has been Changed to lucene, AND logic has been finetuned to include Multiwords.
 * NE preferences has been given at all level of UNL search
 * Spelling variations, frequently occuring concepts has been tackled to improve ranking
 * Ranking has bee improved by including URL based ranking
 */


package org.apache.nutch.search.unl;

import org.apache.nutch.unl.*;
import org.apache.nutch.analysis.unl.ta.*;
//import org.apache.nutch.analysis.unl.ta.database.*;
import java.lang.*;
import java.math.BigDecimal;
import java.io.*;
import java.util.*;



public class search_UNL implements Serializable
{
	public static BinarySearchTree bstcrc=new BinarySearchTree();//Initialization of Concept-Relation-Concept Binary Search Tree of Index
 	public static BinarySearchTree bstcr=new BinarySearchTree(); //Initialization of Concept-Relation Binary Search Tree of Index
 	public static BinarySearchTree bstc=new BinarySearchTree(); //Initialization of Concept Binary Search Tree of Index

	public HeadNode temp3=new HeadNode();			//Initialization of Head Node of Index Tree
	public ConceptNode cpt1=new ConceptNode();		//Initialization of Concept Node of Index Tree
	public ConceptToNode ToCpt1=new ConceptToNode();	//Initialization of ToConceptNode of Index Tree

	public BinaryNextNode bnextcc=new BinaryNextNode();	//Initialization of the BinaryNextNode class which contains the concepts that 									has same hash value details
	public BinaryNode bNodecc=new BinaryNode(); //Initialization of the BinaryNode class which contains the indexed 				

        boolean flag_singleword=false;

	public DocNode d1=new DocNode(); //Initialization of DocNode which contains document identifier of all indexed concepts

        public offline o=new offline();	//Initialization of

	nodeDetails node_details=null; //Initialization of sub class node_details contains all the retrieved documents details 

	public static Hashtable fileList = null; //Used to Retrieve document url using document identifier
	public static Hashtable conList = null; //Used to Retrieve document url using document identifier

	FinalLLImpl[]  llq=new FinalLLImpl[3]; //Multilist contains query translation output

	public static boolean check_index=true; //boolean check to avoid recursive loading of index for each user search 

	public static Hashtable hashtable_c,counter;//Used for query processing

	public static Hashtable cache=new Hashtable();//To maintain results in cache

	Hashtable table=new Hashtable();//To store only docid of two different concepts 

	Hashtable andFreq=new Hashtable(); //To store the frequency count of two different concepts

	TreeSet hlight=new TreeSet(); //TreeSet to specify which concept's needs to be highlighted in the snippet

	String c1pos,c2pos,tamil_c1,tamil_c2;//String that assigns concepts and it's equivalent tamil words 

	ArrayList rank,freqsort;//Arraylist for final ranking
	
	
	
	
	public void loadindex()
	{
		
		check_index = false;//once loaded boolean value will be false to avoid reloading of index for every time 

		try
	    	{   	
			FileInputStream fiscon=new FileInputStream("./crawl-unl/freqcon.ser");
			//File Input Stream for reading the URL of the document identifers 
			ObjectInputStream oiscon=new ObjectInputStream(fiscon);				
			conList=(Hashtable)oiscon.readObject();
			oiscon.close();//closing object stream 
			fiscon.close();//closing file stream
			
			
		}
		catch(Exception e)
		{
			conList = new Hashtable();	
			
			e.printStackTrace();//To print the run time exception			
		} 
		
	}
	/**
	 * This is the method for Searching UNL index.
	 *
	 * @param queryWord user query term 
	 * @return an ArrayList that consists of document identifier,ranktag value,snippet and summary
	 */
	public ArrayList crc_UNLSearch(String queryWord, FinalLLImpl[] llq) throws Exception
	{
		ArrayList rank3= new ArrayList(); //ArrayList which contains the Final result reside in this 
	LuceneUNLSearch LUS=new LuceneUNLSearch();
// ArrayList _searchC1C2(String indexDir, String con, String check, String tamil, String pos, String qtag, String mwtag, int uwconceptid, int uwtamilid, boolean flag_singleword)
TreeSet set1=new TreeSet();
		int limiter=0; //To limit total number of expanded words for search

		long startTime=0;
        long endTime=0;
        long startingtime =0;
        long endtime = 0;
	boolean con_Only=false;
          String conid="";
         String tamilid="";
hashtable_c=new Hashtable();
if(check_index)
{
loadindex();
}
         
        FileWriter fw = new FileWriter("/root/Desktop/moduletimeSearch.csv",true);
        fw.write(queryWord+", ");

		querywordHashtable(llq);
		TreeSet set=new TreeSet();

		String relation,c1_qtag,mw_tag,c2,to_con_tag,c1,c1rc2,c1c2=null;
		//String variable used for assigning tags of query word,concept word,multiwords
		boolean crccheck=false;
		
		startingtime = System.nanoTime();
		
		endtime=System.nanoTime();                
       		double result = endtime - startingtime;		
		result = result / (Math.pow(10, 9));    			
		fw.write(BigDecimal.valueOf(result)+", ");
		
		
		startingtime = System.nanoTime();
		try
		{
		//	llq=TamilQueryGraph.multilist_UNLQuery();//get translated query
			TreeSet c2concpt=new TreeSet();//Tree set for c2 concept (to_concept)
			
			////////////////////////System.out.printlnrintln("Concept Size"+llq[0].Conceptsize() );
			if(llq[0].Conceptsize() == 1)
			{
			flag_singleword = true;
			}
			//if(!flag_singleword){
			for(int i=0;i<1;i++) //Interating the search process for each query and it's expansion
			{
				//for(int k=0;k<llq[i].Conceptsize();k++)
				//{
				
				temp3=llq[i].head;//Intializing the head pointer for the concept node
				cpt1=temp3.colnext;//Assigning the multilist concept node pointer to traverse in vertical
				////////////////////System.out.printlnrintln("cpt value :"+cpt1);

				while(cpt1 != null)
				{
					                
						c1=cpt1.uwconcept.trim();//String that holds query concept
						c1pos=cpt1.poscheck.trim();//String that holds POS(Part Of Speech)tag
						ToCpt1=cpt1.rownext;//Assigning the multilist To concept node pointer to traverse in 										horizontal
						c1_qtag=cpt1.queryTag.trim();//String that holds query tag(QW/CW/EQW)
						mw_tag=cpt1.MWtag_Qw.trim();//String that holds Multiword query tag
						tamil_c1=cpt1.gn_word.trim();	//String that holds tamil word 
						//System.out.printlnrintln("Input for c results"+tamil_c1+c1+c1pos+c1_qtag);
						if(ToCpt1!=null && !ToCpt1.uwtoconcept.equals("None"))//null check
						{
						crccheck =true;
						while(ToCpt1 != null)//Traveral of c2(To concept) Node
						{	
						limiter++;
							
						relation=ToCpt1.relnlabel;
						//System.out.printlnrintln("The toUW concept"+ToCpt1.uwtoconcept.trim());
						
						String to_con=llq[i].getconcept_vs_conceptid_query(ToCpt1.uwtoconcept.trim());
						String[] toc=to_con.split("&");
						c2=toc[0].toString().trim();
						 conid=toc[1].toString().trim();
						 tamilid=toc[2].toString().trim();
						//System.out.printlnrintln("c2"+c2+"<--------conid---->"+conid+"\t"+tamilid);
						if(!c1.equals(c2)){

						to_con_tag=llq[i].gettagvalue(c2).trim();
						tamil_c2=llq[i].gettamilword(c2).trim();
						c2pos=llq[i].getentityofuw(c2).trim();
						
						//////////System.out.printlnrintln("c2tag"+to_con_tag+"conid"+tamil_c2+"\t"+c2pos);
						c2concpt.add(c2);
						////////////////////////System.out.printlnrintln(tamil_c1+c1pos+"*"+tamil_c2+c2pos);
						
									
						
					
						if(set1.add(c2))
						{
							

                                                       
							
							   if((!(c2pos.equals("Verb"))) && (!(c2pos.equals("Finite Verb"))&& (!(c2pos.equals("Adjective"))) && (!(c2pos.equals("Particle"))) && (!(c2pos.equals("Postposition")))&&(!(c2pos.equals("Adjectival Noun"))) ))
							{
							
						if(!conList.containsKey(c2)){
						//System.out.printlnrintln("Inside c2 only"+c2);
LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",c2,"c2",tamil_c2,c2pos,to_con_tag,mw_tag,Integer.parseInt(conid),tamilid,flag_singleword,queryWord);
						
						}
						
							
							}
                                                      
						}
						if(set1.add(c1))
						{
							
                                                    
							   if((!(cpt1.poscheck.equals("Verb"))) && (!(cpt1.poscheck.equals("Finite Verb"))&& (!(cpt1.poscheck.equals("Adjective"))) && (!(cpt1.poscheck.equals("Particle"))) && (!(cpt1.poscheck.equals("Postposition"))) && (!(cpt1.poscheck.equals("Adjectival Noun")))))
							{
							if(!conList.containsKey(c1)){
	//System.out.printlnrintln("Inside c1 only"+c1);
						
LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",c1,"c1",tamil_c1,cpt1.poscheck,cpt1.queryTag,cpt1.MWtag_Qw.trim(),Integer.parseInt(cpt1.con_uid),cpt1.tam_uid,flag_singleword,queryWord);
}						
							
							}
                                                       
						}
						

                                  //System.out.printlnrintln(tamil_c1+"*"+c1+relation+"*"+c2+tamil_c2+"*"+c1_qtag+"*"+to_con_tag+"*"+mw_tag);
						
						if(!c1pos.equals("Adjective")&&!c2pos.equals("Adjective")&&!c1pos.equals("Adjectival Noun")&&!c2pos.equals("Adjectival Noun"))
						{
					if(c1pos.equals("Entity")||c2pos.equals("Entity")){
if(!conList.containsKey(c1)&&!conList.containsKey(c2)){
						////System.out.printlnrintln("Inside CRC"+Integer.parseInt(cpt1.con_uid));
LUS._searchCRC("",tamil_c1,c1,relation,c2,tamil_c2,c1_qtag,to_con_tag,mw_tag,Integer.parseInt(cpt1.con_uid),queryWord);

}
						}
						}
						
						
						
						
					}//c1c2 equals check
						
				ToCpt1=ToCpt1.getRowNext();//increment the to-concept node pointer
				
					
				}//while tocpt
				}//if tocpt not null
				
				else
				{
				//con_Only = true;
				////System.out.printlnrintln("else");
				 if((!(cpt1.poscheck.equals("Verb"))) && (!(cpt1.poscheck.equals("Finite Verb"))&& (!(cpt1.poscheck.equals("Adjective"))) && (!(cpt1.poscheck.equals("Particle"))) && (!(cpt1.poscheck.equals("Postposition")))&& (!(cpt1.poscheck.equals("Adjectival Noun"))) ))
					{			
						
							/*if(!conList.containsKey(cpt1.uwconcept)&&!cpt1.uwconcept.contains("temple(icl>facilities)"))
							{
							if(set1.add(cpt1.uwconcept)){
							LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",cpt1.uwconcept,"c1",cpt1.gn_word.trim(),cpt1.poscheck,cpt1.queryTag,cpt1.MWtag_Qw.trim(),Integer.parseInt(cpt1.con_uid),cpt1.tam_uid,flag_singleword);
							}
							}	
							else
							{
								if(set1.add(cpt1.uwconcept)){
							LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",cpt1.uwconcept,"c1",cpt1.gn_word.trim(),cpt1.poscheck,cpt1.queryTag,cpt1.MWtag_Qw.trim(),Integer.parseInt(cpt1.con_uid),cpt1.tam_uid,flag_singleword);
							}
							}*/
								
						
						
						if(!hashtable_c.containsKey("Entity")&&cpt1.uwconcept.contains("temple(icl>facilities)"))
						{
						if(set1.add(cpt1.uwconcept)){
						LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",cpt1.uwconcept,"c1",cpt1.gn_word.trim(),cpt1.poscheck,cpt1.queryTag,cpt1.MWtag_Qw.trim(),Integer.parseInt(cpt1.con_uid),cpt1.tam_uid,flag_singleword,queryWord);
						}
						}
						else
						{
							
							if(!conList.containsKey(cpt1.uwconcept))
							{
							
							if(set1.add(cpt1.uwconcept)){
							//System.out.printlnrintln("No relation"+cpt1.uwconcept);
LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",cpt1.uwconcept,"c1",cpt1.gn_word.trim(),cpt1.poscheck,cpt1.queryTag,cpt1.MWtag_Qw.trim(),Integer.parseInt(cpt1.con_uid),cpt1.tam_uid,flag_singleword,queryWord);
							}
							}
							
							
						}
						
								
												
					}//if
				}//else		
				
		     		 cpt1=cpt1.getColNext();//increment the concept node pointer
				
			}//while of cpt
	
			//}//for
			
		} //for	
		//}//if for not single word query

				//If there is no relations exist between the concept do only the concept search in the index tree
		/*		//////////System.out.printlnrintln("con_Only"+con_Only);
		if(con_Only){
		


		
		for(int i=0;i<1;i++) //Interating the search process for each query and it's expansion
			{
				
				
					if(llq!=null){	
			
				temp3=llq[i].head;
				cpt1=temp3.colnext;
				
				while(cpt1 != null)
				{
						limiter++;
					//////////System.out.printlnrintln("Now llq is not null\t"+cpt1.uwconcept+cpt1.poscheck);
				
				        if((!(cpt1.poscheck.equals("Verb"))) && (!(cpt1.poscheck.equals("Finite Verb"))&& (!(cpt1.poscheck.equals("Adjective"))) && (!(cpt1.poscheck.equals("Particle"))) && (!(cpt1.poscheck.equals("Postposition")))&& (!(cpt1.poscheck.equals("Adjectival Noun"))) ))
					{
												
						
						
						
					
						
						
					
						if(!cpt1.uwconcept.contains("temple(icl>facilities)"))
						{
							if(!conList.containsKey(cpt1.uwconcept))
							{
							LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",cpt1.uwconcept,"c1",cpt1.gn_word.trim(),cpt1.poscheck,cpt1.queryTag,cpt1.MWtag_Qw.trim(),Integer.parseInt(cpt1.con_uid),cpt1.tam_uid,flag_singleword);
							}
						}
						else
						{
						LUS._searchC1C2("/root/Desktop/Lucene-UWDict-Index/",cpt1.uwconcept,"c1",cpt1.gn_word.trim(),cpt1.poscheck,cpt1.queryTag,cpt1.MWtag_Qw.trim(),Integer.parseInt(cpt1.con_uid),cpt1.tam_uid,flag_singleword);
						}
						
						
						
						
						
					
							
					}//if
					cpt1=cpt1.getColNext();
				}//
				
			}//for loop ends
			}
		
		}*/



	}
	 catch(Exception e)
	{
		////////////////////////////System.out.printlnrintln("Exception in CRC search"); 
		e.printStackTrace();
	}
	 endtime=System.nanoTime();                
     result = endtime - startingtime;		
	 result = result / (Math.pow(10, 9));    			
	 fw.write(BigDecimal.valueOf(result)+",\n "); 

	 fw.close();
	rank3=LUS.getResult();
	
		
		return rank3;
	}// method end



	
	/**
	 * This method used to check actual query term and it's concepts 
	 * @param tamilword String tamil word of the term in the index
	 * @param givenword actual query term given by user
	 * @return boolean
	 */
	public boolean tamilcheck(String tamilword,String givenword)
	{
		if(tamilword.equals(givenword))
		{
			return true;
		}
		return false;
	}
	/**
	 * This method used to check for the c1,c2 concepts are Named Entity or Not 
	 * @param poscheck
	 * @return boolean
	 */
	public boolean Necheck(String poscheck)
	{
		if(poscheck.equals("Entity")||poscheck.equals("Non Tamil Noun"))
		{
			return true;
		}
		return false;
	}
	/**This method return the Document Node details of the matched query term 
	 * @return docDetail_bnode object of matched query term document id,pos weight,freqency weight 
	 */
	public Object[] DocNodeDetails()
	{
		TreeSet set=new TreeSet();		
		Object[] docDetail_bnode = new Object[12];
		docDetail_bnode[0] = node_details.frequency; //frequency
		docDetail_bnode[1] ="";	//
		docDetail_bnode[2] =node_details.docid; //docid
		docDetail_bnode[3] =node_details.ind_tamil; //tamilword
		docDetail_bnode[4] =""; //ranktag
		docDetail_bnode[5] =node_details.con; //concept
		docDetail_bnode[6] =node_details.senid; //sentid
		docDetail_bnode[7] =""; //querytag
		docDetail_bnode[8] =""; //NE chk
		docDetail_bnode[9] =""; //for snippet highlight

		docDetail_bnode[10] =node_details.tocon; 
		docDetail_bnode[11] =node_details.toconTamil;
		
		
		return docDetail_bnode;
	}

	
	
	
	/***
	 * To put the query terms which are verb or finite verb
	 */
	public void querywordHashtable(FinalLLImpl[] llq)
	{
				
							
				temp3=llq[0].head;
				cpt1=temp3.colnext;
				
				while(cpt1 != null)
				{
						
					
				
				     if((!(cpt1.poscheck.equals("Verb"))) && (!(cpt1.poscheck.equals("Finite Verb"))))
					{
						hashtable_c.put(cpt1.poscheck,cpt1.gn_word);
					//	////////////////////////System.out.printlnrintln("hashtable"+cpt1.uwconcept+cpt1.gn_word);
						//////////////////////////////////System.out.printlnrintln("size of hashtable"+hashtable_c.size());
					}	//if
					cpt1=cpt1.getColNext();
					//////////////////////////////////System.out.printlnrintln("Hashtable="+hashtable_c);
				}//while
			
		
	}
	/**
	 * Method for getting the actual tamil term of the concept 2(c2)
	 * @param uwconcept c2 concept as string
	 * @return the actual tamil term of the c2 concept
	 */
	public String getQW(String uwconcept)
	{
			////////////////////System.out.printlnrintln("uwconcept"+uwconcept);
			return ((String)hashtable_c.get(uwconcept));
	}
	/**
	 * Get the Hit results from the cache
	 * @param query actual query term of the concept
	 * @return list of ranked results
	 */
	public static ArrayList catcheHit(String query)
	{
		Enumeration names=null;
		ArrayList list=null;
		int counter=0;
		//////////////////////////////////System.out.printlnrintln("Cachehit method is called....");
		if(cache.size()!=0)
		{
			if(cache.containsKey(query))
			{
				//////////////////////////////////System.out.printlnrintln("Query exist in cache..");
				Hashtable value=(Hashtable)cache.get(query);
				names = value.keys(); 
				if(names.hasMoreElements())
				{
					String str=(String)names.nextElement();
					list =(ArrayList)value.get(str);
					//////////////////////////////////System.out.printlnrintln("Query exist in cache.."+list.size());
					counter=Integer.parseInt(str);
					counter++;
					value.put(counter,list);
				}
			}
		}
		
		return list;
	}
	/**
	 * This method used to seperate the tag for matrix display of the user interface
	 * @param list final ranked list of matched documents
	 * @return finalList final list of ranked documents seperated in different tag value
	 */
		public static ArrayList seperateTag(ArrayList list)
	{
		//Changes given to get the conceptual results
		ArrayList finalList=new ArrayList();
		ArrayList tag1=new ArrayList();
		ArrayList tag23=new ArrayList();
		ArrayList tag4=new ArrayList();
		ArrayList tag5=new ArrayList();
		ArrayList tag67=new ArrayList();
		for(int i=0;i<list.size();i++)
		{
			Object obj[]=(Object[])list.get(i);
			
			String tag=obj[3].toString();
			
			if(tag.startsWith("1"))
			{
				tag1.add(obj);
			}
			else if(tag.startsWith("2")||tag.startsWith("3"))
			{
				tag23.add(obj);
			}
			else if(tag.startsWith("4"))
			{
				tag4.add(obj);
			}
			else if(tag.startsWith("5"))
			{
				
				tag5.add(obj);
			}
			else if(tag.startsWith("6") || tag.startsWith("7"))
			{
				tag67.add(obj);
			}
			else
			{
				////////////////////////////System.out.printlnrintln("No match..");
			}

		}
		//if(tag1.size()!=0)
		{
			//////////////System.out.printlnrintln("Inside First Tag");
			finalList.add(tag1);
		}
		//if(tag23.size()!=0)
		{
			//////////////System.out.printlnrintln("Inside Second Tag");
			finalList.add(tag23);
		}
		//if(tag4.size()!=0)
		{
			//////////////System.out.printlnrintln("Inside Fourth Tag");
			finalList.add(tag4);
		}
	//if(tag56.size()!=0)
		{
			//////////////System.out.printlnrintln("Inside Fifthandsixth Tag");
			finalList.add(tag5);
		}
		//if(tag7.size()!=0)
		{
			//////////////System.out.printlnrintln("Inside Seventh Tag");
			finalList.add(tag67);
		}
		
		return finalList;
	}
		
}
/**
 * 
 * @author Umamaheswari E
 * This class contains the details of node for the matched concept in the index tree
 *
 */
class nodeDetails
{
	public nodeDetails()
	{
		con = null;
        	rel = null;
		tocon = null;
        	ind_tamil = null;
        	docid = null;
        	senid = null;
        	weight = 0;
        	frequency = 0;
		toconTamil=null;
	}
	public String con;
    	public String rel;
	public String tocon;
    	public String ind_tamil;
    	public String docid;
    	public String senid;
    	public int weight;
    	public double frequency;
	public double ranktag;
	public String toconTamil;
	public String mw;
	
	/**
	 * 
	 * @param wight concept position weight
	 * @param freq frequency count of concepts and terms
	 * @param dociD document identifier
	 * @param relation relation identifier
	 * @param concpt query concept
	 * @param sentid position of the concept in a sentence 
	 * @param in_tamil actual query term of the conept
	 */
    	public void construtObject(int wight,int freq,String dociD,String relation,String concpt,String toconcept,String sentid,String in_tamil,String totamil,String mwtag)
    	{
		//////////////////System.out.printlnrintln(" Construct Object"+wight+"\t"+frequency+"\t"+dociD+"\t"+relation+"\t"+concpt+"\t"+toconcept+"\t"+sentid+"\t"+in_tamil+"\t"+totamil);
    		con = concpt;
        	rel = relation;
		tocon = toconcept;
        	ind_tamil = in_tamil;
        	docid = dociD;
        	senid = sentid;
        	weight = wight;
        	frequency = freq;
		toconTamil = totamil;
		mw=mwtag;
		
		
	}
}
