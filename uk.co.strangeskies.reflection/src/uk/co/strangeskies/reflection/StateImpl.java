/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

public class StateImpl implements State {
	private final State enclosingState;
	private final Scope scope;
	private final LocalVariableStore locals;

	private boolean returned;
	private Object returnValue;

	protected StateImpl(Scope scope) {
		this(null, scope);

		if (scope.getEnclosingScope().isPresent()) {
			throw new ReflectionException(p -> p.invalidScopeForState());
		}
	}

	protected StateImpl(State enclosingState, Scope scope) {
		this.enclosingState = enclosingState;
		this.scope = scope;

		if (enclosingState != null && enclosingState.getScope() != scope.getEnclosingScope().orElse(null)) {
			throw new ReflectionException(p -> p.invalidScopeForState());
		}

		locals = new LocalVariableStore();
	}

	@Override
	public Scope getScope() {
		return scope;
	}

	@Override
	public LocalVariableStore getEnclosingScopeVariableStore(Scope scope) {
		if (scope == this.scope) {
			return locals;
		} else if (enclosingState != null) {
			return enclosingState.getEnclosingScopeVariableStore(scope);
		} else {
			throw new ReflectionException(p -> p.cannotResolveEnclosingScope(scope));
		}
	}

	@Override
	public <J> J getEnclosingInstance(ClassDefinition<J> parentScope) {
		if (enclosingState != null) {
			return enclosingState.getEnclosingInstance(parentScope);
		} else {
			throw new ReflectionException(p -> p.cannotResolveEnclosingScope(parentScope));
		}
	}

	@Override
	public Object getReturnValue() {
		return returnValue;
	}

	@Override
	public boolean isReturned() {
		return returned;
	}

	@Override
	public void returnValue(Object value) {
		returned = true;
		returnValue = value;
	}

	@Override
	public void returnVoid() {
		returned = true;
	}
}