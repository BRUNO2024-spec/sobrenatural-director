# Kaggle/GitHub Preparation Audit

Audit date: 2026-09-15.

## Repository State

The workspace root is not currently a Git repository: `git status`, `git remote -v`, and `git branch --show-current` all fail with `not a git repository`. No `.git` directory was found under the workspace. Therefore there is currently no remote, branch, commit history, or staged set to trust. No `git init`, commit, or push was performed.

The workspace is approximately `10G` and contains `25,968` files. The largest local groups are `runtime-tests/` (`9.8G`), `original_mods/` (`140M`), `director/` (`18M`), `analysis/` (`18M`), `decompiled/` (`11M`) and `tools/` (`14M`). The root inventory includes `2,678` JARs, `2,760` logs, `1,233` region files, `4,137` data files, `701` Java files, `2,553` Markdown files and `4,483` JSON files.

## Kaggle Payload

The future GitHub/Kaggle payload should include the Director source under `director/src/`, Gradle wrapper/build metadata, offline benchmark source under `director/src/main/java/com/sobrenaturaldirector/benchmark/`, benchmark tests, `analysis/benchmark/`, `analysis/ml/`, `ml/README.md`, the benchmark JSON/JSONL/properties outputs under `runtime-tests/benchmark/`, and concise project documentation/manifests. There is no standalone `tools/benchmark/`; the benchmark tooling is in the Director module. The benchmark output is about `1.7M` and is intentionally retained as a reproducible baseline artifact.

The payload must not include Minecraft worlds/saves, Forge runtimes, logs, crash reports, build output, caches, proprietary/original mod JARs, temporary PID files, or future ML datasets/checkpoints. Runtime evidence outside `runtime-tests/benchmark/` is local-only and is excluded wholesale.

## Secret Audit

No secrets were found by filename or content-pattern audit in the text files scanned outside generated/runtime/binary directories. No values were printed. The audit found no `.env`, `kaggle.json`, credential file, API-key pattern, bearer token, cloud-key pattern, or Jupyter token. This is a pattern audit, not a guarantee against secrets encoded in binary files.

## Ignore Policy

The root `.gitignore` was extended to exclude local Forge/Minecraft state, all JAR/archive binaries, build and Gradle output, logs/crash reports, worlds/saves, ML artifacts/datasets/checkpoints, credentials/tokens and notebook checkpoints. `runtime-tests/benchmark/` is explicitly re-included because it is the reproducible machine-readable baseline.

## Safety Assessment

`SAFE_TO_COMMIT=NO` for now because there is no Git repository and no reviewed staging area. After `git init`, review `git status --short`, `git diff --cached --stat`, and the secret scan again before the first commit. Estimated first reviewed commit after exclusions: approximately `4-10M` for the Kaggle-focused payload above (`2.2M` Director source, `1.7M` benchmark output, about `40K` benchmark source/tests and documentation/wrappers). Including all non-generated `tools/runtime` drivers would add about `5.2M` and is not necessary for Kaggle ML reproduction; exact size requires staging in the newly initialized repository.

Release hardening remains `PARTIAL_FINAL_INTEGRATION_LONG_RUN_RELEASE_HARDENING`; this preparation does not change its gate, candidate status, Pojav blocker, production behavior, or protected world.

## Recommended Commands

Do not execute these automatically:

```bash
cd /home/desktop/Documentos/SOBRENATURAL-DIRECTOR
git init -b main
git add .gitignore README.md director/src director/build.gradle director/gradle director/gradlew director/gradlew.bat director/gradle.properties director/settings.gradle analysis/benchmark analysis/ml ml runtime-tests/benchmark
git status --short
git diff --cached --stat
git diff --cached --check
git remote add origin <GITHUB_REPOSITORY_URL>
git commit -m "Add Director benchmark workspace foundation"
git push -u origin main
```

The `git add` command is intentionally followed by review and must not be run until the intended GitHub repository and scope are confirmed. Commit and push are not part of this audit.

## Final Staging and Clean Room

`GIT_INITIALIZED=YES`

`BRANCH=main`

`REMOTE=NONE_NOT_CONFIGURED`

`STAGED_FILE_COUNT=368`

`STAGED_SIZE=2705055 bytes`

`GRADLE_WRAPPER_INCLUDED=YES`

`GRADLE_WRAPPER_TYPE=official Gradle 4.4.1 wrapper, SHA-256 4e318d74d06aa7b998091345c397a3c7c4b291b59da31e6f9c772a596711acac`

`PROPRIETARY_JARS_STAGED=0`

`SECRETS_FOUND=NO`

`CLEAN_ROOM_BENCHMARK=PASS`

`CLEAN_ROOM_TEST_RESULT=295 PASS, 0 failures, 0 errors, 0 skipped`

`CLEAN_ROOM_GRADLE_WRAPPER=PASS`

`MISSING_EXTERNAL_RUNTIME_DEPENDENCIES=Forge/Minecraft runtime and optional proprietary provider mods are required only for Forge/integrated runtime validation; the offline benchmark and all 295 tests compile and run without them`

`SAFE_TO_COMMIT=YES`

The clean-room source was reconstructed from the Git index at `/tmp/opencode/sobrenatural-director-cleanroom-r2`; no original workspace build output, runtime-test worlds, proprietary JAR, or local cache was used. `git diff --cached --check` is clean. The staging audit found no prohibited staged file; the only staged JAR is the allowlisted Gradle wrapper.
