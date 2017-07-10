package org.apache.nutch.search.unl;

import org.apache.nutch.analysis.unl.ta.Rules;
public class testQuery {
	
	Rules rule = new Rules();
	public void testData(String word){
		//System.out.println("Query Word: "+word);
		String trans_out = rule.enconvert(word,false);//to enconvert Actual Query Word.
		//System.out.println(trans_out+"\n");
	}

	public static void main(String args[]){
		new testQuery().testData("மதுரைக்கு வழி");
		new testQuery().testData("மதுரைக்கு செல்லும் வழி");	
		new testQuery().testData("மதுரைக்கு செல்ல வழி");
		
		//மதுரைக்கு போகும் வழி
		//மதுரைக்கு போவதற்கான வழி
		
		new testQuery().testData("மதுரை போவதற்கு வழி");
		new testQuery().testData("மதுரைக்கு போகும் வழி");
		new testQuery().testData("மதுரைக்கு போவதற்கான வழி");
	}

}
