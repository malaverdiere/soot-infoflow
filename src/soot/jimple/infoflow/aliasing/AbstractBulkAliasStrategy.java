package soot.jimple.infoflow.aliasing;

import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.heros.InfoflowCFG;

public abstract class AbstractBulkAliasStrategy extends AbstractAliasStrategy {

	public AbstractBulkAliasStrategy(InfoflowCFG cfg) {
		super(cfg);
	}

	@Override
	public boolean isInteractive() {
		return false;
	}
	
	@Override
	public boolean mayAlias(AccessPath ap1, AccessPath ap2) {
		return false;
	}

}
