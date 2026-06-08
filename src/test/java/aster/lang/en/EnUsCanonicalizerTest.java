package aster.lang.en;

import aster.core.canonicalizer.Canonicalizer;
import aster.core.lexicon.LexiconRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 英语特定规范化测试。
 * <p>
 * 从 aster-lang-core CanonicalizerTest 迁移而来，验证英语独有的规范化规则。
 */
@DisplayName("英语规范化测试")
class EnUsCanonicalizerTest {

    private Canonicalizer canonicalizer;

    @BeforeEach
    void setUp() {
        canonicalizer = new Canonicalizer();
    }

    // ============================================================
    // 多词关键字大小写规范化测试
    // ============================================================

    @Nested
    @DisplayName("多词关键字规范化")
    class MultiWordKeywordTests {

        @Test
        void testNormalizeMultiWordKeywords_ModuleIs() {
            String input = "Module app.";
            String expected = "Module app.";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }

        @Test
        void testNormalizeMultiWordKeywords_OneOf() {
            String input = "As One Of the options.";
            String expected = "as one of options.";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }

        @Test
        void testNormalizeMultiWordKeywords_WaitFor() {
            String input = "Wait For the result.";
            String expected = "wait for result.";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }
    }

    // ============================================================
    // 冠词移除测试
    // ============================================================

    @Nested
    @DisplayName("冠词移除")
    class ArticleRemovalTests {

        @Test
        void testRemoveArticles_Basic() {
            String input = "define the function to return a value";
            String expected = "define function to return value";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }

        @Test
        void testRemoveArticles_PreserveInStrings() {
            String input = "print \"the quick brown fox\"";
            String expected = "print \"the quick brown fox\"";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }

        @Test
        void testRemoveArticles_MixedContext() {
            String input = "call the function with \"the parameter\"";
            String expected = "call function with \"the parameter\"";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }

        @Test
        void testRemoveArticles_AllArticleTypes() {
            String input = "a function takes an input and returns the result";
            String expected = "function takes input and returns result";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }

        @Test
        void testRemoveArticles_OnlyWithTrailingSpace() {
            String input = "the function with parameter";
            String expected = "function with parameter";
            assertEquals(expected, canonicalizer.canonicalize(input));
        }

        @Test
        @DisplayName("不吞模块路径里的单字母大写段（risk.A 不应变 risk.）")
        void testRemoveArticles_PreservesDottedModulePathSegment() {
            // ADR 0015：单字母大写标识符段（如 risk.A、A.a）此前被 CASE_INSENSITIVE
            // 冠词正则误判为冠词 'a' 而吞掉。冠词移除应排除 dotted 上下文。
            assertEquals("Use risk.A version 1 as Score.",
                canonicalizer.canonicalize("Use risk.A version 1 as Score."));
            assertEquals("Return A.a(amount).",
                canonicalizer.canonicalize("Return A.a(amount)."));
        }

        @Test
        @DisplayName("真冠词仍移除（修复未误伤正常冠词）")
        void testRemoveArticles_RealArticlesStillRemoved() {
            assertEquals("Use risk.Scoring version 1 as Score.",
                canonicalizer.canonicalize("Use risk.Scoring version 1 as Score."));
            // 句中真冠词 a/the 仍被移除
            assertEquals("define function to return value",
                canonicalizer.canonicalize("define the function to return a value"));
        }

        // ============================================================
        // 标识符保护：a/an/the 当参数名/变量名时不应被当冠词吞掉。
        // 判据：冠词后紧跟声明关键字 as、列表分隔符逗号、运算符词或句末/标点
        // 时，它是标识符（其后没有被它修饰的名词），必须保留。
        // ============================================================

        @Test
        @DisplayName("a 作参数名（后跟 as）不吞")
        void testArticleAsIdentifier_BeforeAs() {
            assertEquals("Rule add given a as Int, b as Int, produce Int:",
                canonicalizer.canonicalize("Rule add given a as Int, b as Int, produce Int:"));
        }

