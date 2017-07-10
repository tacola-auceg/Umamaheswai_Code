package org.apache.nutch.search.unl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.lucene.analysis.KeywordAnalyzer;
//import javax.swing.text.Document;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

public class SimpleFileIndexer {

    public static void main(String[] args) throws Exception {
        //"/root/Desktop/jjj/"
        //_UWDictIndex("/root/Desktop/Lucene-UWDict-Index/");
        //_indexC("/root/Desktop/Lucene-C-Index-All/");
        //_indexCRC("/root/Desktop/Lucene-CRC-Index-All/");
        ////System.out.println(("" + _searchCRC("/root/Desktop/Lucene-CRC-Index-All/", "implicit(aoj>thing)", "client(icl>person)")).replace("stored/uncompressed,indexed", " ").replace("Document", "\n"));
        // System.out.println(("" + new SimpleFileIndexer()._searchC("/root/Desktop/Lucene-C-Index/", "சூரியன்", "tamilword", 5)).replace("stored/uncompressed,indexed", " ").replace("Document", "\n"));
        ///opt/Jurassic-Park/a/1/a1/Snippet
        //System.out.println(("" + new SimpleFileIndexer()._getSnippet("a1", new String[]{"சென்னை", "வெட்ட"})));//.replace("stored/uncompressed,indexed", " ").replace("Document", "\n"));
        //System.out.println(("" + new SimpleFileIndexer()._getSummary("a1")));
        //System.out.println(("" + new SimpleFileIndexer()._resultSegment("a1", "http://www.superstar.com/a.html", new String[]{"சென்னை", "வெட்ட"})));
        //System.out.println(("" + new SimpleFileIndexer()._resultSegment("a1", new String[]{"சென்னை", "வெட்ட"})));
     
    }

    public ArrayList<Document> _searchCRC(String indexDir, String fromuw, String touw) {
        ArrayList<Document> arr = new ArrayList<Document>();
        try {
            IndexSearcher is = new IndexSearcher(FSDirectory.getDirectory(indexDir));
            String ff = "crc";// br.readLine();
            String q = fromuw + touw;//br.readLine();
            q = ff + ":" + QueryParser.escape(q).replace(" ", "?");
            long l1 = System.currentTimeMillis();
            QueryParser qp = new QueryParser(ff.trim(), new KeywordAnalyzer());
            Query qq = qp.parse(q);
            //
            // String[] sF = {"conceptfrequency", "termfrequency"};
            // Sort sss = new Sort(sF);
            //
            Hits td = is.search(qq);
            System.out.println("Found: " + td.length() + " Entries.");
            for (int i = td.length() - 1; i >= 0; i++) {
                Document d = td.doc(i);
                //
                //Document d = is.doc(td.scoreDocs[i].doc);
                if (d.get("fromtamilconcept").equals(d.get("totamilconcept")) == false && (d.get("frompos").contains("Entity") || d.get("frompos").equals("Noun")) && (d.get("topos").contains("Entity") || d.get("topos").equals("Noun"))) {
                    d.removeField("crc");
                    arr.add(d);

                }
                if (arr.size() > 1000) {
                    td = null;
                    break;
                }
            }
            long l2 = System.currentTimeMillis();
            ////////////System.out.println("Searching Time:" + (l2 - l1));
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }
//

    public ArrayList<Document> _searchC(String indexDir, String tid, String fname, int norm_factor, Hashtable h) {
return _searchC(indexDir, tid, fname, norm_factor);
}
    public ArrayList<Document> _searchC(String indexDir, String tid, String fname, int norm_factor) {
        ArrayList<Document> arr = new ArrayList<Document>();
        try {
            IndexSearcher is = new IndexSearcher(FSDirectory.getDirectory(indexDir));
            String ff = fname.trim();// br.readLine();
            String q = tid.trim();//br.readLine();
            q = QueryParser.escape(q).replace(" ", "?");
            long l1 = System.currentTimeMillis();
            QueryParser qp = new QueryParser(ff.trim(), new KeywordAnalyzer());
            Query qq = qp.parse(q);
            //
            String[] sF = {"conceptfrequency"};
            Sort sss = new Sort(sF);
            sss.setSort("conceptfrequency", true);
            TopFieldDocs td;
            td = is.search(qq, new QueryFilter(qq), 1000, sss);
            System.out.println("C Results Found:" + td.scoreDocs.length);
            //      System.out.println("Yes" + td.scoreDocs.length);
            for (int i = 0; i < td.scoreDocs.length; i++) {
                //
                Document d = is.doc(td.scoreDocs[i].doc);
//
                String sentid = d.get("documentid"), con_freq = d.get("conceptfrequency");
                int sen_weight = 0, weight_c = 0;
                if (sentid.equals("s1")) {
                    sen_weight = 100;
                    int count_c = Integer.parseInt(con_freq.toString());
                    weight_c = (count_c * norm_factor) + sen_weight;
                } else if (sentid.equals("s2")) {
                    sen_weight = 75;
                    int count_c = Integer.parseInt(con_freq.toString());
                    weight_c = (count_c * norm_factor) + sen_weight;
                } else if (sentid.equals("s3") || (sentid.equals("s4"))
                        || (sentid.equals("s5"))) {
                    sen_weight = 50;
                    int count_c = Integer.parseInt(con_freq.toString());
                    weight_c = (count_c * norm_factor) + sen_weight;
                } else {
                    sen_weight = 0;
                    int count_c = Integer.parseInt(con_freq.toString());
                    weight_c = (count_c) + sen_weight;
                }
//
                d.removeField("weight");
                d.add(new Field("weight", "" + weight_c, Field.Store.YES, Field.Index.UN_TOKENIZED));
                //
                arr.add(d);
            }

            long l2 = System.currentTimeMillis();
            System.out.println("Results Found:" + td.scoreDocs.length);
            ////////////System.out.println("Searching Time:" + (l2 - l1) + "ms");
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }
//

}
// 

