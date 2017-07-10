package org.apache.nutch.search.unl;

import java.io.*;
import java.util.*;
import javax.sql.rowset.spi.TransactionalWriter;
import org.apache.nutch.unl.*;
//import org.apache.nutch.analysis.unl.ta.CRC;
import org.apache.nutch.search.unl.CRC;
import org.apache.nutch.unl.UWInfo;
import org.hibernate.*;

public class QueryExpansion_DB {
	
	
	public String getUWConceptfrmDB(Transaction transaction, Session session, int synonymid) {		
		List<UWInfo> tamilconcept_list = null;
		String uwconcept ="";
		try {
			tamilconcept_list = (List<UWInfo>) session.createQuery("from UWInfo where synonymid=" + synonymid).list();
			for (Iterator<UWInfo> iterator = tamilconcept_list.iterator(); iterator.hasNext();) {
				UWInfo uwinfo = (UWInfo) iterator.next();			
				uwconcept = uwinfo.getUwconcept();
			}
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
		}
		return uwconcept;
	}
	
	/*public ArrayList<CRC> getQueryexpansion(String tamilconcept){
		Session session = HibernateUtil.getSessionFactory().openSession();		
		Transaction transaction = null;
		ArrayList<CRC> expansion = new ArrayList<CRC>();
		try {
			transaction = session.beginTransaction();
			//to get the node id
			int i = 0;
			String uwconcept = "";			
			List<UWInfo> nodeinfo = (List<UWInfo>) session.createQuery("from UWInfo where tamilconcept='"+tamilconcept+"'").list();			
			for (Iterator<UWInfo> iterator = nodeinfo.iterator(); iterator.hasNext();) {
				UWInfo uwinfo = (UWInfo) iterator.next();				
				uwconcept = uwinfo.getUwconcept();
				//System.out.println("uwconcept:"+uwconcept);
			}								
			
			List<UWInfo> uwinfo = (List<UWInfo>) session.createQuery("from UWInfo where uwconcept='"+uwconcept+"'").list();			
			for (Iterator<UWInfo> iterator = uwinfo.iterator(); iterator.hasNext();) {
				UWInfo info = (UWInfo) iterator.next();			
				//System.out.println(">>?"+info.getSynonymid());
				
				String fromUWConcept = getUWConceptfrmDB(transaction, session, info.getSynonymid());				
				List<CRCNode> getCRCNodeinfo = (List<CRCNode>) session.createQuery("from CRCNode where tosynonymid="+ info.getSynonymid()).list();				
				for (Iterator<CRCNode> iterator1 = getCRCNodeinfo.iterator(); iterator1.hasNext();) {
					CRCNode crcnode = (CRCNode) iterator1.next();
					int crcnodeid = crcnode.getCrcid();
					////System.out.println("CRC id:"+crcnodeid);
					List<CRCInfo> getCRCdocinfo = (List<CRCInfo>) session.createQuery("from CRCInfo where crcid='" +crcnodeid  + "'").list();
					int count = getCRCdocinfo.size();										
					
					String totamilconcept = getTamilconceptfrmDB(transaction,session, crcnode.getFromsynonymid());					
					String touwconcept =    getUWConceptfrmDB   (transaction,session, crcnode.getFromsynonymid());
					
					//System.out.println("frm:"+tamilconcept+"\t"+fromUWConcept+"\t"+crcnode.getFrompos()+"\t"+crcnode.getRelation()+"\t"+totamilconcept +"\t" +touwconcept+"\t"+crcnode.getTopos()+"\t"+count);
					CRC crc =  new CRC();
					i++;
					crc.setTam1(totamilconcept);
					crc.setC1(touwconcept);
					crc.setPos1(crcnode.getFrompos());
					crc.setQWTag1("EQW"+i);
					crc.setMWTag1("");
					crc.setRel(crcnode.getRelation());
					crc.setTam2(tamilconcept);
					String MWTag=CheckMultiword(tamilconcept);//Multiword check
					crc.setC2(fromUWConcept);
					crc.setPos2(crcnode.getTopos());
					crc.setQWTag2("QW");
					crc.setMWTag2(MWTag);
					expansion.add(crc);
				}						
			}
									
			//String tamilconcept = "";crc
			/*int synonymid = 0; 
			String fromUWConcept = "";
			int i = 0;
			if(getNodeid.size() > 0 ){
			List<Synonym> getSynonyminfo = (List<Synonym>) session.createQuery("from Synonym where nodeid='" + nodeid + "'").list();			
			for (Iterator<Synonym> iterator = getSynonyminfo.iterator(); iterator.hasNext();) {
				Synonym synonym = (Synonym) iterator.next();								
				synonymid = synonym.getSynonymid();
				tamilconcept =  synonym.getTamilconcept();
				fromUWConcept = getUWConceptfrmDB(transaction,session,nodeid);				
				List<CRCNode> getCRCNodeinfo = (List<CRCNode>) session.createQuery("from CRCNode where tosynonymid='" + synonymid + "'").list();
				for (Iterator<CRCNode> iterator1 = getCRCNodeinfo.iterator(); iterator1.hasNext();) {
					CRCNode crcnode = (CRCNode) iterator1.next();
					int crcnodeid = crcnode.getCrcid();
					////System.out.println("CRC id:"+crcnodeid);
					List<CRCInfo> getCRCdocinfo = (List<CRCInfo>) session.createQuery("from CRCInfo where crcid='" +crcnodeid  + "'").list();
					int count = getCRCdocinfo.size();										
					String totamilconcept = getTamilconceptfrmDB(transaction,session, crcnode.getFromsynonymid());
					String touwconcept = getUWConceptfrmDB(transaction,session,totamilconcept);
					//System.out.println("frm:"+tamilconcept+"\t"+fromUWConcept+"\t"+crcnode.getFrompos()+"\t"+crcnode.getRelation()+"\t"+totamilconcept +"\t" +touwconcept+"\t"+crcnode.getTopos()+"\t"+count);
					CRC crc =  new CRC();
					i++;
					crc.setTam1(totamilconcept);
					crc.setC1(touwconcept);
					crc.setPos1(crcnode.getFrompos());
					crc.setQWTag1("EQW"+i);
					crc.setMWTag1("");
					crc.setRel(crcnode.getRelation());
					crc.setTam2(tamilconcept);
					String MWTag=CheckMultiword(tamilconcept);//Multiword check
					crc.setC2(fromUWConcept);
					crc.setPos2(crcnode.getTopos());
					crc.setQWTag2("QW");
					crc.setMWTag2(MWTag);
					expansion.add(crc);
				}
			}						
			}*/
			/*transaction.commit();
			//Collections.sort(expansion);
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return expansion;
	}*/
			

