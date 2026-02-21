package aster.lang.en;

import aster.core.identifier.DomainVocabulary;
import aster.core.identifier.VocabularyLoader;
import aster.core.identifier.VocabularyPlugin;
import aster.core.lexicon.DynamicLexicon;
import aster.core.lexicon.Lexicon;
import aster.core.lexicon.LexiconPlugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 英文语言包插件 (en-US)。
 * <p>
 * 从 JSON 配置加载英文词法表，通过 SPI 注册到 {@link aster.core.lexicon.LexiconRegistry}。
 * 英文变换器（english-possessive, result-is, set-to）属于 IR 规范化基础能力，
 * 保留在 aster-lang-core 的 {@link aster.core.canonicalizer.TransformerRegistry} 中。
 * <p>
 * 同时实现 {@link VocabularyPlugin}，提供英文领域词汇表（汽车保险、贷款金融）。
 */
public final class EnUsPlugin implements LexiconPlugin, VocabularyPlugin {

    @Override
    public Lexicon createLexicon() {
        String json = loadResource("lexicons/en-US.json");
        return DynamicLexicon.fromJsonString(json);
    }

    @Override
    public DomainVocabulary createVocabulary() {
        return loadVocabulary("vocabularies/insurance-auto-en-US.json");
    }

    @Override
    public List<DomainVocabulary> getVocabularies() {
        return List.of(
            loadVocabulary("vocabularies/finance-loan-en-US.json")
        );
    }

    private DomainVocabulary loadVocabulary(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return VocabularyLoader.loadFromStream(is);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load vocabulary: " + path, e);
        }
    }

    private String loadResource(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load resource: " + path, e);
        }
    }
}
