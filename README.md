# Immersive Aircraft — 1.12.2 backport

[![GitHub][github-badge]][github]
[![Discord][discord-badge]][discord]
[![Modrinth][modrinth-badge]][modrinth]
[![CurseForge][curseforge-badge]][curseforge]

[github-badge]: https://img.shields.io/badge/GitHub-Source-181717?style=for-the-badge&logo=github&logoColor=white
[github]: https://github.com/Vendetta1871/ImmersiveAircraft-1.12.2
[discord-badge]: https://img.shields.io/badge/Discord-Join-5865F2?style=for-the-badge&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyByb2xlPSJpbWciIHZpZXdCb3g9IjAgMCAyNCAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48dGl0bGU%2BRGlzY29yZDwvdGl0bGU%2BPHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0yMC4zMTcgNC4zNjk4YTE5Ljc5MTMgMTkuNzkxMyAwIDAwLTQuODg1MS0xLjUxNTIuMDc0MS4wNzQxIDAgMDAtLjA3ODUuMDM3MWMtLjIxMS4zNzUzLS40NDQ3Ljg2NDgtLjYwODMgMS4yNDk1LTEuODQ0Ny0uMjc2Mi0zLjY4LS4yNzYyLTUuNDg2OCAwLS4xNjM2LS4zOTMzLS40MDU4LS44NzQyLS42MTc3LTEuMjQ5NWEuMDc3LjA3NyAwIDAwLS4wNzg1LS4wMzcgMTkuNzM2MyAxOS43MzYzIDAgMDAtNC44ODUyIDEuNTE1LjA2OTkuMDY5OSAwIDAwLS4wMzIxLjAyNzdDLjUzMzQgOS4wNDU4LS4zMTkgMTMuNTc5OS4wOTkyIDE4LjA1NzhhLjA4MjQuMDgyNCAwIDAwLjAzMTIuMDU2MWMyLjA1MjggMS41MDc2IDQuMDQxMyAyLjQyMjggNS45OTI5IDMuMDI5NGEuMDc3Ny4wNzc3IDAgMDAuMDg0Mi0uMDI3NmMuNDYxNi0uNjMwNC44NzMxLTEuMjk1MiAxLjIyNi0xLjk5NDJhLjA3Ni4wNzYgMCAwMC0uMDQxNi0uMTA1N2MtLjY1MjgtLjI0NzYtMS4yNzQzLS41NDk1LTEuODcyMi0uODkyM2EuMDc3LjA3NyAwIDAxLS4wMDc2LS4xMjc3Yy4xMjU4LS4wOTQzLjI1MTctLjE5MjMuMzcxOC0uMjkxNGEuMDc0My4wNzQzIDAgMDEuMDc3Ni0uMDEwNWMzLjkyNzggMS43OTMzIDguMTggMS43OTMzIDEyLjA2MTQgMGEuMDczOS4wNzM5IDAgMDEuMDc4NS4wMDk1Yy4xMjAyLjA5OS4yNDYuMTk4MS4zNzI4LjI5MjRhLjA3Ny4wNzcgMCAwMS0uMDA2Ni4xMjc2IDEyLjI5ODYgMTIuMjk4NiAwIDAxLTEuODczLjg5MTQuMDc2Ni4wNzY2IDAgMDAtLjA0MDcuMTA2N2MuMzYwNC42OTguNzcxOSAxLjM2MjggMS4yMjUgMS45OTMyYS4wNzYuMDc2IDAgMDAuMDg0Mi4wMjg2YzEuOTYxLS42MDY3IDMuOTQ5NS0xLjUyMTkgNi4wMDIzLTMuMDI5NGEuMDc3LjA3NyAwIDAwLjAzMTMtLjA1NTJjLjUwMDQtNS4xNzctLjgzODItOS42NzM5LTMuNTQ4NS0xMy42NjA0YS4wNjEuMDYxIDAgMDAtLjAzMTItLjAyODZ6TTguMDIgMTUuMzMxMmMtMS4xODI1IDAtMi4xNTY5LTEuMDg1Ny0yLjE1NjktMi40MTkgMC0xLjMzMzIuOTU1NS0yLjQxODkgMi4xNTctMi40MTg5IDEuMjEwOCAwIDIuMTc1NyAxLjA5NTIgMi4xNTY4IDIuNDE5IDAgMS4zMzMyLS45NTU1IDIuNDE4OS0yLjE1NjkgMi40MTg5em03Ljk3NDggMGMtMS4xODI1IDAtMi4xNTY5LTEuMDg1Ny0yLjE1NjktMi40MTkgMC0xLjMzMzIuOTU1NC0yLjQxODkgMi4xNTY5LTIuNDE4OSAxLjIxMDggMCAyLjE3NTcgMS4wOTUyIDIuMTU2OCAyLjQxOSAwIDEuMzMzMi0uOTQ2IDIuNDE4OS0yLjE1NjggMi40MTg5WiIvPjwvc3ZnPg%3D%3D&logoColor=white
[discord]: https://discord.gg/gvmeswCQFP
[modrinth-badge]: https://img.shields.io/badge/Modrinth-Download-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white
[modrinth]: https://modrinth.com/mod/immersive-aircraft-unofficial-backport
[curseforge-badge]: https://img.shields.io/badge/CurseForge-Download-F16436?style=for-the-badge&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyByb2xlPSJpbWciIHZpZXdCb3g9IjAgMCAyNCAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48dGl0bGU%2BQ3Vyc2VGb3JnZTwvdGl0bGU%2BPHBhdGggZmlsbD0id2hpdGUiIGQ9Ik02LjMwNyA1LjU4MWwuMzkxIDEuNjc1SDBzLjExMi41MDIuMTY3LjU1OGMuMTY4LjI3OS4zMzUuNjE0LjU1OS44MzcgMS4wNiAxLjIyOCAyLjkwMiAxLjczIDQuNDA5IDIuMDA5IDEuMDYuMjI0IDIuMTIxLjI4IDMuMTgxLjMzNWwxLjIyOCAzLjI5M2guNjdsLjM5MSAxLjA2MWgtLjU1OGwtLjk0OSAzLjA3aDkuMzIxbC0uOTQ5LTMuMDdoLS41NThsLjM5LTEuMDYxaC42N3MuNTU4LTMuNDA0IDIuMjg4LTQuOTY3QzIxLjkzNSA3Ljc1OCAyNCA3LjUzNSAyNCA3LjUzNVY1LjU4MUg2LjMwN3ptOS4zNzcgOC40MjhjLS40NDcuMjc5LS45NDkuMjc5LTEuMjg0LjUwMy0uMjIzLjExMS0uMzM1LjQ0Ni0uMzM1LjQ0Ni0uMjIzLS41MDItLjUwMi0uNjctLjgzNy0uNzgxLS4zMzUtLjExMi0uOTQ5LS4wNTYtMS43ODYtLjc4Mi0uNTU4LS41MDItLjYxNC0xLjE3Mi0uNTU4LTEuNTA3di0uMTY3YzAtLjA1NiAwLS4xMTIuMDU2LS4xNjguMTExLS4zMzQuMzktLjY2OS45NDgtLjg5MyAwIDAtLjM5LjU1OSAwIDEuMTE3LjIyNC4zMzUuNjcuNTAyIDEuMDYxLjI3OS4xNjctLjExMi4yNzktLjMzNS4zMzUtLjUwMy4xMTEtLjM5LjExMS0uNzgxLS4yMjQtMS4wNi0uNTAyLS40NDYtLjYxMy0xLjA2LS4yNzktMS40NTEgMCAwIC4xMTIuNTAyLjYxNC40NDYuMzM1IDAgLjMzNS0uMTExLjIyNC0uMjIzLS4wNTYtLjE2Ny0uNzgyLTEuMjI4LjI3OS0yLjAwOSAwIDAgLjY2OS0uNDQ3IDEuNDUxLS4zOTEtLjQ0Ny4wNTYtLjk0OS4zMzUtMS4xMTYuNzgydi4wNTVjLS4xNjguNDQ3LS4wNTYuOTQ5LjI3OSAxLjM5Ni4yMjMuMzM1LjUwMi42MTQuNjE0IDEuMDYtLjE2OC0uMDU2LS4yNzkgMC0uMzkxLjExMmEuNTMzLjUzMyAwIDAwLS4xMTIuNTAyYy4wNTYuMTEyLjE2OC4yMjMuMjc5LjIyM2guMTY4Yy4xNjctLjA1NS4yNzktLjI3OS4yMjMtLjQ0Ni4xMTIuMTExLjE2Ny4zOTEuMTEyLjU1OCAwIC4xNjctLjExMi4zMzUtLjE2OC40NDYtLjA1Ni4xMTItLjE2Ny4yMjQtLjIyMy4zMzUtLjA1Ni4xMTItLjExMi4yMjQtLjExMi4zMzUgMCAuMTEyIDAgLjI3OS4wNTYuMzkxLjIyMy4zMzUuNjcgMCAuNzgyLS4yNzkuMTY3LS4zMzUuMTExLS43MjYtLjExMi0xLjA2MSAwIDAgLjM5MS4yMjQuNjcgMS4wMDUuMjIzLjY3LS4xNjggMS40NTEtLjYxNCAxLjczeiIvPjwvc3ZnPg%3D%3D&logoColor=white
[curseforge]: https://www.curseforge.com/minecraft/mc-mods/immersive-aircraft-legacy