	/*public ArrayList<CRC> getQueryexpansion(String tamilconcept){
		Session session = HibernateUtil.getSessionFactory().openSession();		
		Transaction transaction = null;
		ArrayList<CRC> expansion = new ArrayList<CRC>();
		try {
			transaction = session.beginTransaction();
			//to get the node id
			int frmnodeid = 0;
			int nodeid = 0;
			String frmuwconcept ="";
			List<Synonym> getNodeid = (List<Synonym>) session.createQuery("from Synonym where tamilconcept='" + tamilconcept + "'").list();			
			for (Iterator<Synonym> iterator = getNodeid.iterator(); iterator.hasNext();) {
				Synonym synonym = (Synonym) iterator.next();
				nodeid = synonym.getNodeid();
				//frmuwconcept = synonym.getUwconcept();
				////System.out.println("Node id:"+nodeid );
			}
									
			//String tamilconcept = "";crc
			int synonymid = 0; 
			String fromUWConcept = "";
			int i = 0;
			if(getNodeid.size() > 0 ){
			List<Synonym> getSynonyminfo = (List<Synonym>) session.createQuery("from Synonym where nodeid='" + nodeid + "'").list();			
			for (Iterator<Synonym> iterator = getSynonyminfo.iterator(); iterator.hasNext();) {
				Synonym synonym = (Synonym) iterator.next();								
				synonymid = synonym.getSynonymid();
				tamilconcept =  synonym.getTamilconcept();
				fromUWConcept = getUWConceptfrmDB(transaction,session,nodeid);				
				List<CRCNode> getCRCNodeinfo = (List<CRCNode>) session.createQuery("from CRCNode where tosynonymid='" + synonymid + "'").list();
				for (Iterator<CRCNode> iterator1 = getCRCNodeinfo.iterator(); iterator1.hasNext();) {
					CRCNode crcnode = (CRCNode) iterator1.next();
					int crcnodeid = crcnode.getCrcid();
					////System.out.println("CRC id:"+crcnodeid);
					List<CRCInfo> getCRCdocinfo = (List<CRCInfo>) session.createQuery("from CRCInfo where crcid='" +crcnodeid  + "'").list();
					int count = getCRCdocinfo.size();										
					String totamilconcept = getTamilconceptfrmDB(transaction,session, crcnode.getFromsynonymid());
					String touwconcept = getUWConceptfrmDB(transaction,session,totamilconcept);
					//System.out.println("frm:"+tamilconcept+"\t"+fromUWConcept+"\t"+crcnode.getFrompos()+"\t"+crcnode.getRelation()+"\t"+totamilconcept +"\t" +touwconcept+"\t"+crcnode.getTopos()+"\t"+count);
					CRC crc =  new CRC();
					i++;
					crc.setTam1(totamilconcept);
					crc.setC1(touwconcept);
					crc.setPos1(crcnode.getFrompos());
					crc.setQWTag1("EQW"+i);
					crc.setMWTag1("");
					crc.setRel(crcnode.getRelation());
					crc.setTam2(tamilconcept);
					String MWTag=CheckMultiword(tamilconcept);//Multiword check
					crc.setC2(fromUWConcept);
					crc.setPos2(crcnode.getTopos());
					crc.setQWTag2("QW");
					crc.setMWTag2(MWTag);
					expansion.add(crc);
				}
			}						
			}
			transaction.commit();
			//Collections.sort(expansion);
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return expansion;
	}*/
	
