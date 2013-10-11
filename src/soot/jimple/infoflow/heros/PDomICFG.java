/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors: Marc-André Laverdière and others.
 ******************************************************************************/
package soot.jimple.infoflow.heros;


import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

import java.util.Set;

public interface PDomICFG extends BiDiInterproceduralCFG<Unit,SootMethod>{

    public UnitContainer getPostdominatorOf(Unit u);

    public Set<?> getReadVariables(SootMethod caller, Stmt inv);

    public Set<?> getWriteVariables(SootMethod caller, Stmt inv);

    /**
     * Abstraction of a postdominator. This is normally a unit. In cases in which
     * a statement does not have a postdominator, we record the statement's
     * containing method and say that the postdominator is reached when the method
     * is left. This class MUST be immutable.
     *
     * @author Steven Arzt
     */
    static public class UnitContainer {

        private final Unit unit;
        private final SootMethod method;

        public UnitContainer(Unit u) {
            unit = u;
            method = null;
        }

        public UnitContainer(SootMethod sm) {
            unit = null;
            method = sm;
        }

        @Override
        public int hashCode() {
            return 31 * (unit == null ? 0 : unit.hashCode())
                    + 31 * (method == null ? 0 : method.hashCode());
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof UnitContainer))
                return false;
            UnitContainer container = (UnitContainer) other;
            if (this.unit == null) {
                if (container.unit != null)
                    return false;
            }
            else
                if (!this.unit.equals(container.unit))
                    return false;
            if (this.method == null) {
                if (container.method != null)
                    return false;
            }
            else
                if (!this.method.equals(container.method))
                    return false;

            assert this.hashCode() == container.hashCode();
            return true;
        }

        public Unit getUnit() {
            return unit;
        }

        public SootMethod getMethod() {
            return method;
        }

        @Override
        public String toString() {
            if (method != null)
                return "(Method) " + method.toString();
            else
                return "(Unit) " + unit.toString();
        }

    }
}
