# Releasing AGC to Maven Central

This project is configured for **Sonatype OSSRH** (Maven Central). Library modules are deployed; **`agc-demo-app`** and **`agc-architecture-tests`** skip deploy (`maven.deploy.skip`).

---

## Prerequisites (one-time)

### 1. Register with Sonatype

1. Create an account at [central.sonatype.com](https://central.sonatype.com/) (or legacy [issues.sonatype.org](https://issues.sonatype.org/) if you still use JIRA).
2. **Verify namespace** for your `groupId`. For **`com.framework.agent`** you must prove you own that domain (DNS TXT record) **or** use a GitHub-based verification if Sonatype offers it for your coordinates.
3. Wait until the namespace is **approved** before deploying.

### 2. Install GPG and create a signing key

**You must have at least one *secret* key** in the same keyring that Maven’s `gpg` uses. If the build prints `gpg: no default secret key: No secret key`, you still need to create or import a key (or point Maven at the right `gpg` binary — see below).

```bash
# 1) Create a key (interactive). Use RSA 4096 and the same email as pom.xml <developers>.
gpg --full-generate-key

# 2) Confirm a secret key exists (must show a line starting with "sec")
gpg --list-secret-keys --keyid-format LONG

# 3) Publish the **public** key so Central can verify signatures
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

`YOUR_KEY_ID` is the long hex after `rsa4096/` on the `sec` line (or the fingerprint’s last 8/16 chars — use what `gpg --list-secret-keys` shows).

**If keys show up under `gpg2` but not `gpg`:** Ubuntu/Debian often install both; Maven may call `gpg` with an empty keyring. Use:

```bash
gpg2 --list-secret-keys --keyid-format LONG
mvn clean deploy -Prelease -Dgpg.executable=gpg2
```

**To force a specific key** (when you have several):

```bash
mvn clean deploy -Prelease -Dgpg.keyname=YOUR_KEY_ID
```

Ensure **`gpg-agent`** can sign (see troubleshooting). For CI, use a dedicated key and non-interactive signing as documented by Sonatype.

### 3. Maven `settings.xml`

Edit **`~/.m2/settings.xml`** (never commit tokens):

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username><!-- Central portal user token username --></username>
      <password><!-- Central portal user token password --></password>
    </server>
  </servers>
</settings>
```

The **`id` must be `ossrh`** — it matches `<distributionManagement>` and `nexus-staging-maven-plugin` in the root `pom.xml`.

---

## Release steps (this repository)

The reactor is versioned **`1.0.0`** for the first GA line. Adjust if you need a different number.

### 1. Sync source of truth

```bash
git status   # clean tree
mvn clean verify
```

All tests must pass.

### 2. Align `scm` with the Git tag (recommended)

In the root **`pom.xml`**, for the commit you will tag, set:

```xml
<tag>v1.0.0</tag>
```

(use the same tag name you will create in Git). After the release, you can set `<tag>HEAD</tag>` again on the development branch.

### 3. Commit and tag

```bash
git add -A
git commit -m "Release 1.0.0"
git tag -s v1.0.0 -m "AGC 1.0.0"
git push origin main --tags
```

### 4. Deploy to OSSRH (sign + stage)

From the repository root:

```bash
mvn clean deploy -Prelease
```

This **release** profile:

- Attaches **sources** and **javadoc** JARs  
- **Signs** artifacts with GPG  
- Uploads to the **staging** repository (`nexus-staging-maven-plugin`, `autoReleaseAfterClose=true`)

If `autoReleaseAfterClose` succeeds, the staging repo **closes and releases** automatically; artifacts then sync to **Maven Central** (often **15–30+ minutes**).

### 5. Verify on Central

- [search.maven.org — `g:com.framework.agent`](https://search.maven.org/search?q=g:com.framework.agent)  
- Confirm **`agc-spring-boot-starter`** and transitive library artifacts appear.

---

## After the first release (ongoing development)

1. Bump the reactor to the next **SNAPSHOT**, e.g. **`1.0.1-SNAPSHOT`**:

   ```bash
   mvn versions:set -DnewVersion=1.0.1-SNAPSHOT -DprocessAllModules=true -DgenerateBackupPoms=false
   ```

   (The **`versions-maven-plugin`** is declared in the root `pluginManagement`.)

2. Set **`<scm><tag>HEAD</tag></scm>`** again if you had set a release tag.
3. Update **README / QUICKSTART** dependency examples if you document the SNAPSHOT line for contributors.
4. Commit: `Prepare 1.0.1-SNAPSHOT`.

For **1.0.1** GA, repeat the tag + `mvn clean deploy -Prelease` flow with `1.0.1`.

---

## Troubleshooting

| Symptom | What to check |
|---------|----------------|
| **401 / 403** on deploy | `settings.xml` server **id** = `ossrh`; token not expired; namespace approved |
| **`maven-gpg-plugin` … Exit code: 2** | See **GPG exit code 2** below |
| **Rule failure: snapshot dependencies** | Release profile enforcer: only **release** versions of dependencies (Spring Boot BOM is fine) |
| **Missing artifacts** | Only library modules deploy; demo/arch-tests are skipped by design |
| **Javadoc warnings** | Root POM sets `failOnError=false`; fix Javadoc and tighten later if you want stricter builds |

### GPG sign failed (`Exit code: 2`)

Common on Linux when Maven invokes `gpg` to sign the POM/JARs:

1. **Secret key present**  
   ```bash
   gpg --list-secret-keys --keyid-format LONG
   ```  
   If empty, create/import a key and use the same identity as in `pom.xml` `<developers>`.

2. **Terminal / pinentry** (passphrase prompt never appears)  
   In the **same shell** before `mvn`:  
   ```bash
   export GPG_TTY=$(tty)
   gpgconf --kill gpg-agent
   gpg-agent --daemon
   ```  
   Then run `mvn clean deploy -Prelease` again.

3. **`gpg` vs `gpg2`**  
   If signing works on the CLI only with `gpg2`:  
   ```bash
   mvn clean deploy -Prelease -Dgpg.executable=gpg2
   ```

4. **Headless / loopback** (CI or no GUI pinentry)  
   In `~/.gnupg/gpg-agent.conf`:  
   `allow-loopback-pinentry`  
   Then restart `gpg-agent`. You may need `gpgArguments` with `--pinentry-mode loopback` and passphrase supplied via Maven (see [GPG usage with Maven](https://maven.apache.org/plugins/maven-gpg-plugin/sign-mojo.html)) — prefer local interactive deploy for the first releases.

5. **Dry-run without signing** (does **not** produce Central-ready artifacts):  
   ```bash
   mvn clean verify -Prelease -Dgpg.skip=true
   ```  
   **Do not** use `-Dgpg.skip=true` for the actual Central upload; Sonatype requires signatures.

---

## Quick command reference

```bash
# Full check
mvn clean verify

# Publish (after OSSRH + GPG + settings.xml)
export GPG_TTY=$(tty)   # often required on Linux so pinentry works
mvn clean deploy -Prelease

# Same but force gpg2 binary
# mvn clean deploy -Prelease -Dgpg.executable=gpg2

# Next development version
mvn versions:set -DnewVersion=1.0.1-SNAPSHOT -DprocessAllModules=true -DgenerateBackupPoms=false
```

More context: [LIBRARY.md](LIBRARY.md) (coordinates, discovery).