        @Test
        @DisplayName("a 在参数列表（后跟逗号）不吞")
        void testArticleAsIdentifier_BeforeComma() {
            assertEquals("given a, b, c",
                canonicalizer.canonicalize("given a, b, c"));
        }

        @Test
        @DisplayName("a 作操作数（后跟运算符）不吞——运算符词翻译成符号是预期的")
        void testArticleAsIdentifier_BeforeOperator() {
            // plus → + 是正常的运算符翻译；关键是标识符 a 被保留（不再变 `Return + b`）
            assertEquals("Return a + b.",
                canonicalizer.canonicalize("Return a plus b."));
            // equals to → ==；逻辑 and/or 保持词形。标识符 a 保留
            assertEquals("Return a == 1 or b == 2 and c == 3.",
                canonicalizer.canonicalize(
                    "Return a equals to 1 or b equals to 2 and c equals to 3."));
        }

        @Test
        @DisplayName("the/an 作标识符（后跟 as / 逗号 / 运算符）不吞")
        void testArticleAsIdentifier_TheAndAn() {
            assertEquals("given the as Int, an as Text",
                canonicalizer.canonicalize("given the as Int, an as Text"));
            // the/an 标识符保留；plus → +
            assertEquals("Return the + an.",
                canonicalizer.canonicalize("Return the plus an."));
        }

        @Test
        @DisplayName("冠词后紧跟句末/冒号（无修饰名词）不吞")
        void testArticleAsIdentifier_BeforeTerminator() {
            assertEquals("Return a.",
                canonicalizer.canonicalize("Return a."));
        }
    }

    // ============================================================
    // 英语属格 's -> . 转换测试
    // ============================================================

    @Nested
    @DisplayName("英语属格转换")
    class PossessiveTests {

        @Test
        void testEnglishPossessive_Basic() {
            String input = "driver's age";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("driver.age"),
                    "driver's age 应转换为 driver.age，实际结果: " + result);
        }

