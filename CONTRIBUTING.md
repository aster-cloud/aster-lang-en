# Contributing to Aster Lang Lexicons

> Help bring Policy-as-Code to your native language.

This guide is the **reference** for all Aster Lang lexicon contributions.
For language-specific repos (`aster-lang-zh`, `aster-lang-de`, etc.), see their
localized copies linking back here.

---

## 1. Choose your contribution path

| Path | What you do | Aster involvement | Best for |
|---|---|---|---|
| **Official lexicon** | Aster team direct maintenance | 100% | en / zh / de (core markets) |
| **Officially-endorsed lexicon** | Fork template → translate → PR to `aster-cloud/aster-lang-<lang>` | Review + security audit + maven publish | Mainstream languages (ja / fr / es / ...) |
| **Community-maintained lexicon** | Fork template → translate → keep in your own GitHub org | Doc listing only, no endorsement | Long-tail languages / industry dialects |

---

## 2. Contribution workflow (officially-endorsed)

1. Check [Wanted Languages board](https://aster-lang.dev/community/wanted-languages) — your language may already have an open PR or paid author.
2. Fork [`aster-lang-template`](https://github.com/aster-cloud/aster-lang-template) → complete the **15-minute tutorial** in its README.
3. Open a PR to `aster-cloud/aster-lang-<lang>-<region>` (repo created by Aster team upon initial request).
4. CI validates the lexicon JSON + Aster reviewer approves → merge.
5. Aster publishes to maven central / npm.
6. You're added to the contributor roster.

---

## 3. Translation rules

### 3.1 Keyword translation principles

- ✅ **Prefer industry terminology over colloquial words**
  - e.g. translate `Module` to `モジュール` rather than `組`
- ✅ **Keywords must be unique within the same lexicon** (different keys must not map to the same string, except those listed in `canonicalization.allowedDuplicates`)
- ❌ **Do not use Aster reserved characters** in keyword values: `[](),.;:=`
- ❌ Keyword values must not start with digits
- ✅ For multi-word keywords, use space separators (not underscores)

### 3.2 Punctuation

Every lexicon must declare three separators explicitly:

- `listSeparator` (default `,`)
- `enumSeparator` (default `,`)
- `statementEnd` (default `.`)

German / French may use `,` for decimals — make sure `listSeparator` doesn't conflict.

### 3.3 Vocabularies (optional, industry terminology)

If your language has industry-specific term packs (e.g. Japanese finance `finance-loan-ja-JP.json`), include them in `src/main/resources/vocabularies/`.

Vocabulary IDs may overlap across languages (e.g. `finance-loan` exists in en / zh / ja with different localized terms).

---

## 4. Review SLA

| Stage | Aster team commitment |
|---|---|
| First acknowledgment | **24 hours** (label + reviewer assigned) |
| First full review | **7 days** (substantive feedback on translation quality + technical correctness) |
| Merge or final decision | **30 days** (no PR is left to rot) |

---

## 5. Reviewer requirements

- Reviewer **must be a native speaker** of the target language, OR hold professional translation credentials.
- At least 1 Aster team member acts as **co-reviewer** for technical correctness (SPI compliance, build integrity, security).
- **Both must approve** before merge.

---

## 6. DCO + CLA

- All commits must be sign-off: `git commit -s` ([Developer Certificate of Origin](https://developercertificate.org/))
- Major contributions (≥ 1 complete lexicon PR) require signing the Aster CLA (one-time, electronic).

---

## 7. Incentives — Aster Language Steward program

Merge **≥ 2 lexicons** OR maintain 1 lexicon for **≥ 12 months**:

- 🏷️ "Aster Language Steward" badge on docs/contributors page
- 💰 **¥3,000 / year platform credit** (Steward-exclusive)
- 📝 Listed publicly in [contributor roster](https://aster-lang.dev/community/contributors)
- 🎙️ Priority participation in new SPI ABI design discussions

---

## 8. Maintenance commitment

**You commit to:**
- Tracking ABI upgrades (v1 → v2 with 6-month migration window)
- Fixing community-reported translation bugs
- Responding to lexicon-related issues quarterly

**Aster team provides:**
- At least 1 co-maintainer as backstop
- 6-month advance notice for ABI breaking changes
- Translation quality review (not language nit-picking)

---

## 9. Code of Conduct

Be respectful. Disagreement is fine — personal attacks aren't. See [Contributor Covenant](https://www.contributor-covenant.org/version/2/1/code_of_conduct/) v2.1 as our baseline.

---

## License

By contributing, you agree your contributions will be licensed under [Apache License 2.0](LICENSE).
