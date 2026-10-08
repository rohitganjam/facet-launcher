plugins {
    id("com.android.application") version "9.4.1" apply false
    // Pins the Kotlin compiler version explicitly rather than relying on AGP 9's built-in default.
    id("org.jetbrains.kotlin.android") version "2.4.10" apply false
    // Plain-JVM Kotlin, for :detekt-rules only — a custom Detekt rule has to be a separate,
    // pre-compiled module (Detekt loads it as a plugin jar), so it can't be an Android module itself.
    id("org.jetbrains.kotlin.jvm") version "2.4.10" apply false
    // 2.0.0-alpha.6, not the 1.23.x stable line — 1.23.8's bundled Kotlin-compiler-adjacent code
    // crashes parsing this machine's JDK 25 version string ("25.0.3"); 2.0's alpha line supports
    // it. Revisit pinning to a stable release once 2.0 ships one.
    id("dev.detekt") version "2.0.0-alpha.6" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    id("com.android.test") version "9.4.1" apply false
}

// Self-installs the repo's tracked git hooks (.githooks/) into the real hooks directory on every
// Gradle configuration pass — automatic for every contributor, no manual `git config
// core.hooksPath` opt-in step to forget (see .githooks/pre-commit's own doc comment). Resolves
// the *common* git dir via `git rev-parse --git-common-dir` rather than assuming ".git/hooks",
// since ".git" is a file (not the hooks directory) in a worktree checkout. Copies only when the
// tracked hook differs from what's installed, so this is a no-op on every later build; silently
// does nothing if git isn't on PATH or this isn't a git checkout at all (e.g. a source tarball).
run {
    val hooksSourceDir = file(".githooks")
    if (hooksSourceDir.exists()) {
        val gitCommonDir = try {
            ProcessBuilder("git", "rev-parse", "--git-common-dir")
                .directory(rootDir)
                .start()
                .let { process ->
                    val output = process.inputStream.bufferedReader().readText().trim()
                    process.waitFor()
                    output
                }
        } catch (e: Exception) {
            ""
        }
        if (gitCommonDir.isNotEmpty()) {
            val gitHooksDir = rootDir.resolve(gitCommonDir).resolve("hooks")
            if (gitHooksDir.exists()) {
                hooksSourceDir.listFiles()?.filter { it.isFile }?.forEach { hook ->
                    val target = gitHooksDir.resolve(hook.name)
                    if (!target.exists() || target.readText() != hook.readText()) {
                        hook.copyTo(target, overwrite = true)
                        target.setExecutable(true)
                    }
                }
            }
        }
    }
}