        @Test
        void testEnglishPossessive_Multiple() {
            String input = "driver's accidents";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("driver.accidents"),
                    "driver's accidents 应转换为 driver.accidents");
        }

        @Test
        void testEnglishPossessive_PreserveInStrings() {
            String input = "Return \"driver's license\".";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("\"driver's license\""),
                    "字符串内的 's 不应转换");
        }

        @Test
        @DisplayName("Unicode 标识符的 's 也能正确转换")
        void testEnglishPossessive_Unicode() {
            String input = "Müller's score";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("Müller.score"),
                    "Unicode 标识符 Müller's score 应转换为 Müller.score，实际结果: " + result);
        }
    }

    // ============================================================
    // "The result is X" -> "Return X" 重写测试
    // ============================================================

    @Nested
    @DisplayName("Result Is 重写")
    class ResultIsTests {

        @Test
        void testResultIs_Basic() {
            String input = "The result is 42.";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("Return 42."),
                    "The result is 42 应重写为 Return 42，实际结果: " + result);
        }

        @Test
        void testResultIs_WithExpression() {
            String input = "  The result is Quote with approved = true.";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("Return Quote with approved = true."),
                    "'The result is' 应重写为 'Return'，实际结果: " + result);
        }

        @Test
        void testResultIs_CaseInsensitive() {
            String input = "the result is 42.";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("Return 42."),
                    "小写 'the result is' 也应重写");
        }
    }

    // ============================================================
    // "Set X to Y" -> "Let X be Y" 重写测试
    // ============================================================

    @Nested
    @DisplayName("Set To 重写")
    class SetToTests {

        @Test
        void testSetTo_Basic() {
            String input = "Set x to 42.";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("Let x be 42."),
                    "Set x to 42 应重写为 Let x be 42，实际结果: " + result);
        }

        @Test
        void testSetTo_WithExpression() {
            String input = "  Set basePremium to calculateBase with driver, vehicle.";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("Let basePremium be calculateBase with driver, vehicle."),
                    "'Set X to Y' 应重写为 'Let X be Y'，实际结果: " + result);
        }
    }

    // ============================================================
    // 比较运算同义词测试
    // ============================================================

    @Nested
    @DisplayName("比较运算同义词")
    class ComparisonSynonymTests {

        @Test
        void testComparisonSynonym_Under() {
            String input = "x under 18";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("<"),
                    "'under' 应翻译为 '<'，实际结果: " + result);
        }

        @Test
        void testComparisonSynonym_Over() {
            String input = "x over 3";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains(">"),
                    "'over' 应翻译为 '>'，实际结果: " + result);
        }

        @Test
        void testComparisonSynonym_MoreThan() {
            String input = "x more than 3";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains(">"),
                    "'more than' 应翻译为 '>'，实际结果: " + result);
        }
    }

    @Nested
    @DisplayName("可选 is 连接词前缀比较（ADR 0013 #1b-i）")
    class IsComparatorPrefixTests {

        @Test
        @DisplayName("is at least → >=（is 被吸收）")
        void isAtLeast() {
            String result = canonicalizer.canonicalize("score is at least 700");
            assertTrue(result.contains(">="),
                    "'is at least' 应规范化为 '>='，实际: " + result);
            assertFalse(result.matches(".*\\bis\\b.*"),
                    "连接词 'is' 应被吸收，结果不应再含独立 'is'，实际: " + result);
        }

        @Test
        @DisplayName("is greater than → >（精确，非 >=）")
        void isGreaterThan() {
            String result = canonicalizer.canonicalize("score is greater than 700");
            assertTrue(result.contains(">"), "应含 '>'，实际: " + result);
            assertFalse(result.contains(">="), "'is greater than' 不应产生 '>='，实际: " + result);
        }

        @Test
        @DisplayName("各比较词精确映射：< / <= / >（排除相邻运算符 + 断言 is 已吸收）")
        void otherComparators() {
            // 统一断言三件事：期望 operator、排除相邻 operator、is 已被吸收（不残留独立 is）。
            assertComparator("x is less than 5", "<", "<=");
            assertComparator("x is at most 5", "<=", null);
            assertComparator("x is more than 5", ">", ">=");
            assertComparator("x is under 5", "<", "<=");
            assertComparator("x is over 5", ">", ">=");
        }

        /**
         * @param input    含 is-前缀比较的源码
         * @param expected 期望出现的运算符符号
         * @param excluded 不应出现的相邻运算符（null 表示不检查）
         */
        private void assertComparator(String input, String expected, String excluded) {
            String r = canonicalizer.canonicalize(input);
            assertTrue(r.contains(expected), input + " 应含 '" + expected + "'，实际: " + r);
            if (excluded != null) {
                assertFalse(r.contains(excluded),
                        input + " 不应含相邻运算符 '" + excluded + "'，实际: " + r);
            }
            assertFalse(r.matches(".*\\bis\\b.*"),
                    input + " 的连接词 'is' 应被吸收，结果不应残留独立 'is'，实际: " + r);
        }

        @Test
        @DisplayName("多空格：is at  least / is greater   than（与 TS 词法对齐）")
        void multipleSpaces() {
            String r1 = canonicalizer.canonicalize("score is at  least 700");
            assertTrue(r1.contains(">=") && !r1.matches(".*\\bis\\b.*"),
                    "多空格 'is at  least' 仍应吸收 is 并规范化为 '>='，实际: " + r1);
            String r2 = canonicalizer.canonicalize("score is greater   than 700");
            assertTrue(r2.contains(">") && !r2.matches(".*\\bis\\b.*"),
                    "多空格 'is greater   than' 仍应处理，实际: " + r2);
        }

        @Test
        @DisplayName("换行边界：is 与比较词跨行不吸收（与 TS 不跨 NEWLINE 对齐）")
        void doesNotMatchAcrossNewlines() {
            // `is` 在行尾、比较词在下一行：不应被吸收（[ \t]+ 不含 \n），
            // 否则会把两行拼接并与 TS（不跨 NEWLINE token）分歧。换行必须保留，
            // 且 `is` 不被删除（仍在结果中）。
            String r1 = canonicalizer.canonicalize("score is\nat least 700");
            assertTrue(r1.contains("\n"), "换行必须保留，不应拼接，实际: " + r1);
            assertTrue(r1.matches("(?s).*\\bis\\b.*"),
                    "跨行时 'is' 不应被吸收，应原样保留，实际: " + r1);
            // 多词比较词内部跨行：`is at` 行尾、`least` 下一行——不应吸收 is。
            String r2 = canonicalizer.canonicalize("score is at\nleast 700");
            assertTrue(r2.contains("\n") && r2.matches("(?s).*\\bis\\b.*"),
                    "比较词内部跨行时不应处理，is 应保留，实际: " + r2);
        }

        @Test
        @DisplayName("保护：is equal to / is not equal to 不被本变换器误吸（仍交由 normalizeOperator）")
        void doesNotTouchIsEqualTo() {
            // 'is equal to' 的 is 不在比较词前缀范围内，应原样保留给后续等值处理。
            String r1 = canonicalizer.canonicalize("x is equal to 5");
            assertTrue(r1.toLowerCase().contains("is equal to"),
                    "'is equal to' 不应被 is-comparator 拆解，实际: " + r1);
            String r2 = canonicalizer.canonicalize("x is not equal to 5");
            assertTrue(r2.toLowerCase().contains("is not equal to"),
                    "'is not equal to' 不应被 is-comparator 拆解，实际: " + r2);
        }

        @Test
        @DisplayName("保护：字符串字面量内的 \"is at least\" 不被改写")
        void protectsStringLiterals() {
            String input = "Return \"score is at least 700\".";
            String result = canonicalizer.canonicalize(input);
            assertTrue(result.contains("\"score is at least 700\""),
                    "字符串内的 'is at least' 应原样保留，实际: " + result);
        }

        @Test
        @DisplayName("保护：标识符片段（isover/thisAtLeast）不被误吸")
        void doesNotTouchIdentifiers() {
            // \b 词边界确保 'is' 必须是独立单词；'thisover'、'isover' 等不受影响。
            String r1 = canonicalizer.canonicalize("thisover plus 1");
            assertTrue(r1.contains("thisover"),
                    "标识符 'thisover' 不应被改写，实际: " + r1);
            // 'isover' 是单个标识符，开头虽含 'is' 但无词边界分隔，不应被拆。
            String rIsover = canonicalizer.canonicalize("isover plus 1");
            assertTrue(rIsover.contains("isover"),
                    "标识符 'isover' 不应被拆成 'is over'，实际: " + rIsover);
            // 'is' 后不是比较词时（裸 is）不处理——本变换器只管 is+比较词。
            String r2 = canonicalizer.canonicalize("result is something");
            assertTrue(r2.toLowerCase().contains("is something"),
                    "裸 'is' 后非比较词不应被吸收，实际: " + r2);
        }

        @Test
        @DisplayName("bare is（is 5）刻意不实现：is 原样保留")
        void bareIsNotImplemented() {
            String result = canonicalizer.canonicalize("x is 5");
            assertTrue(result.toLowerCase().matches(".*\\bis\\b.*"),
                    "bare 'is' 不应被本变换器处理（设计决策），实际: " + result);
        }
    }

    // ============================================================
    // 英语 Lexicon 无翻译测试
    // ============================================================

    @Test
    @DisplayName("英文 Lexicon 不应进行翻译")
    void testEnglishLexiconNoTranslation() {
        var enCanonicalizer = new Canonicalizer(LexiconRegistry.getInstance().getOrThrow("en-US"));
        String input = "if condition return true";
        String result = enCanonicalizer.canonicalize(input);
        assertEquals("if condition return true", result);
    }

    // ============================================================
    // 多词关键词不修改字符串字面量
    // ============================================================

    @Test
    @DisplayName("多词关键词规范化不应修改字符串字面量")
    void testMultiWordKeyword_NotModifyStrings() {
        String input = "Return \"This Module Is test\".";
        String result = new Canonicalizer().canonicalize(input);
        assertTrue(result.contains("\"This Module Is test\""),
                "字符串字面量内的多词关键词不应被修改，实际结果: " + result);
    }
}
