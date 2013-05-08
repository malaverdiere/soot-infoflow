package soot.jimple.infoflow.test.securibench;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class StrongUpdateTests extends JUnitTests {

	@Test
	public void strongupdates1() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.strong_updates.StrongUpdates1: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	negativeCheckInfoflow(infoflow);
	}

	@Test
	public void strongupdates2() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.strong_updates.StrongUpdates2: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	negativeCheckInfoflow(infoflow);
	}

	@Test
	public void strongupdates3() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.strong_updates.StrongUpdates3: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	negativeCheckInfoflow(infoflow);
	}

	@Test
	public void strongupdates4() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.strong_updates.StrongUpdates4: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow,1);
	}

	@Test
	public void strongupdates5() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.strong_updates.StrongUpdates5: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	negativeCheckInfoflow(infoflow);
	}

}
