/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import java.util.List;

import uk.co.strangeskies.fx.ModularTreeView;
import uk.co.strangeskies.fx.TreeContribution;

/**
 * A source of one or more types of {@link TreeContribution contribution} for
 * the {@link EclipseModularTreeController modular tree controller} of the given id.
 * The contribution classes returned from {@link #getContributions(String)}
 * should be instantiable by Eclipse injector.
 * 
 * @author Elias N Vasylenko
 */
public interface EclipseModularTreeContributor {
	/**
	 * @param treeId
	 *          the id of the tree to fetch appropriate contribution classes for
	 * @return a collection of contributions to be instantiated by the Eclipse
	 *         context injector on behalf of the {@link EclipseModularTreeController}.
	 */
	List<Class<? extends TreeContribution<?>>> getContributions(String treeId);

	/**
	 * @return the id of the contribution, available so that any modular tree
	 *         controller contributed to may filter accepted contributions over it
	 */
	String getContributionId();

	/**
	 * @return the ranking of the contribution, available for any modular tree
	 *         controller contributed to to determine
	 *         {@link ModularTreeView#getPrecedence() precedence}
	 */
	default int getContributionRanking() {
		return 0;
	}
}