---
name: masvs-crypto-review
description: >
  Deep audit of cryptographic implementations against OWASP MASVS-CRYPTO controls.
  Use when code generates keys, encrypts/decrypts data, uses hashing, or touches AndroidKeyStore / iOS Keychain.
---

# Cryptography Review (MASVS-CRYPTO)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-CRYPTO-1 | App uses current, industry-standard cryptography |
| MASVS-CRYPTO-2 | Cryptographic keys managed securely throughout their lifecycle |

## Algorithm Classification

### ❌ Deprecated — FAIL immediately

| Algorithm | Issue |
|---|---|
| DES, 3DES | Broken |
| RC4 | Broken |
| MD5, SHA-1 (security use) | Collision-prone |
| RSA < 2048-bit | Insufficient key length |
| `java.util.Random` | Not cryptographically secure |
| ECB mode (any cipher) | Deterministic, pattern-leaking |

### ✅ Acceptable

| Algorithm | Notes |
|---|---|
| AES-256-GCM | Preferred for symmetric encryption |
| AES-256-CBC + HMAC-SHA256 | Acceptable with random IV |
| ChaCha20-Poly1305 | Good for KMP/mobile |
| RSA ≥ 2048-bit with OAEP | For key wrapping |
| SHA-256 / SHA-512 | General hashing |
| Argon2id / bcrypt / scrypt | Password hashing |
| `SecureRandom` | Required for all random bytes |

## Audit Scope for Dividox

Dividox uses cryptography for:
- OAuth token storage (Android Keystore / iOS Keychain)
- Encrypted local preferences (`EncryptedSharedPreferences`)
- Any future biometric-bound key operations

### Android (`androidMain`)

- `KeyGenParameterSpec` must specify purpose (`ENCRYPT|DECRYPT` or `SIGN|VERIFY`).
- Block mode must not be `ECB`.
- Key must be bound to user authentication if protecting financial data.
- `setRandomizedEncryptionRequired(true)` must be set (enforces random IV).

### iOS (`iosMain`)

- Keys stored in Keychain with `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`.
- `CryptoKit` preferred over legacy `CommonCrypto`.
- `SecKey` operations must use OAEP for RSA.

### KMP Common (`commonMain`)

- `kotlinx-crypto` or platform-specific expects must use algorithms from the acceptable list.
- No hardcoded IV, salt, or key material anywhere in source.

## Audit Steps

1. **Inventory all crypto operations** — search: `Cipher`, `KeyGenerator`, `MessageDigest`, `SecretKey`, `CryptoKit`, `SecKey`, `AES`, `RSA`, `SHA`.
2. **Check algorithm choice** against approved list above.
3. **Verify key generation** uses `KeyGenParameterSpec` (Android) or `SecKeyGenerateSymmetric` (iOS).
4. **Scan for hardcoded secrets** — base64 blobs, hex strings, `"key"`, `"secret"`, `"iv"` literals.
5. **Verify IVs are random** — never reused or derived from predictable input.
6. **Confirm key storage** in Keystore/Keychain (not in SharedPreferences or files).

## Output Format

```markdown
## MASVS-CRYPTO Audit Report

### Cryptographic Inventory
| Location | Algorithm | Mode | Key size | Storage | Status |
|----------|-----------|------|----------|---------|--------|

### CRYPTO-1 — Standard Algorithms
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### CRYPTO-2 — Key Management
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED
```

## MASTG Test Mappings

- MASTG-TEST-0013: Testing the Configuration of Cryptographic Standard Algorithms (Android)
- MASTG-TEST-0014: Testing Random Number Generation (Android)
- MASTG-TEST-0060: Testing the Configuration of Cryptographic Standard Algorithms (iOS)
