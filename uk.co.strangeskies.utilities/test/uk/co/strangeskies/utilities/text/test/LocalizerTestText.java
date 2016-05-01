package uk.co.strangeskies.utilities.text.test;

import uk.co.strangeskies.utilities.text.LocalizationText;
import uk.co.strangeskies.utilities.text.LocalizedString;

public interface LocalizerTestText extends LocalizationText<LocalizerTestText> {
	LocalizedString missingMethod();

	LocalizedString simple();

	LocalizedString anotherSimple();

	LocalizedString substitution(String item);

	LocalizedString multipleSubstitution(String first, String second);

	default LocalizedString defaultMethod() {
		return substitution("default");
	}
}
