/**
 * 
 */
package org.openprovenance.prov.xml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.bag.HashBag;

/**
 * @author Trung Dong Huynh <tdh@ecs.soton.ac.uk>
 *
 */
public class DocumentEquality {

	static private boolean collectionEqual(Collection<?> c1, Collection<?> c2) {
		HashBag bag1 = new HashBag(c1);
		HashBag bag2 = new HashBag(c2);
		return bag1.equals(bag2);
	}

	@SuppressWarnings("unchecked")
	private static boolean statementEqual(StatementOrBundle r1, StatementOrBundle r2) {
		// If one of the statemens is a named bundle
		if (r1 instanceof NamedBundle || r2 instanceof NamedBundle) {
			if (r1 instanceof NamedBundle && r2 instanceof NamedBundle) {
				NamedBundle b1 = (NamedBundle)r1;
				NamedBundle b2 = (NamedBundle)r2;
				if (!b1.getId().equals(b2.getId())) return false;
				List<?> stmts1 = b1.getEntityOrActivityOrWasGeneratedBy();
				List<?> stmts2 = b2.getEntityOrActivityOrWasGeneratedBy();
				return statementListEqual((List<StatementOrBundle>)stmts1,
						(List<StatementOrBundle>)stmts2);
			}
		}
		// Two normal statements
		Class<?> class1 = r1.getClass();
		if (class1 != r2.getClass()) return false;
		Method[] allMethods = class1.getDeclaredMethods();
		for (Method m : allMethods) {
			if (m.getName().startsWith("get")) {
				try {
					Object attr1 = m.invoke(r1);
					Object attr2 = m.invoke(r2);
					if (attr1 == null && attr2 == null) continue;
					// Try the standard check first. This will also fail the test if either attributes is null.
					if (attr1.equals(attr2)) continue;
					if (attr1 instanceof Collection<?> && attr1 instanceof Collection<?>)
						if (collectionEqual((Collection<?>)attr1, (Collection<?>)attr2)) continue;
					// the two attributes are not equal
					return false;
				} catch (Exception e) {
					// Any exception means no equality
					return false;
				}
			}
		}

		return true;
	}

	private static boolean statementListEqual(List<StatementOrBundle> stmts1, List<StatementOrBundle> stmts2) {
		if (stmts1.size() != stmts2.size()) return false;
		// Cloning the lists to avoid modification of the originals
		List<StatementOrBundle> list1 = new ArrayList<StatementOrBundle>(stmts1);
		List<StatementOrBundle> list2 = new ArrayList<StatementOrBundle>(stmts2);
		for (StatementOrBundle stmt1: list1) {
			// Try to find the same in list2
			StatementOrBundle found = null;
			for (StatementOrBundle stmt2: list2) {
				if (statementEqual(stmt1, stmt2)) {
					found = stmt2;
					break;
				}
			}
			if (found == null) return false;
			list2.remove(found);
		}

		return true;
	}

	public static boolean check(Document d1, Document d2) {
		return statementListEqual(d1.getEntityOrActivityOrWasGeneratedBy(),
				d2.getEntityOrActivityOrWasGeneratedBy());
	}

}