	public ArrayList<CRC> getQueryexpansion(String tamilconcept, Session session, Transaction transaction){
	//Session session = HibernateUtil.getSessionFactory().openSession();		
	//Transaction transaction = null;
	ArrayList<CRC> expansion = new ArrayList<CRC>();
	try {
		//transaction = session.beginTransaction();
		//to get the node id
		int i = 0;
		String uwconcept = "";			
		List<UWInfo> nodeinfo = (List<UWInfo>) session.createQuery("from UWInfo where tamilconcept='"+tamilconcept+"'").list();			
		for (Iterator<UWInfo> iterator = nodeinfo.iterator(); iterator.hasNext();) {
			UWInfo uwinfo = (UWInfo) iterator.next();				
			uwconcept = uwinfo.getUwconcept();
			////System.out.println("uwconcept:"+uwconcept);
		}								
								
			List<CRCNodeExt> getCRCNodeinfo = (List<CRCNodeExt>) session.createQuery("from CRCNodeExt where touwconcept = '"+uwconcept+"'").list();				
			for (Iterator<CRCNodeExt> iterator1 = getCRCNodeinfo.iterator(); iterator1.hasNext();) {
				CRCNodeExt crcnodeext = (CRCNodeExt) iterator1.next();
				int crcnodeid = crcnodeext.getCrcid();
				////System.out.println("CRC id:"+crcnodeid);
				List<CRCInfo> getCRCdocinfo = (List<CRCInfo>) session.createQuery("from CRCInfoExt where crcid='" +crcnodeid  + "'").list();
				int count = getCRCdocinfo.size();										
				
				//String totamilconcept = getTamilconceptfrmDB(transaction,session, crcnode.getFromsynonymid());					
				//String touwconcept =    getUWConceptfrmDB   (transaction,session, crcnode.getFromsynonymid());
				
				////System.out.println("frm:"+crcnodeext.getFromuwconcept()+"\t"+crcnodeext.getFromtamilconcept()+"\t"+crcnodeext.getFrompostag()+"\t"+crcnodeext.getRelation()+"\t"+crcnodeext.getTouwconcept() +"\t" +crcnodeext.getTotamilconcept()+"\t"+crcnodeext.getTopostag()+"\t"+count);
				CRC crc =  new CRC();
				i++;
				crc.setTam2(crcnodeext.getTotamilconcept());
				crc.setC2(crcnodeext.getTouwconcept());
				crc.setPos2(crcnodeext.getTopostag());
				crc.setQWTag2("EQW"+i);
				crc.setMWTag2("");
				crc.setRel(crcnodeext.getRelation());
				crc.setTam1(crcnodeext.getFromtamilconcept());
				String MWTag=CheckMultiword(crcnodeext.getFromtamilconcept());//Multiword check
				crc.setC1(crcnodeext.getFromuwconcept());
				crc.setPos1(crcnodeext.getFrompostag());
				crc.setQWTag1("QW");
				crc.setMWTag1(MWTag);
				expansion.add(crc);
			}																		
		//transaction.commit();
		//Collections.sort(expansion);
	} catch (HibernateException e) {
		transaction.rollback();
		e.printStackTrace();
	} finally {
		//session.close();
	}
	return expansion;
}
	
	
	/**
	 * Multiword Checker
	 * @param g_word
	 * @return MW if g_word is Multiword else return empty string(" ")
	 */
	    public String CheckMultiword(String g_word){
	    String MWtag_Qw="";
	    String[] QW_MWCheck	= g_word.split(" ");
	    int QW_MWCheckCnt=QW_MWCheck.length;
	        if(QW_MWCheckCnt>=2){
			MWtag_Qw="MW";		
	        }
	        else{
			MWtag_Qw="NMW ";
	            }
	        return MWtag_Qw;
	    }
	
