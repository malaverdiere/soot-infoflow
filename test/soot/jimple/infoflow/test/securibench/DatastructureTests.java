package soot.jimple.infoflow.test.securibench;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class DatastructureTests extends JUnitTests {

	@Test
	public void datastructures1() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.datastructures.Datastructures1: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow,1);
	}

	@Test
	public void datastructures2() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.datastructures.Datastructures2: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow,1);
	}

	@Test
	public void datastructures3() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.datastructures.Datastructures3: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow,1);
	}

	@Test
	public void datastructures4() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.datastructures.Datastructures4: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	negativeCheckInfoflow(infoflow);
	}

	@Test
	public void datastructures5() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.datastructures.Datastructures5: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow,1);
	}

	@Test
	public void datastructures6() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.datastructures.Datastructures6: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow,1);
	}

}
