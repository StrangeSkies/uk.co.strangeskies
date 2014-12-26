package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;

public interface GenericTypeContext<T extends GenericDeclaration> {
	public Type getDeclaringType();

	public T getGenericDeclaration();
}
