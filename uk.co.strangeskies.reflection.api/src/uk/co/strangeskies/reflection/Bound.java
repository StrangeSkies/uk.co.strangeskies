package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import uk.co.strangeskies.reflection.BoundVisitor.PartialBoundVisitor;
import uk.co.strangeskies.utilities.IdentityProperty;

public class Bound {
	private final Consumer<BoundVisitor> visitation;

	public Bound(Consumer<BoundVisitor> visitation) {
		this.visitation = visitation;

		visitation.accept(new PartialBoundVisitor() {
			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				if (b.equals(Object.class))
					new IllegalArgumentException("SHITTER").printStackTrace();
			}

			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void accept(BoundVisitor visitor) {
		visitation.accept(visitor);
	}

	@Override
	public String toString() {
		IdentityProperty<String> result = new IdentityProperty<>();

		accept(new BoundVisitor() {
			@Override
			public void acceptSubtype(Type a, InferenceVariable b) {
				result.set(a.getTypeName() + " <: " + b.getTypeName());
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				result.set(a.getTypeName() + " <: " + b.getTypeName());
			}

			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				result.set(a.getTypeName() + " <: " + b.getTypeName());
			}

			@Override
			public void acceptFalsehood() {
				result.set("false");
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				result.set(a.getTypeName() + " = " + b.getTypeName());
			}

			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				result.set(a.getTypeName() + " = " + b.getTypeName());
			}

			@Override
			public void acceptCaptureConversion(CaptureConversion c) {
				result.set(c.toString());
			}
		});
		if (result.get() == null)
			throw new AssertionError("Type of bound not understood");

		return result.get();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null || !(object instanceof Bound))
			return false;

		Bound that = (Bound) object;
		IdentityProperty<Boolean> result = new IdentityProperty<>(false);

		accept(new BoundVisitor() {
			@Override
			public void acceptSubtype(Type a, InferenceVariable b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(Type a2, InferenceVariable b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(InferenceVariable a2, Type b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptFalsehood() {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptFalsehood() {
						result.set(true);
					}
				});
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptEquality(InferenceVariable a2, Type b2) {
						result.set(a.equals(a2) && b.equals(b2));
					}
				});
			}

			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptEquality(InferenceVariable a2, InferenceVariable b2) {
						result.set((a.equals(a2) && b.equals(b2))
								|| (a.equals(b2) && b.equals(a2)));
					}
				});
			}

			@Override
			public void acceptCaptureConversion(CaptureConversion c) {
				that.accept(new PartialBoundVisitor() {
					@Override
					public void acceptCaptureConversion(CaptureConversion c2) {
						result.set(c.equals(c2));
					}
				});
			}
		});

		return result.get();
	}

	@Override
	public int hashCode() {
		IdentityProperty<Integer> result = new IdentityProperty<>();

		accept(new BoundVisitor() {
			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				result.set(a.hashCode() ^ b.hashCode());
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				result.set(a.hashCode() ^ b.hashCode() * 7);
			}

			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				result.set(a.hashCode() ^ b.hashCode() * 23);
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				result.set(a.hashCode() ^ b.hashCode() * 53);
			}

			@Override
			public void acceptSubtype(Type a, InferenceVariable b) {
				result.set(a.hashCode() ^ b.hashCode() * 67);
			}

			@Override
			public void acceptFalsehood() {
				result.set(0);
			}

			@Override
			public void acceptCaptureConversion(CaptureConversion c) {
				result.set(c.hashCode());
			}
		});

		return result.get();
	}
}
