# Releasing AGC to Maven Central

This project publishes via the **Sonatype Central Portal** ([`central-publishing-maven-plugin`](https://central.sonatype.org/publish/publish-portal-maven/)) to **Maven Central**. Library modules are included in the deployment bundle; **`agc-demo-app`**, **`agc-architecture-tests`**, and **`agc-publish`** are excluded from the Central bundle (`excludeArtifacts`) and skip Maven deploy (`maven.deploy.skip`). The **`agc-publish`** module is an empty `pom` listed **last** in the reactor so the plugin’s bundle-and-upload step runs after all other modules (the plugin only uploads from the **last** project that executes the goal).

**Important:** Central Portal **user tokens** use `settings.xml` server id **`central`**, not legacy **`ossrh`** + `s01.oss.sonatype.org`. Using a Portal token against the old Nexus staging URL causes **401 Unauthorized**.

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

Generate a **User Token** from [central.sonatype.com](https://central.sonatype.com/) (Account / token — see [Sonatype: User tokens](https://central.sonatype.org/publish/generate-portal-token/)).

Edit **`~/.m2/settings.xml`** (never commit tokens):

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username><!-- token username from Central Portal --></username>
      <password><!-- token password from Central Portal --></password>
    </server>
  </servers>
</settings>
```

The **`id` must be `central`** — it matches `publishingServerId` in the root `pom.xml` **`release`** profile (`central-publishing-maven-plugin`).

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

### 4. Deploy to Central Portal (sign + bundle + publish)

From the repository root:

```bash
export GPG_TTY=$(tty)
mvn clean deploy -Prelease
```

This **release** profile:

- Attaches **sources** and **javadoc** JARs  
- **Signs** artifacts with GPG  
- Uses **`central-publishing-maven-plugin`** to upload a bundle to **central.sonatype.com** (`autoPublish` is enabled)

After validation, artifacts sync to **Maven Central** (allow **15–30+ minutes** for search/index). You can also check **Deployments** on [central.sonatype.com](https://central.sonatype.com/).

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
| **401 / 403** on deploy | Server **id** = **`central`** (Portal token); not legacy `ossrh`. Regenerate token; no extra spaces; XML-escape `&` in password as `&amp;` |
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

6. **Central validation: `Namespace 'com.framework.agent' is not allowed`**  
   Your Sonatype account has not completed **namespace verification** for that `groupId`. In [central.sonatype.com](https://central.sonatype.com/) open **Namespaces**, add `com.framework.agent`, and finish the flow (DNS TXT on the domain that backs the reverse-DNS groupId, or whatever verification Sonatype offers). Until the namespace shows as **approved**, every deployment will fail for these coordinates.

7. **Central validation: `Could not find a public key by the key fingerprint`**  
   The key you used to produce the `.asc` files must be published to a **keyserver Sonatype queries** (e.g. [keys.openpgp.org](https://keys.openpgp.org/) or `keyserver.ubuntu.com`). After signing locally, upload the **public** key, then wait a few minutes and redeploy:  
   ```bash
   gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
   # or: gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```  
   Use the same key Maven uses (`gpg --list-secret-keys` / `-Dgpg.keyname=…` if needed).

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
