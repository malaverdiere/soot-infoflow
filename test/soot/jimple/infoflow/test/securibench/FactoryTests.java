package soot.jimple.infoflow.test.securibench;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class FactoryTests extends JUnitTests {

	@Test
	public void factories1() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.factories.Factories1: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void factories2() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.factories.Factories2: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void factories3() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.factories.Factories3: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

}
