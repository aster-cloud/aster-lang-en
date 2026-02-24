# aster-lang-en -- Aster CNL 英文语言包

## 概述

提供英文（en-US）词法表、领域词汇和规范化规则，作为 Aster CNL 的基础语言包。
通过 Java SPI 机制自动注册到 `aster-lang-core` 的词法表和词汇表注册中心。

## 包含内容

| 类别 | 数量 | 说明 |
|------|------|------|
| 词法表 (lexicon) | 74 关键词 | `lexicons/en-US.json` |
| 领域词汇 | 2 | 汽车保险 (`insurance-auto`)、贷款金融 (`finance-loan`) |
| 叠加层 (overlay) | 2 | 类型推断规则 (`type-inference`)、LSP 界面文本 (`lsp-ui-texts`) |

## SPI 插件

`EnUsPlugin` 同时实现 `LexiconPlugin` 和 `VocabularyPlugin` 两个接口：

- `createLexicon()` -- 从 JSON 加载 en-US 词法表
- `createVocabulary()` / `getVocabularies()` -- 加载 2 套领域词汇
- `getOverlayResources()` -- 提供 2 个叠加层资源路径

## 规范化规则 (Canonicalization)

英文变换器属于 IR 规范化基础能力，保留在 `aster-lang-core` 的 `TransformerRegistry` 中：

| 变换器 | 功能 |
|--------|------|
| article removal | 移除冠词 the / a / an |
| english-possessive | 所有格 `'s` 转换为 `.` 属性访问 |
| result-is | `result is X` 规范化 |
| set-to | `set X to Y` 规范化 |

## 构建与测试

```bash
./gradlew build
./gradlew test
```

依赖：`aster-lang-core:0.0.1`，Java 25，JUnit 6，AssertJ 3.27。

## 发布

通过 GitHub Packages 发布：

```
cloud.aster-lang:aster-lang-en:0.0.1
```

## 许可证

Apache License 2.0
