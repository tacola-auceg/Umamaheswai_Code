package org.apache.nutch.search.unl;


import java.text.*;
import java.io.*;
import java.util.ArrayList;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;


public class searcMod {
	private static void searchw(String queryString) throws ParseException, IOException {
 
        try {
		//FileWriter fw=new FileWriter("/root/furl_list_doc1.txt");
		FileWriter fw=new FileWriter("/root/furl_apr9_doc1.txt");
          //  IndexSearcher is = new IndexSearcher(FSDirectory.getDirectory("/CliaIITKGP/crawl-june-2012/index"));
  IndexSearcher is = new IndexSearcher(FSDirectory.getDirectory("/opt/index_FINAL1"));
            String ff = "lang";// br.readLine();
            String q = queryString.trim();//br.readLine();
            //q = QueryParser.escape(q).replace(" ", "?");
            //q = "#*" + q + "*";
            QueryParser qp = new QueryParser(ff.trim(), new KeywordAnalyzer());
            Query qq = qp.parse(q);
            TopDocs td;
            td = is.search(qq, new QueryFilter(qq), 5999464);
            System.err.println("Results Found:" + td.scoreDocs.length);
		
            for (int i = 0; i < td.scoreDocs.length; i++) {
                Document d = is.doc(td.scoreDocs[i].doc);
                
		System.out.println(d.get("url"));
		fw.append(d.get("url")+";"+"\n");
            }
            is.close();
		fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
	public static void main(String args[]) throws Exception, IOException{
		searcMod scrc=new searcMod();
		 String ta="ta";
		 scrc.searchw(ta);
		
	}
}

