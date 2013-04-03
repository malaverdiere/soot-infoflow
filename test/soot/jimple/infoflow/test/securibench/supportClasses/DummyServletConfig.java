package soot.jimple.infoflow.test.securibench.supportClasses;

import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class DummyServletConfig implements ServletConfig{

	@Override
	public String getInitParameter(String arg0) {
		return arg0;
	}

	@Override
	public Enumeration getInitParameterNames() {
		return new StringTokenizer("one two three");
	}

	@Override
	public ServletContext getServletContext() {
		return new DummyServletContext();
	}

	@Override
	public String getServletName() {
		return "dummyServlet";
	}

}