	public String getTamilconceptfrmDB(Transaction transaction, Session session, int synonymid) {		
		List<UWInfo> tamilconcept_list = null;
		String tamilconcept ="";
		try {
			tamilconcept_list = (List<UWInfo>) session.createQuery("from UWInfo where synonymid=" + synonymid).list();			
			for (Iterator<UWInfo> iterator = tamilconcept_list.iterator(); iterator.hasNext();) {
				UWInfo UWObj = (UWInfo) iterator.next();			
				tamilconcept = UWObj.getTamilconcept();
			}
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
		}
		return tamilconcept;
	}
			
	
	/**
	 * 
	 * @param transaction
	 * @param session
	 * @param nodeid
	 * @return	 
	public String getUWConceptfrmDB(Transaction transaction, Session session, int tamilconceptid) {		
		List<UWInfo> synonyn_list = null;
		String uwconcept = "";		
		try {			
			synonyn_list = (List<UWInfo>) session.createQuery("from UWInfo where synonymid ="+tamilconceptid).list();
			for (Iterator<UWInfo> iterator = synonyn_list.iterator(); iterator.hasNext();) {
				UWInfo uwinfo = (UWInfo) iterator.next();
				uwconcept = uwinfo.getUwconcept();			
			}				
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} 
		return uwconcept;
	}*/
	
	/*public String getUWConceptfrmDB(Transaction transaction, Session session, int nodeid) {		
		List<Node> uwconcept_list = null;
		String uwconcept = "";
		try {
			
			uwconcept_list = (List<Node>) session.createQuery("from Node where nodeid=" + nodeid).list();
			for (Iterator<Node> iterator = uwconcept_list.iterator(); iterator
					.hasNext();) {
				Node node = (Node) iterator.next();			
				uwconcept = node.getUwconcept();
			}
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} 
		return uwconcept;
	}*/

