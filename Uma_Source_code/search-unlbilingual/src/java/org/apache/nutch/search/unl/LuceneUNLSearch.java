package org.apache.nutch.search.unl;
import org.apache.nutch.unl.*;
import org.apache.nutch.analysis.unl.ta.*;
import org.apache.nutch.analysis.unl.ta.database.*;

import java.io.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.analysis.KeywordAnalyzer;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

public class LuceneUNLSearch extends SimpleFileIndexer {

    public Hashtable hashtable_c, counter;//Used for query processing
    ArrayList crc_QWQW_NElist, crc_QWCQW_NElist, crc_CQWQW_NElist, crc_CQWCQW_NElist, crc_QWQW_nonNElist, crc_QWCQW_nonNElist, crc_CQWQW_nonNElist, crc_CQWCQW_nonNElist;
    ArrayList crc_QWEQW_NElist, crc_EQWQW_NElist, crc_CQWEQW_NElist, crc_EQWCQW_NElist, crc_QWEQW_nonNElist, crc_EQWQW_nonNElist, crc_CQWEQW_nonNElist, crc_EQWCQW_nonNElist;
    ArrayList c1r_QW_NElist, c1r_CQW_NElist, c1r_EQW_NElist, c1r_QW_nonNElist, c1r_CQW_nonNElist, c1r_EQW_nonNElist;
    ArrayList c2r_QW_NElist, c2r_CQW_NElist, c2r_EQW_NElist, c2r_QW_nonNElist, c2r_CQW_nonNElist, c2r_EQW_nonNElist;
    ArrayList c1_QW_NElist, c1_CQW_NElist, c1_EQW_NElist, c1_QW_nonNElist, c1_CQW_nonNElist, c1_EQW_nonNElist;
    ArrayList c2_QW_NElist, c2_CQW_NElist, c2_EQW_NElist, c2_QW_nonNElist, c2_CQW_nonNElist, c2_EQW_nonNElist;
    ArrayList and_QWQW_list, and_QWCQW_list, and_CQWQW_list, and_CQWCQW_list, and_QWEQW_list, and_EQWQW_list, and_CQWEQW_list, and_EQWCQW_list;
    ArrayList crRelList, crcRelList, mwCList;
    Hashtable table = new Hashtable();//To store only docid of two different concepts 
    Hashtable andFreq = new Hashtable(); //To store the frequency count of two different concepts
    TreeSet hlight = new TreeSet(); //TreeSet to specify which concept's needs to be highlighted in the snippet
    String c1pos, c2pos, tamil_c1, tamil_c2;//String that assigns concepts and it's equivalent tamil words 
    ArrayList rank, freqsort;//Arraylist for final ranking
    TreeSet set = null;
    search_UNL SUNL = new search_UNL();
    public Hashtable fileList = null;
    public static Hashtable conList = null;
    public static Hashtable spellingList = null;
    ArrayList rank3 = null;
boolean check_files=true;
public static Configuration conf = NutchConfiguration.create();
	public static String c_index_path = conf.get("UNLConceptIndex");
public static String crc_index_path = conf.get("UNLConceptRelationIndex");
	public static String recnotourlpath=conf.get("RecnoToURLMap");
public static String freqconfpath=conf.get("Freqconpath" );
public static String spellingvariationspath=conf.get("spellingvariationspath");
RecnoToURL rectourl= new RecnoToURL();
    public ArrayList getResult() {
// ////////////System.ouln("The results in MWList are"+mwCList.size());

       

        //CRC
        andlogic(mwCList, c1_QW_NElist, "mw");
        andlogic(mwCList, c2_QW_NElist, "mw");
        andlogic(mwCList, c1_CQW_NElist, "mw");
        andlogic(mwCList, c2_QW_NElist, "mw");
        andlogic(c1_QW_NElist, c2_QW_NElist, "xy");
        andlogic(c1_QW_NElist, c2_CQW_NElist, "xy");
        andlogic(c1_CQW_NElist, c2_QW_NElist, "xy");
        andlogic(c1_CQW_NElist, c2_CQW_NElist, "xy");
        ArrayList conly_list = new ArrayList();
        conly_list.addAll(c1_QW_NElist);
        conly_list.addAll(c1_QW_nonNElist);
        conly_list.addAll(c2_QW_NElist);
        conly_list.addAll(c2_QW_nonNElist);
        conly_list.addAll(mwCList);
        //////////////System.ouln("Concept Only"+conly_list.size());

        andlogicforconly(conly_list);
        //AND results for concepts C1 and C2
        rank.addAll(and_QWQW_list);
        rank.addAll(and_QWCQW_list);
        rank.addAll(and_CQWQW_list);
        rank.addAll(and_CQWCQW_list);
 rank.addAll(mwCList);
        rank.addAll(crc_QWQW_NElist);
        rank.addAll(crc_QWCQW_NElist);
        rank.addAll(crc_CQWCQW_NElist);
        rank.addAll(c1_QW_NElist);
        rank.addAll(c2_QW_NElist);


        rank.addAll(c1_CQW_NElist);
        rank.addAll(c2_CQW_NElist);

        rank.addAll(c1_QW_nonNElist);
        rank.addAll(c2_QW_nonNElist);
        rank.addAll(c1_CQW_nonNElist);
        rank.addAll(c2_CQW_nonNElist);

        rank = tagSorting(rank);

        ArrayList rank1 = removeDuplicateWithOrder(rank);
       ArrayList rank2 = Freq_Sorting(rank1);
	    rank3 = getNodeLink(rank2, recnotourlpath);
       // rank3 = getNodeLink(rank2);
        rank3 = seperateTag(rank3);
        ArrayList resultis = new ArrayList();
        resultis.addAll(rank3);

        return rank3;
    }

