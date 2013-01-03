package soot.jimple.infoflow.test;

import java.util.LinkedList;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class EasyNegativeTestCode {

	public void easyNegativeTest(){
		String tainted = TelephonyManager.getDeviceId();
		LinkedList<String> notRelevantList = new LinkedList<String>();
		LinkedList<String> list = new LinkedList<String>();
		list.add("neutral");
		notRelevantList.add(tainted);
		String taintedElement = notRelevantList.get(0);
		taintedElement.toString();
		String outcome = list.get(0);

		ConnectionManager cm = new ConnectionManager();
		cm.publish(outcome);
	}
}
