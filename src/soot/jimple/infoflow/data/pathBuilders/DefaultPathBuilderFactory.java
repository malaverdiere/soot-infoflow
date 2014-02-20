package soot.jimple.infoflow.data.pathBuilders;

/**
 * Default factory class for abstraction path builders
 * 
 * @author Steven Arzt
 */
public class DefaultPathBuilderFactory implements IPathBuilderFactory {
	
	/**
	 * Enumeration containing the supported path builders
	 */
	public enum PathBuilder {
		Recursive,
		Threaded
	}
	
	private final PathBuilder pathBuilder;
	
	/**
	 * Creates a new instance of the {@link DefaultPathBuilderFactory} class
	 */
	public DefaultPathBuilderFactory() {
		this.pathBuilder = PathBuilder.Threaded;
	}

	/**
	 * Creates a new instance of the {@link DefaultPathBuilderFactory} class
	 * @param builder The path building algorithm to use
	 */
	public DefaultPathBuilderFactory(PathBuilder builder) {
		this.pathBuilder = builder;
	}
	
	@Override
	public IAbstractionPathBuilder createPathBuilder(int maxThreadNum) {
		switch (pathBuilder) {
		case Recursive :
			return new RecursivePathBuilder(maxThreadNum);
		case Threaded :
			return new ThreadedPathBuilder(maxThreadNum);
		}
		throw new RuntimeException("Unsupported path building algorithm");
	}
	
}
