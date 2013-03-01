package soot.jimple.infoflow.test.securibench;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class ReflectionTests extends JUnitTests {
	@Test
	public void refl1() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.reflection.Refl1: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void refl2() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.reflection.Refl2: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void refl3() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.reflection.Refl3: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void refl4() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.reflection.Refl4: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}


}