    public LuceneUNLSearch() {
        hashtable_c = new Hashtable();
        freqsort = new ArrayList();
        counter = new Hashtable();
        rank = new ArrayList();
        freqsort = new ArrayList();
        crRelList = new ArrayList();
        crcRelList = new ArrayList();
        mwCList = new ArrayList();

        crc_QWQW_NElist = new ArrayList();
        crc_QWCQW_NElist = new ArrayList();
        crc_CQWQW_NElist = new ArrayList();
        crc_CQWCQW_NElist = new ArrayList();

        crc_QWQW_nonNElist = new ArrayList();
        crc_QWCQW_nonNElist = new ArrayList();
        crc_CQWQW_nonNElist = new ArrayList();
        crc_CQWCQW_nonNElist = new ArrayList();

        crc_QWEQW_NElist = new ArrayList();
        crc_EQWQW_NElist = new ArrayList();
        crc_CQWEQW_NElist = new ArrayList();
        crc_EQWCQW_NElist = new ArrayList();

        crc_QWEQW_nonNElist = new ArrayList();
        crc_EQWQW_nonNElist = new ArrayList();
        crc_CQWEQW_nonNElist = new ArrayList();
        crc_EQWCQW_nonNElist = new ArrayList();

        c1r_QW_NElist = new ArrayList();
        c1r_CQW_NElist = new ArrayList();
        c1r_EQW_NElist = new ArrayList();
        c1r_QW_nonNElist = new ArrayList();
        c1r_CQW_nonNElist = new ArrayList();
        c1r_EQW_nonNElist = new ArrayList();

        c2r_QW_NElist = new ArrayList();
        c2r_CQW_NElist = new ArrayList();
        c2r_EQW_NElist = new ArrayList();
        c2r_QW_nonNElist = new ArrayList();
        c2r_CQW_nonNElist = new ArrayList();
        c2r_EQW_nonNElist = new ArrayList();

        c1_QW_NElist = new ArrayList();
        c1_CQW_NElist = new ArrayList();
        c1_EQW_NElist = new ArrayList();
        c1_QW_nonNElist = new ArrayList();
        c1_CQW_nonNElist = new ArrayList();
        c1_EQW_nonNElist = new ArrayList();

        c2_QW_NElist = new ArrayList();
        c2_CQW_NElist = new ArrayList();
        c2_EQW_NElist = new ArrayList();
        c2_QW_nonNElist = new ArrayList();
        c2_CQW_nonNElist = new ArrayList();
        c2_EQW_nonNElist = new ArrayList();

        and_QWQW_list = new ArrayList();
        and_QWCQW_list = new ArrayList();
        and_CQWQW_list = new ArrayList();
        and_CQWCQW_list = new ArrayList();

        and_QWEQW_list = new ArrayList();
        and_EQWQW_list = new ArrayList();
        and_CQWEQW_list = new ArrayList();
        and_EQWCQW_list = new ArrayList();
        

    }
public void load_files()
{
	check_files = false; //boolean for loading files only once

        try {
            FileInputStream fiscon = new FileInputStream(freqconfpath);
            //File Input Stream for reading the URL of the document identifers 
            ObjectInputStream oiscon = new ObjectInputStream(fiscon);
            conList = (Hashtable) oiscon.readObject();
            oiscon.close();//closing object stream 
            fiscon.close();//closing file stream


        } catch (Exception e) {
            conList = new Hashtable();

            e.printStackTrace();//To print the run time exception			
        }
        try {
            FileInputStream fiscon = new FileInputStream(spellingvariationspath);
            //File Input Stream for reading the URL of the document identifers 
            ObjectInputStream oiscon = new ObjectInputStream(fiscon);
            spellingList = (Hashtable) oiscon.readObject();
            oiscon.close();//closing object stream 
            fiscon.close();//closing file stream


        } catch (Exception e) {
            spellingList = new Hashtable();

            e.printStackTrace();//To print the run time exception			
        }
	 
}

    public static void main(String[] args) throws Exception {
     
    }

   public ArrayList _searchC1C2(String indexDir, String con, String check, String tamil, String pos, String qtag, String mwtag, int uwconceptid, String uwtamilid, boolean flag_singleword, String q)  {
	if(check_files)
	{
	load_files();
	
	}
	int count =0;
        ArrayList<Document> arr = new ArrayList<Document>();
        boolean flag_tamil = false;
        boolean flag_NE = false;
        ArrayList<Document> nodeinfo = new ArrayList<Document>();




       
              flag_NE = SUNL.Necheck(pos);
		String synid=new String(uwtamilid+"");
            //    ArrayList<Document> docinfo = _searchCC(c_index_path, synid.trim(),"tamilwordid");
          //    ArrayList<Document> docinfo = _searchC(c_index_path, con.trim(),"uwconcept",SUNL.hashtable_c);
ArrayList<Document> docinfo = _searchC(c_index_path+"/"+(uwconceptid/10000)+"/", con,"uwconcept",5);
                for (int J = 0; J < docinfo.size(); J++) {

                    String dociddb = docinfo.get(J).get("documentid");
                    int freqdb = Integer.parseInt(docinfo.get(J).get("conceptfrequency")) + Integer.parseInt(docinfo.get(J).get("termfrequency"));
                    int weightdb = Integer.parseInt(docinfo.get(J).get("weight"));
                    String sentid = docinfo.get(J).get("sentenceid");
		   String tamilwordid=docinfo.get(J).get("tamilwordid");
			
		
                   flag_tamil = SUNL.tamilcheck(tamilwordid, uwtamilid);
                 
                   if(flag_tamil==true)
		{
                    nodeDetails node_details = new nodeDetails();
                    node_details.construtObject(weightdb, freqdb, dociddb, "", con, "", sentid,tamil, "",mwtag);
		    ////System.out.println("The list of Documents"+dociddb);
                    process(flag_tamil, flag_NE, check, qtag, mwtag, node_details, flag_singleword);
		}else
		{
			count++;
			//if(count<7)
			{
			nodeDetails node_details = new nodeDetails();
                    node_details.construtObject(weightdb, freqdb, dociddb, "", con, "", sentid,tamil, "",mwtag);
		    
                    process(flag_tamil, flag_NE, check, qtag, mwtag, node_details, flag_singleword);
			}
			
		}
		
                }
         
	         

	/*if(mwtag.equals("MW"))
	{
	  nodeinfo = _searchMW("/root/Desktop/Lucene-MWDict-Index/", con);
	}
	else
	{		
       nodeinfo = _searchUW("/root/Desktop/Lucene-UWDict-Index/", con);
	}
//////////////System.out.println("Outside: "+nodeinfo.size());

           for (int I = 0; I < nodeinfo.size(); I++) {
		
              int tamilid = Integer.parseInt(nodeinfo.get(I).get("tamilwordid"));
               String str_tamil = nodeinfo.get(I).get("synonym");
		String synid=new String(tamilid+"");
		//////////////System.out.println("Synonymidis:"+synid+str_tamil);
		if(spellingList.containsKey(str_tamil))
		{
		flag_tamil = true;
		}
		else
		{
                flag_tamil = SUNL.tamilcheck(str_tamil, tamil);
		}
                flag_NE = SUNL.Necheck(pos);
//////////////System.out.println(str_tamil+" "+synid);
                ArrayList<Document> docinfo = _searchCC(c_index_path, synid.trim(),"tamilwordid");
		//////////////System.out.println(""+docinfo);
                for (int J = 0; J < docinfo.size(); J++) {

////////////////System.out.println(("Inside: "+docinfo.get(J)).replace("stored/uncompressed,indexed", " ").replace("Document", "\n"));
                    int dociddb = Integer.parseInt(docinfo.get(J).get("documentid"));
                    int freqdb = Integer.parseInt(docinfo.get(J).get("conceptfrequency"))+Integer.parseInt(docinfo.get(J).get("termfrequency"));
                    int weightdb = Integer.parseInt(docinfo.get(J).get("weight"));
                    String sentid = docinfo.get(J).get("sentenceid");
		 //////////System.out.println("Concept"+con+dociddb+mwtag+pos+"\t"+docinfo.get(J).get("tamilwordid"));
                    nodeDetails node_details = new nodeDetails();
                    node_details.construtObject(weightdb, freqdb, dociddb, "", con, "", sentid, str_tamil, "",mwtag);
                    process(flag_tamil, flag_NE, check, qtag, mwtag, node_details, flag_singleword);
                }
          }//for loop closed
            */
	 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rank3;
    }
//#########SearchUNL
//

