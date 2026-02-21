package aster.lang.en;

import aster.core.lexicon.EnUsLexicon;
import aster.core.lexicon.Lexicon;
import aster.core.lexicon.LexiconPlugin;

/**
 * English language pack plugin (en-US).
 * <p>
 * Registers the English lexicon into {@link aster.core.lexicon.LexiconRegistry} via SPI.
 * English transformers (english-possessive, result-is, set-to) are part of the IR
 * normalization baseline and remain in aster-lang-core.
 */
public final class EnUsPlugin implements LexiconPlugin {

    @Override
    public Lexicon createLexicon() {
        return EnUsLexicon.INSTANCE;
    }
}