	/*public void getQueryEX(String tamilconcept){
		Session session = HibernateUtil.getSessionFactory().openSession();		
		Transaction transaction = null;
		ArrayList<CRC> expansion = new ArrayList<CRC>();
		try {
			transaction = session.beginTransaction();
			int synonymid = 0;
			String frmuwconcept ="";
			List<Synonym> synonyntable = (List<Synonym>) session.createQuery("from Synonym where tamilconcept='" + tamilconcept+"'").list();			
			for (Iterator<Synonym> iterator = synonyntable.iterator(); iterator.hasNext();) {
				Synonym synonym = (Synonym) iterator.next();
				synonymid = synonym.getSynonymid();				
				//System.out.println("Node id:"+synonym.getSynonymid() );								
			}

				ArrayList<DocInfo> documentinformation = (ArrayList<DocInfo>) session.createQuery("from DocInfo where synonymid=" + synonymid).list();				
				for(DocInfo docinfo : documentinformation){
					//System.out.println("Document Id :---"+docinfo.getDocumentid() );
				}										
			transaction.commit();
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}*/

	/**
	*Temproarly hide the data
	*/
/*public  ArrayList<CRC> getQueryexpansion_hierarchy(String tamilconcept, String actualuwconcept, String pos, String constraint){
		Session session = HibernateUtil.getSessionFactory().openSession();		
		Transaction transaction = null;
		ArrayList<CRC> expansion = new ArrayList<CRC>();
		try {
			transaction = session.beginTransaction();		
			int frmnodeid = 0;
			int nodeid = 0;
			int i =  0;
			String frmuwconcept ="";
			ArrayList<Node> nodeinfo = (ArrayList<Node>) session.createQuery("from Node where constraintword='" + constraint + "'").list();
			for(Node objnode : nodeinfo){
				ArrayList<Synonym> synonyminfo = (ArrayList<Synonym>) session.createQuery("from Synonym where nodeid='" + objnode.getNodeid() + "'").list();
					for(Synonym objsynonym : synonyminfo){
					////System.out.println("uwconcept"+objnode.getUwconcept()+"\t"+ objsynonym.getTamilconcept()+"\t"+objsynonym.getPostag());
					CRC crc =  new CRC();
					i++;
					crc.tam1=objsynonym.getTamilconcept();
					crc.c1=getUWConceptfrmDB(transaction, session, objsynonym.getTamilconcept());
					crc.pos1=objsynonym.getPostag();
					crc.QWTag1= "EQW"+i;
					crc.MWTag1="";
					crc.rel="None";
					crc.tam2=tamilconcept;
					String MWTag=CheckMultiword(tamilconcept);//Multiword check
					crc.c2=actualuwconcept;
					crc.pos2=pos;
					crc.QWTag2="QW";
					crc.MWTag2=MWTag;
					////System.out.println("="+crc.tam1+"\t"+crc.c1+"\t"+crc.pos1+"\t");
					expansion.add(crc);
					}
			}			
		
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return expansion;
	}	*/

	
	private static QueryExpansion_DB objQueryExpansion = null;	
	public static QueryExpansion_DB instanceOf(){
		if(objQueryExpansion == null){
			objQueryExpansion = new QueryExpansion_DB();
		}
		return objQueryExpansion;
	}


public void testView(String uwconcept){
		Session session = HibernateUtil.getSessionFactory().openSession();		
		Transaction transaction = null;
		try {
		     //transaction = session.beginTransaction();
			////System.out.println("Session Started...");
   		        ArrayList<UWInfo> uwinfo = (ArrayList<UWInfo>) session.createQuery("from UWInfo where uwconcept='" + uwconcept + "'").list();					
			for(UWInfo info : uwinfo){
			//System.out.println(">>>|"+info.getTamilconcept());
			}
			////System.out.println("Session ended...");			
		        transaction.commit();
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			//session.close();
		}
		//return expansion;
	}


		
	public static void main(String args[]){
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		new QueryExpansion_DB().getQueryexpansion("சென்னை",session,transaction );
		//new QueryExpansion_DB().getQueryexpansion_hierarchy("மதுரை", "madurai(iof>city)", "Entity", "icl>food");
		//new QueryExpansion_DB().testView("chennai(iof>city)");
	}
}