  public ArrayList _searchCRC(String indexDir, String c1tamil, String con, String relation, String tocon, String c2tamil, String c1_qtag, String to_con_tag, String mwtag, int toconid, String qword)  {
        ArrayList<Document> arr = new ArrayList<Document>();
        try {
            int fromconceptid = 0;
            int toconceptid = 0;
            boolean flagRelation = false;
            boolean tamilword_checkc1 = false;
            boolean nec1 = false;
            boolean tamilword_checkc2 = false;
            boolean nec2 = false;
            //int sentenceidcrc = 0;
            int crc_weight = 0;
            int crc_frequency = 0;
            String docid_crc = "";
            String sentid_crc = "";
            String ind_con = "";
            String ind_relation = "";
            String ind_tocon = "";
            String str_tamil = "";
            String str_tamil2 = "";
            int crcid = 0;
            TreeSet ts = new TreeSet();
//
          //  ArrayList<Document> nodeinfo = _searchCRC(crc_index_path , con, tocon);
  ArrayList<Document> nodeinfo = _searchCRC(crc_index_path+"/"+(toconid/10000)+"/", con, tocon);
            for (int I = 0; I < nodeinfo.size(); I++) {
                String tamil_wrdc1 = nodeinfo.get(I).get("fromtamilconcept");
                String tamil_wrdc2 = nodeinfo.get(I).get("totamilconcept");
                nec1 = Necheck(nodeinfo.get(I).get("frompos"));
                nec2 = Necheck(nodeinfo.get(I).get("topos"));
                String c2 = tocon;
                tamilword_checkc2 = tamilcheck(tamil_wrdc2, c2tamil);
                tamilword_checkc1 = tamilcheck(tamil_wrdc1, c1tamil);
                ind_relation = nodeinfo.get(I).get("relation");
                crc_weight = Integer.parseInt(nodeinfo.get(I).get("weight"));
                crc_frequency = Integer.parseInt(nodeinfo.get(I).get("termfrequency")) + Integer.parseInt(nodeinfo.get(I).get("conceptfrequency"));
                docid_crc = nodeinfo.get(I).get("documentid");
                sentid_crc = nodeinfo.get(I).get("sentenceid");
                nodeDetails node_details = new nodeDetails();

                //////////////System.ouln("CRC Information"+crc_weight+"\t"+crc_frequency+"\t"+docid_crc+"\t"+sentid_crc);
                try {
                    if (ts.add(docid_crc+ con + tocon)) {
                        node_details.construtObject(crc_weight, crc_frequency, docid_crc, relation, con, c2, sentid_crc, tamil_wrdc1, tamil_wrdc2,mwtag);
                        if ((ind_relation).equals(relation)) {
                            flagRelation = true;
                            process_crc(tamilword_checkc1, tamilword_checkc2, nec1, nec2, flagRelation, c1_qtag, to_con_tag, mwtag, node_details);
                        } else {
                            flagRelation = false;
                            process_crc(tamilword_checkc1, tamilword_checkc2, nec1, nec2, flagRelation, c1_qtag, to_con_tag, mwtag, node_details);
                        }
                    }//if
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



            //
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rank3;
    }
//

    public boolean Necheck(String poscheck) {
        if (poscheck.equals("Entity") || poscheck.equals("Non Tamil Noun")) {
            return true;
        }
        return false;
    }

    public boolean tamilcheck(String tamilword, String givenword) {
        if (tamilword.equals(givenword)) {
            return true;
        }
        return false;
    }

//
    public void process_crc(boolean ftc1, boolean ftc2, boolean fNec1, boolean fNec2, boolean flagRelation, String c1tag, String c2tag, String mwtag, nodeDetails node_details) {

        boolean pos_check = false;

        double freq_count = 0;

        Object docDetail[] = DocNodeDetails(node_details);

        docDetail[0] = 0.5 * (node_details.frequency + node_details.weight);
        docDetail[3] = node_details.ind_tamil + ":" + node_details.toconTamil;
        docDetail[9] = node_details.ind_tamil + node_details.con;
        try {
            if (ftc1 == true && ftc2 == true) {
                //////////System.ouln("CRCresults");
                if ((node_details.rel).equals("pos")) {
                    if ((c1tag.equals("QW") && c2tag.equals("QW")) || (c1tag.equals("QW") && c2tag.equals("CQW")) || (c1tag.equals("CQW") && c2tag.equals("QW")) || (c1tag.equals("CQW") && c2tag.equals("CQW"))) {
                        freq_count = 1 + (0.5 * (node_details.frequency+ node_details.weight));
                        pos_check = true;
                    }


                } else {
                    if (mwtag.contains("MW") && c1tag.equals("QW") && c2tag.equals("QW")) {
                        
                    String MW_words = node_details.ind_tamil.trim();
                    String[] MW_List = MW_words.split(" ");
                    //////////////System.ouln("Length of the multiwor"+MW_List.length);			
                    docDetail[4] = "1.0";
                    docDetail[7] = c2tag;
		    docDetail[12] = "MW";
                    freq_count = MW_List.length;
                    docDetail[0] = 1 + 0.2 * (freq_count+ node_details.frequency + node_details.weight);
                    mwCList.add(docDetail);
                    }

                }

                if (fNec1 == true || fNec2 == true) {

                    if (c1tag.equals("QW") && c2tag.equals("QW")) {
                        docDetail[4] = "1.11";
                        //////////////////////////////////////////System.ouln("bf crc add"+docDetail[2]);
                        crc_QWQW_NElist.add(docDetail);
                    } else if (c1tag.equals("EQW") && c2tag.equals("QW")) {
                        docDetail[4] = "6.11";
                        crc_EQWQW_NElist.add(docDetail);
                    } else if (c1tag.equals("QW") && c2tag.equals("EQW")) {
                        docDetail[4] = "7.11";
                        crc_QWEQW_NElist.add(docDetail);
                    } else {
                    }
                } else if (fNec1 == false && fNec2 == false) {
                    if (c1tag.equals("QW") && c2tag.equals("QW")) {
                        docDetail[4] = "1.12";
                        //////////////////////////////////////////System.ouln("bf crc add"+docDetail[2]);
                        crc_QWQW_nonNElist.add(docDetail);
                    } else if (c1tag.equals("EQW") && c2tag.equals("QW")) {
                        docDetail[4] = "6.12";
                        crc_EQWQW_nonNElist.add(docDetail);
                    } else if (c1tag.equals("QW") && c2tag.equals("EQW")) {
                        docDetail[4] = "7.12";
                        crc_QWEQW_nonNElist.add(docDetail);
                    } else {
                    }
                }

            }//if QWQW check
            else if ((ftc1 == true && ftc2 == false)) {
                if (fNec1 == true || fNec2 == true) {
                    if (c1tag.equals("QW") && c2tag.equals("CQW")) {
                        docDetail[4] = "2.11";
                        crc_QWCQW_NElist.add(docDetail);
                    } else if (c1tag.equals("EQW") && c2tag.equals("CQW")) {
                        docDetail[4] = "7.11";
                        crc_EQWCQW_NElist.add(docDetail);
                    } else {
                        //////////////////////////////////////////System.ouln("Query Tag does not mathch");
                        //////////////////////////////////////////System.ouln("c1tag is  :"+c1tag+"  :c2 tag is :"+c2tag);
                    }
                }
                if (fNec1 == false || fNec2 == false) {
                    if (c1tag.equals("QW") && c2tag.equals("CQW")) {
                        docDetail[4] = "2.12";
                        crc_QWCQW_nonNElist.add(docDetail);
                    } else if (c1tag.equals("EQW") && c2tag.equals("CQW")) {
                        docDetail[4] = "7.12";
                        crc_EQWCQW_nonNElist.add(docDetail);
                    } else {
                    }
                }

            }//else if QWCQW check
            else if ((ftc1 == false && ftc2 == true)) {
                if (fNec1 == true || fNec2 == true) {
                    if (c1tag.equals("CQW") && c2tag.equals("QW")) {
                        docDetail[4] = "3.11";
                        crc_CQWQW_NElist.add(docDetail);
                    } else if (c1tag.equals("CQW") && c2tag.equals("EQW")) {
                        docDetail[4] = "7.11";
                        crc_CQWEQW_NElist.add(docDetail);
                    } else {
                    }
                }
                if (fNec1 == false || fNec2 == false) {
                    if (c1tag.equals("CQW") && c2tag.equals("QW")) {
                        docDetail[4] = "3.12";
                        crc_CQWQW_nonNElist.add(docDetail);
                    } else if (c1tag.equals("CQW") && c2tag.equals("EQW")) {
                        freq_count = 0.5 * (node_details.frequency);

                        docDetail[0] = freq_count;
                        docDetail[4] = "7.12";
                        crc_CQWEQW_nonNElist.add(docDetail);
                    } else {
                    }
                }

            }//else if QWCQW check
            else if ((ftc1 == false && ftc2 == false)) {
                if (fNec1 == true && fNec2 == true) {
                    if (c1tag.equals("CQW") && c2tag.equals("CQW")) {
                        docDetail[4] = "4.11";
                        crc_CQWCQW_NElist.add(docDetail);
                    } else {
                    }
                }
                if (fNec1 == true || fNec2 == true) {
                    if (c1tag.equals("CQW") && c2tag.equals("CQW")) {
                        docDetail[4] = "4.12";
                        crc_CQWCQW_nonNElist.add(docDetail);
                    } else {
                        //////////////////////////////////////////System.ouln("Query Tag does not mathch");
                        //////////////////////////////////////////System.ouln("c1tag is  :"+c1tag+"  :c2 tag is :"+c2tag);
                    }
                }

            }//else if QWCQW check
        } catch (Exception e) {//////////////////////////////////System.ouln("Exception in process crc");
            e.printStackTrace();
        }
        /*
         * if(flagRelation) { crcRelList.add(docDetail);
		}
         */
    }
    public static ArrayList seperateTag(ArrayList list) {
        //Changes given to get the conceptual results
        ArrayList finalList = new ArrayList();
        ArrayList tag1 = new ArrayList();
        ArrayList tag23 = new ArrayList();
        ArrayList tag4 = new ArrayList();
        ArrayList tag5 = new ArrayList();
        ArrayList tag67 = new ArrayList();
	ArrayList tag8 = new ArrayList();

        ArrayList tour = new ArrayList();
        ArrayList wiki = new ArrayList();
        ArrayList blogs = new ArrayList();
        ArrayList others = new ArrayList();
	

        for (int i = 0; i < list.size(); i++) {
            Object obj[] = (Object[]) list.get(i);
            String tag = obj[3].toString();
            String url = obj[0].toString();
	 // if(!url.contains("news")&& !url.contains("health"))
		
	 //   {
            if (tag.startsWith("1")) {
               if (url.contains("tour")||url.contains("inbasutrula")) {
                    tour.add(obj);
                } else if (url.contains("wiki")) {
                    wiki.add(obj);
                
                }else if (url.contains("blog")) {
		blogs.add(obj);
		}
		else {
                    others.add(obj);
                }

                tag1.addAll(tour);
		tour.clear();
                tag1.addAll(wiki);
		wiki.clear();
              // tag1.addAll(blogs);
		//blogs.clear();
              //  tag1.addAll(others);
		//others.clear();
		//tag1.add(obj);

            } else if (tag.startsWith("2") || tag.startsWith("3")) {


	         if (url.contains("tour")) {
                    tour.add(obj);
                } else if (url.contains("wiki")) {
                    wiki.add(obj);
                }else if (url.contains("blog")) {
		blogs.add(obj);
		} else {
                    others.add(obj);
                }

         	   tag23.addAll(tour);
		   tour.clear();
                   tag23.addAll(wiki);
		   wiki.clear();
               		//tag23.addAll(blogs);
		//blogs.clear();
             //   tag23.addAll(others);
		//others.clear();
		//tag23.add(obj);

            } else if (tag.startsWith("4")) {


               if (url.contains("tour")) {
                    tour.add(obj);
                } else if (url.contains("wiki")) {
                    wiki.add(obj);
                }  else if (url.contains("blog")) {
		blogs.add(obj);
		}
		else {
                    others.add(obj);
                }

               tag4.addAll(tour);
		tour.clear();
               tag4.addAll(wiki);
		wiki.clear();
             // tag4.addAll(blogs);
		//blogs.clear();
            //   tag4.addAll(others);
		//others.clear();
		//tag4.add(obj);

            } else if (tag.startsWith("5")) {

              if (url.contains("tour")) {
                    tour.add(obj);
                } else if (url.contains("wiki")) {
                    wiki.add(obj);
                }  else if (url.contains("blog")) {
		blogs.add(obj);
		}else {
                    others.add(obj);
                }

                tag5.addAll(tour);
		tour.clear();
                tag5.addAll(wiki);
		wiki.clear();
              // tag5.addAll(blogs);
		//blogs.clear();
              //  tag5.addAll(others);
		//others.clear();
		//tag5.add(obj);


            } else if (tag.startsWith("6") || tag.startsWith("7")) {

              if (url.contains("tour")) {
                    tour.add(obj);
                } else if (url.contains("wiki")) {
                    wiki.add(obj);
                }else if (url.contains("blog")) {
		blogs.add(obj);
		}else {
                    others.add(obj);
                }

                tag67.addAll(tour);
		tour.clear();
                tag67.addAll(wiki);
		wiki.clear();	
              // tag67.addAll(blogs);
		//blogs.clear();
             //   tag67.addAll(others);
		//others.clear();
		//tag67.add(obj);

            } else {
            }
	
	
        }
        finalList.addAll(tag1);
	
        finalList.addAll(tag23);

        finalList.addAll(tag4);

        finalList.addAll(tag5);

        finalList.addAll(tag67);

 finalList.addAll(blogs);

finalList.addAll(others);



	System.out.println("NumberThe result is"+finalList.size());
        return finalList;
    }
//

    public ArrayList getNodeLink(ArrayList al_rank, String index_dir) {
        ArrayList get_url = new ArrayList();
        //////////////////////////////////////////System.ouln("al_rank size"+al_rank.size());
        try {
            for (int i = 0; i < al_rank.size(); i++) {
                Object[] info = (Object[]) al_rank.get(i);
             //   StringBuffer sbresult = new StringBuffer();

             //   String str2 = (String) fileList.get(info[1].toString().trim());

String str2 = rectourl._getRecNoToURL(index_dir,info[1].toString().trim());

                info[0] = str2;
             //   //System.out.println("INFO:"+info[0]+"\tDOCID:"+info[1].toString());
                if (info[0] != null) {
                    get_url.add(info);

                }

            }//for loop ends 
        } catch (Exception e) {
            e.printStackTrace();
        }
        //getNodeLinkCnt++;
        return get_url;
    }

     public ArrayList removeDuplicateWithOrder(ArrayList arlList) {
        //////////////////////////////////////////System.ouln("In remove duplicate"+arlList.size());
        TreeSet set = new TreeSet();
        ArrayList newList = new ArrayList();
        for (int i = 0; i < arlList.size(); i++) {
            //////////////////////////////////////////System.ouln("Inside for");
            Object[] element = (Object[]) arlList.get(i);
            String docid1 = (String) element[2];
            //////////////////////////////////////////System.ouln("docid in dup chk  :"+element[2]);
            if (set.add("" + element[2])) {

                newList.add(element);
                
            }
        }
        //removeDuplicateCnt++;
      //System.out.println("List of URL"+newList.size());
        return newList;
    }

    public ArrayList Freq_Sorting(ArrayList al_weight) {


        Object[] bfSort = new Object[al_weight.size()];
        double sumoffreq = 0.0;
        for (int i = 0; i < al_weight.size(); i++) {

            Object docinfo[] = (Object[]) al_weight.get(i);
            Object hit[] = new Object[9];

            hit[0] = ""; //url
            hit[1] = docinfo[2];//docid
            hit[2] = "";//snippet
            hit[3] = docinfo[4];//ranktag
            hit[4] = docinfo[3];//queryword
            hit[5] = docinfo[5];//con
            hit[6] = docinfo[7]; //query tag
            hit[7] = docinfo[0];//frequency
            hit[8] = docinfo[9];//frequency

            bfSort[i] = hit;
        }

        for (int i = 0; i < bfSort.length; i++) {
            Object[] doc1 = (Object[]) bfSort[i];
            String ranktag1 = doc1[3].toString();
            String queryTag = doc1[6].toString();
            String query_word = doc1[4].toString();
            double freq1 = (Double) doc1[7];

            for (int j = 0; j < bfSort.length; j++) {

                Object[] doc2 = (Object[]) bfSort[j];
                String ranktag2 = doc2[3].toString();
                double freq2 = (Double) doc2[7];


                if (ranktag1.startsWith("5.0")) {
                    if (queryTag.equals("QW")) {
                        if (freq1 > freq2) {

                            Object[] o = (Object[]) bfSort[i];
                            bfSort[i] = bfSort[j];
                            bfSort[j] = o;
                        }

                    }
                } else {

                    if (ranktag1.equals(ranktag2)) {
                        if (freq1 > freq2) {

                            Object[] o = (Object[]) bfSort[i];
                            bfSort[i] = bfSort[j];
                            bfSort[j] = o;
                        }

                    }

                }
            }
        }
        List list = Arrays.asList(bfSort);
        ArrayList rankresult = new ArrayList(list);

        for (int i = 0; i < bfSort.length; i++) {
            Object o[] = (Object[]) bfSort[i];
            //////////////System.ouln("The url"+o[1].toString()+o[3].toString());

        }
        //freqSortingCnt++;
        return rankresult;
    }

    /**
     * This method used for sorting the results with respect to the rank tag
     *
     * @param rank matched results with rank tag
     * @return ranked results
     */
    public ArrayList tagSorting(ArrayList rank) {

        ArrayList sortconcept = new ArrayList();

        double weight[] = new double[rank.size()];

        for (int i = 0; i < rank.size(); i++) {

            Object docinfo[] = (Object[]) rank.get(i);

            String str = (String) docinfo[4];
            //////////////////System.ouln("STR inside TAG SORTING:"+rank.get(i).toString()+"=========>"+str);

            try {
                weight[i] = Double.parseDouble(str);

            } catch (Exception e) {
            }
        }

        Object alweight[] = rank.toArray();
        Object temp[] = new Object[rank.size()];
        double temp1 = 0;
        for (int i = 0; i < weight.length; i++) {

            for (int j = 0; j < weight.length; j++) {
                if (weight[i] < weight[j]) {

                    Object[] o = (Object[]) alweight[i];
                    alweight[i] = alweight[j];
                    alweight[j] = o;
                    temp1 = weight[i];
                    weight[i] = weight[j];
                    weight[j] = temp1;

                }
            }
        }
        List list = Arrays.asList(alweight);
        ArrayList rankresult = new ArrayList(list);
        for (int i = 0; i < rankresult.size(); i++) {
            Object obj[] = (Object[]) rankresult.get(i);
            //  ////////System.ouln("The list of ranked object"+obj[4]+obj[6]);

        }
        //tagSortingCnt++;
        return rankresult;
    }

    //Adding Rank Tag for Concept Only UNL Search
    public Object[] process(boolean flagTamil, boolean flagNe, String c1c2check, String qtag, String mwtag, nodeDetails node_details, boolean flag_singleword) {

        ////////////System.ouln("flag_singleword"+flag_singleword);
        //////////////System.ouln("ftamil"+flagTamil+flagNe+qtag+c1c2check+mwtag);
        double compute_freq = 0.0;
        Object docDetail[] = DocNodeDetails(node_details);
        docDetail[9] = node_details.ind_tamil + node_details.con;



        try {
            docDetail[0] = 0.2 * (node_details.frequency + node_details.weight);
            if (mwtag.equals("MW")) {
                if (!qtag.equals("EQW")) {


                    String MW_words = node_details.ind_tamil.trim();
                    String[] MW_List = MW_words.split(" ");
                    //////////////System.ouln("Length of the multiwor"+MW_List.length);			
                    docDetail[4] = "1.0";
                    docDetail[7] = qtag;
		    docDetail[12] = "MW";
                    compute_freq = MW_List.length;
                    docDetail[0] = 1 + 0.2 * (compute_freq + node_details.frequency + node_details.weight);
                    mwCList.add(docDetail);
                }

            } else {

                if (flagTamil == true) {

                    if (flagNe == true) {






                        if (c1c2check.equals("c1")) {
                            if (qtag.equals("QW") || qtag.equals("CONC")) {

                                if (flag_singleword) {
                                    docDetail[4] = "5.01";
                                } else {

                                    docDetail[4] = "5.61";
                                }
                                docDetail[7] = "QW";
                                docDetail[8] = "NE";
                                c1_QW_NElist.add(docDetail);
                            } else if (qtag.equals("EQW")) {
                                docDetail[4] = "7.71";
                                docDetail[7] = "EQW";
                                docDetail[8] = "NE";
                                c1_EQW_NElist.add(docDetail);
                            } else {
                            }

                        } else if (c1c2check.equals("c2")) {
                            if (qtag.equals("QW") || qtag.equals("CONC")) {
                                if (flag_singleword == true) {
                                    docDetail[4] = "5.01";
                                } else {
                                    docDetail[4] = "5.62";
                                }
                                docDetail[7] = "QW";
                                docDetail[8] = "NE";
                                c2_QW_NElist.add(docDetail);
                            } else if (qtag.equals("EQW")) {
                                docDetail[4] = "7.71";
                                docDetail[7] = "EQW";
                                docDetail[8] = "NE";
                                c2_EQW_NElist.add(docDetail);
                            } else {
                            }

                        } else {
                        }

                    } else if (flagNe == false) {



                        if (c1c2check.equals("c1")) {
                            if (qtag.equals("QW") || qtag.equals("CONC")) {
                                if (flag_singleword) {
                                    docDetail[4] = "5.02";
                                } else {
                                    docDetail[4] = "5.71";
                                }
                                docDetail[7] = "QW";
                                docDetail[8] = "nonNE";
                                c1_QW_nonNElist.add(docDetail);
                            } else if (qtag.equals("EQW")) {
                                docDetail[4] = "7.72";
                                docDetail[7] = "EQW";
                                docDetail[8] = "nonNE";
                                c1_EQW_nonNElist.add(docDetail);
                            } else {
                                //////////////////////////////////////////System.ouln(" in c1 process_cr tag violate");
                            }
                        } else if (c1c2check.equals("c2")) {
                            if (qtag.equals("QW") || qtag.equals("CONC")) {
                                if (flag_singleword) {
                                    docDetail[4] = "5.02";
                                } else {
                                    docDetail[4] = "5.72";
                                }
                                docDetail[7] = "QW";
                                docDetail[8] = "nonNE";
                                c2_QW_nonNElist.add(docDetail);
                            } else if (qtag.equals("EQW")) {
                                docDetail[4] = "7.72";
                                docDetail[7] = "EQW";
                                docDetail[8] = "nonNE";
                                c2_EQW_nonNElist.add(docDetail);
                            } else {
                                //////////////////////////////////////////System.ouln(" in c1 process_cr tag violate");
                            }
                        } else {
                            //////////////////////////////////////////System.ouln("process_cr c1c2 check fail");
                        }

                    }//ne check

                }//tamil check
                else if (flagTamil == false) {




                    if (c1c2check.equals("c1")) {
                        if (qtag.equals("QW") || qtag.equals("CONC")) {
                            docDetail[4] = "7.61";
                            docDetail[7] = "CQW";
                            docDetail[8] = "NE";
                            c1_CQW_NElist.add(docDetail);
                        } else if (qtag.equals("EQW")) {
                            docDetail[4] = "7.71";
                            docDetail[7] = "EQW";
                            docDetail[8] = "NE";
                            c1_EQW_NElist.add(docDetail);
                        } else {
                            //////////////////////////////////////////System.ouln(" in c1 process_cr tag violate");
                        }

                    } else if (c1c2check.equals("c2")) {
                        if (qtag.equals("QW") || qtag.equals("CONC")) {
                            docDetail[4] = "7.61";
                            docDetail[7] = "CQW";
                            docDetail[8] = "NE";
                            c2_CQW_NElist.add(docDetail);
                        } else if (qtag.equals("EQW")) {
                            docDetail[4] = "7.71";
                            docDetail[7] = "EQW";
                            docDetail[8] = "NE";
                            c2_EQW_NElist.add(docDetail);
                        } else {
                            //////////////////////////////////////////System.ouln(" in c2 process_cr tag violate");
                        }

                    } else {
                        //////////////////////////////////////////System.ouln("process_cr c1c2 check fail");
                    }


                }
                if (flagNe == false) {
                    //docDetail[0] = 0.2 * (node_details.frequency+node_details.weight);


                    if (c1c2check.equals("c1")) {
                        if (qtag.equals("QW") || qtag.equals("CONC")) {
                            docDetail[4] = "7.62";
                            docDetail[7] = "CQW";
                            docDetail[8] = "nonNE";
                            c1_CQW_nonNElist.add(docDetail);
                        } else if (qtag.equals("EQW")) {
                            docDetail[4] = "7.72";
                            docDetail[7] = "EQW";
                            docDetail[8] = "nonNE";
                            c1_EQW_nonNElist.add(docDetail);
                        } else {
                            //////////////////////////////////////////System.ouln(" in c1 process_cr tag violate");
                        }
                    } else if (c1c2check.equals("c2")) {
                        if (qtag.equals("QW") || qtag.equals("CONC")) {
                            docDetail[4] = "7.62";
                            docDetail[7] = "CQW";
                            docDetail[8] = "nonNE";
                            c2_CQW_nonNElist.add(docDetail);
                        } else if (qtag.equals("EQW")) {
                            docDetail[4] = "7.72";
                            docDetail[7] = "EQW";
                            docDetail[8] = "nonNE";
                            c2_EQW_nonNElist.add(docDetail);
                        } else {
                            //////////////////////////////////////////System.ouln(" in c1 process_cr tag violate");
                        }
                    } else {
                        //////////////////////////////////////////System.ouln("process_cr c1c2 check fail");
                    }//ne check
                }
            }

        } catch (Exception e) {////////////////////////////////////System.ouln("Exception in process crc");
            e.printStackTrace();
        }
        return docDetail;
    }
//

    public void tagPrefix(String c1tag, String c2tag, Object[] c1doc, String c1_tamil, String suffix, String highlight) {
        String tag = "";
        Object[] docDetail = new Object[9];
        docDetail = c1doc;
        //////////////////////////////System.ouln("c1tag"+c1_tamil+";"+c1tag+"\t"+"c2tag"+c2tag+suffix);
        ////////////////////////////System.ouln("andfreq in tagprefix is="+c1doc[4]);
        if (c1tag.equals("QW") && c2tag.equals("QW") || c1tag.equals("CONC") && c2tag.equals("CONC")) {
            tag = "1." + suffix;
            docDetail[3] = c1_tamil;
            docDetail[4] = tag;
            docDetail[9] = highlight;
            and_QWQW_list.add(docDetail);
        } else if (c1tag.equals("QW") && c2tag.equals("CQW")) {
            tag = "2." + suffix;

            docDetail[3] = c1_tamil;
            docDetail[4] = tag;
            docDetail[9] = highlight;
            and_QWCQW_list.add(docDetail);
        } else if (c1tag.equals("CQW") && c2tag.equals("QW")) {
            tag = "3." + suffix;
            docDetail[3] = c1_tamil;
            docDetail[4] = tag;
            docDetail[9] = highlight;
            and_CQWQW_list.add(docDetail);
        } else if (c1tag.equals("CQW") && c2tag.equals("CQW")) {
            tag = "4." + suffix;
            docDetail[3] = c1_tamil;
            docDetail[4] = tag;
            docDetail[9] = highlight;
            and_CQWCQW_list.add(docDetail);

        }/*
         * else if (c1tag.equals("QW") && c2tag.equals("EQW")) {
         *
         * tag = "6." + suffix; docDetail[3] = c1_tamil; docDetail[4] = tag;
         * docDetail[9] = highlight; and_QWEQW_list.add(docDetail); } else if
         * (c1tag.equals("EQW") && c2tag.equals("QW")) {
         *
         * tag = "6." + suffix; docDetail[3] = c1_tamil; docDetail[4] = tag;
         * docDetail[9] = highlight; and_EQWQW_list.add(docDetail);
         *
         * } else if (c1tag.equals("CQW") && c2tag.equals("EQW")) { tag = "7." +
         * suffix; docDetail[3] = c1_tamil; docDetail[4] = tag; docDetail[9] =
         * highlight; and_CQWEQW_list.add(docDetail); } else if
         * (c1tag.equals("EQW") && c2tag.equals("CQW")) { tag = "7." + suffix;
         * docDetail[3] = c1_tamil; docDetail[4] = tag; docDetail[9] =
         * highlight; and_EQWCQW_list.add(docDetail); } else if
         * (c1tag.equals("EQW") && c2tag.equals("EQW")) { tag = "7." + suffix;
         * docDetail[3] = c1_tamil; docDetail[4] = tag; docDetail[9] =
         * highlight; and_EQWCQW_list.add(docDetail); } else {
        }
         */
        TreeSet sett = (TreeSet) table.get(docDetail[2]);
        if (sett != null) {
            if (sett.size() > 2) {
                String tamilword = "";
                highlight = "";
                Iterator i = sett.iterator();
                while (i.hasNext()) {
                    String concpt = (String) i.next();
                    String word = (String) hashtable_c.get(concpt);
                    if (word != null) {
                        tamilword += word + " : ";
                        highlight += word + concpt + ":";
                    }
                }

                tag = "1";
                c1doc[0] = (Double) andFreq.get(docDetail[2]);
                c1doc[3] = tamilword;
                //////////////////////////////System.ouln("Inside >2"+c1doc[3].toString());
                c1doc[4] = tag;
                docDetail[9] = highlight;
                and_QWQW_list.add(c1doc);
            }
        }

        c1_tamil = null;
    }
    TreeSet ts_new = new TreeSet();

    public void andlogicforconly(ArrayList c) {
        //////////////System.ouln("Inside andlogicforconly"+c.size());
        double andfreq = 0;
Hashtable andcon_count=new Hashtable();
TreeSet ts1=new TreeSet();
        for (int i = 0; i < c.size(); i++) {
            Object[] cdoc = (Object[]) c.get(i);
            String docidc1 = (String) cdoc[2];
            double andval = 0;
            for (int j = 0; j < c.size(); j++) {
                Object[] cdoc2 = (Object[]) c.get(j);
                String docidc2 = (String) cdoc2[2];
                String and_terms = "";
                //////////////////////////System.ouln("Docid"+cdoc[5]+docidc1+"*"+cdoc2[5]+docidc2);
                if (docidc1.equals(docidc2)){
                    if (cdoc[8].toString().equals("NE") || cdoc2[8].toString().equals("NE")) {
                        if (!cdoc[5].equals(cdoc2[5])) {
                    




                            if (!cdoc[6].toString().isEmpty() && !cdoc2[6].toString().isEmpty()) {
                              //  if (((andval = Integer.parseInt(cdoc[6].toString()) & Integer.parseInt(cdoc2[6].toString()))) != 0) 
				if(cdoc[6].toString().trim().equals(cdoc2[6].toString().trim()))
					{

                                    if (!(cdoc[3].toString().contains(":") || cdoc2[3].toString().contains(":"))) {
                                        double docfreq1 = (Double) cdoc[0];
                                        double docfreq2 = (Double) cdoc2[0];
                                        andfreq = andfreq + (docfreq1 + docfreq2) / 2;
                                        cdoc[0] = andfreq;
                                        String c1tag = cdoc[7].toString();
                                        String c2tag = cdoc2[7].toString();
                                        String addsuffix_ne = "";
                                        String addsuffix_mw = "";


                                        String suffix = "";
                                        if (cdoc[8].toString().equals("NE") && cdoc2[8].toString().equals("NE")) {
                                            addsuffix_ne = "1";
                                        } else if (cdoc[8].toString().equals("NE") || cdoc2[8].toString().equals("NE")) {
                                            addsuffix_ne = "2";
                                        }
					if(cdoc[12].toString().equals("MW") || cdoc2[12].toString().equals("MW"))
					{
					 suffix = "0" + addsuffix_ne;
					}
					else
					{
                                        suffix = "1" + addsuffix_ne;
					}
                                        tagPrefix(cdoc[7].toString(), cdoc2[7].toString(), cdoc, cdoc[3].toString() + ":" + cdoc2[3].toString(), suffix, cdoc[3].toString() + cdoc[5].toString() + ":" + cdoc2[3].toString() + cdoc2[5].toString());
				if(ts1.add(cdoc[5].toString()+docidc1)){
				andcon_count.put(i,cdoc[5].toString());
				}
                                    }


                                }//if
                            }//if
                        }//if
                    }//if

			
                }//if
            }//for
        }//for

	/*Enumeration andcon_enm=andcon_count.keys();
while(andcon_enm.hasMoreElements()) {
String docid_str = (String)andcon_enm.nextElement();
//////System.ouln("The concepts in AND list are"+docid_str + ": " +andcon_count.get(docid_str));
} */
////////System.out.println("ANDCount"+andcon_count.size());
		
    }


    public void andlogic(ArrayList c2andlist, ArrayList c1andlist, String check) {

        double andfreq = 0;
        double docfreq1 = 0;
        double docfreq2 = 0;

        try {
            if (c2andlist.size() != 0 && c1andlist.size() != 0) {
                ArrayList doclist = new ArrayList();
                for (int i = 0; i < c2andlist.size(); i++) {
                    Object[] c1doc = (Object[]) c2andlist.get(i);
                   // int docid1 = (Integer) c1doc[2];
 String docid1 = (String) c1doc[2];

                    for (int j = 0; j < c1andlist.size(); j++) {
                        Object[] c2doc = (Object[]) c1andlist.get(j);
                        String docid2 = (String) c2doc[2];
                        String c1tag = c1doc[7].toString();
                        String c2tag = c2doc[7].toString();

                        if (docid1.equals(docid2) & i != j) {
                            if (c1doc[8].toString().equals("NE") || c2doc[8].toString().equals("NE")) {

                                int andval = 0;
                                ////////////////System.ouln("Query terms for AND" + c1doc[5].toString() + c2doc[5].toString() + ":" + c1doc[3].toString() + c2doc[3].toString());


                                if (!c1doc[5].equals(c2doc[5])) {

                                    // if (!c1doc[6].toString().isEmpty() && !c2doc[6].toString().isEmpty()) {
                                  //  if (((andval = Integer.parseInt(c1doc[6].toString()) & Integer.parseInt(c2doc[6].toString()))) != 0) 
if(c1doc[6].toString().trim().equals(c2doc[6].toString().trim()))
					{




                                        if (!(c1doc[3].toString().contains(":") || c2doc[3].toString().contains(":"))) {

                                            docfreq1 = (Double) c1doc[0];
                                            docfreq2 = (Double) c2doc[0];
                                            andfreq = (docfreq1 + docfreq2) / 2;
                                            c1doc[0] = andfreq;

                                            String addsuffix_ne = "";
                                            String addsuffix_mw = "";


                                            String suffix = "";
                                            if (c1doc[8].toString().equals("NE") && c2doc[8].toString().equals("NE")) {
                                                addsuffix_ne = "1";
                                            } else if (c1doc[8].toString().equals("NE") || c2doc[8].toString().equals("NE")) {
                                                addsuffix_ne = "2";
                                            } else {
                                                addsuffix_ne = "3";
                                            }


                                            if (check.equals("xy")) {

                                                suffix = "5" + addsuffix_ne;

                                                tagPrefix(c1doc[7].toString(), c2doc[7].toString(), c1doc, c1doc[3].toString() + ":" + c2doc[3].toString(), suffix, c1doc[3].toString() + c1doc[5].toString() + ":" + c2doc[3].toString() + c2doc[5].toString());


                                            } else if (check.equals("mw")) {
                                                if (!c1doc[5].toString().trim().contains(c2doc[5].toString().trim())) {
                                                    suffix = "0"+ addsuffix_ne;

                                                    tagPrefix(c1doc[7].toString(), c2doc[7].toString(), c1doc, c1doc[3].toString() + ":" + c2doc[3].toString(), suffix, c1doc[3].toString() + c1doc[5].toString() + ":" + c2doc[3].toString() + c2doc[5].toString());
                                                }
                                            }

                                            if ((c1tag.equals("QW") && c2tag.equals("QW"))) {
                                                if (table.containsKey(docid1)) {

                                                    set = (TreeSet) table.get(docid1);
                                                    if (set.add(c1doc[5])) {
                                                        if (andFreq.containsKey(docid1)) {
                                                            double add = (Double) andFreq.get(docid1);
                                                            add += docfreq1;
                                                            andFreq.put(docid1, add);
                                                        } else {
                                                            andFreq.put(docid1, c1doc[0]);
                                                        }
                                                    }
                                                    if (set.add(c2doc[5])) {
                                                        if (andFreq.containsKey(docid1)) {
                                                            double add = (Double) andFreq.get(docid1);
                                                            add += docfreq2;
                                                            andFreq.put(docid1, add);
                                                        } else {
                                                            andFreq.put(docid1, c2doc[0]);
                                                        }
                                                    }

                                                    table.put(docid1, set);
                                                } else {
                                                    set = new TreeSet();

                                                    set.add(c1doc[5]);
                                                    set.add(c2doc[5]);
                                                    andFreq.put(docid1, andfreq);
                                                    table.put(docid1, set);
                                                }

                                                ////////System.ouln("Size of the AND results"+set.size());
                                            }

                                        }
                                    }
                                    //}//sentence id
                                } //tamil word not equal
                            }// duplicate chk
                        }
                    } //doc contains
                }//for
            }//if null chk
        } catch (Exception e) {////////////////////////////////////System.ouln("Exception in ANDLogic");
            e.printStackTrace();
        }
        // andlogicCnt++;
    }

    public Object[] DocNodeDetails(nodeDetails node_details) {
        TreeSet set = new TreeSet();
        Object[] docDetail_bnode = new Object[13];
        docDetail_bnode[0] = node_details.frequency; //frequency
        docDetail_bnode[1] = "";	//
        docDetail_bnode[2] = node_details.docid; //docid
        docDetail_bnode[3] = node_details.ind_tamil; //tamilwordTreeSet ts_new=new TreeSet();
        docDetail_bnode[4] = ""; //ranktag
        docDetail_bnode[5] = node_details.con; //concept
        docDetail_bnode[6] = node_details.senid; //sentid
        docDetail_bnode[7] = ""; //querytag
        docDetail_bnode[8] = ""; //NE chk
        docDetail_bnode[9] = ""; //for snippet highlight

        docDetail_bnode[10] = node_details.tocon;
        docDetail_bnode[11] = node_details.toconTamil;
         docDetail_bnode[12]=node_details.mw;

        return docDetail_bnode;
    }
}