Fork of [Luke100000's Immersive Aircraft](https://github.com/Luke100000/ImmersiveAircraft),
dedicated to the **Minecraft 1.12.2, Forge only** backport. This repository
tracks only the `1.12.2` branch; for modern Minecraft versions see the
upstream repository.

The original mod adds bunch of rustic aircraft to travel, transport, and explore! The aircraft have a strong focus on being vanilla-faithful and many details and functionalities, without being overly complicated.

[![Crowdin](https://badges.crowdin.net/immersive-collection/localized.svg)](https://crowdin.com/project/immersive-collection)

The original mod is hosted on
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/immersive-aircraft) and
[Modrinth](https://modrinth.com/mod/immersive-aircraft)

## About the backport

Based on the 1.16.5 codebase, with the 1.20.1 content — Warship, Bamboo
Hopper and the weapon system — ported on top.

Differences from the modern upstream branches:

- Single-module Forge mod, no Fabric/Architectury.
- Configuration via the in-game mod list ("Config" button), powered by Forge's
  `@Config` — there are no datapacks in 1.12.2, so aircraft balance lives in
  `config/immersive_aircraft.cfg`.
- Hooks use Forge events where 1.12.2 has them; the rest (rider sneaking, vehicle
  flight check, rider rendering) are Mixins booted from a coremod (`MixinLoader`).

### Runtime requirements

The released jar does not bundle the Mixin library, so it must be provided at
runtime: install [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter)
(also on [Modrinth](https://modrinth.com/mod/mixinbooter)) alongside this mod.
Without it the `MixinLoader` coremod cannot boot Mixin and the game will not
start. Dev runs (`runClient`/`runServer`) get the library from the Gradle
classpath and do not need it.

### Building

Requirements: **JDK 8** (ForgeGradle 2.3 does not run on newer JDKs).

```bash
export JAVA_HOME=/path/to/jdk8
./gradlew build
```

The reobfuscated (SRG-named) jar lands in `build/libs/`.

Toolchain: [anatawa12's ForgeGradle 2.3 fork](https://github.com/anatawa12/ForgeGradle-2.3)
(the official ForgeGradle does not work for 1.12.2 on modern JDKs),
Forge 1.12.2-14.23.5.2847, MCP `stable_39`, Gradle 4.9.

### Development runs

```bash
./gradlew runClient
./gradlew runServer
```
