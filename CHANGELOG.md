# [10.1.0](https://github.com/GeorgeV220/VoteRewards/compare/v10.0.0...v10.1.0) (2025-07-07)


### Bug Fixes

* adjust debug logging conditions and add null safety ([f3d37ee](https://github.com/GeorgeV220/VoteRewards/commit/f3d37eecb36bc75f7e98e45dcef4190953d16845))


### Features

* Rework commands ([ee9c6d1](https://github.com/GeorgeV220/VoteRewards/commit/ee9c6d1184847289e9eb7e55b2c86624a42ff0d0))

# [10.0.0](https://github.com/GeorgeV220/VoteRewards/compare/v9.1.1...v10.0.0) (2024-02-16)


### Features

* add animation option to paged inventories ([98b88a1](https://github.com/GeorgeV220/VoteRewards/commit/98b88a138afa4bee092e9515987464e36d0ee855))
* add Gson and UserTypeAdapter for User serialization (main) ([184f485](https://github.com/GeorgeV220/VoteRewards/commit/184f485f7cc8d1ca74b989889a6ad2f333cefc75))
* add gson dependency and relocate it ([c46f6c0](https://github.com/GeorgeV220/VoteRewards/commit/c46f6c067b6743fc115291a0d70cbddb12008116))
* add UserTypeAdapter class for User serialization and deserialization ([811bdd0](https://github.com/GeorgeV220/VoteRewards/commit/811bdd02d14383144d585ccb78919518c4ab7291))
* Refactor the entire database system to use GSON instead, ([7a499d0](https://github.com/GeorgeV220/VoteRewards/commit/7a499d01628432ec434eb230d85cc00042a81945))
* **VoteRewardPlugin:** add getter for voteRewardInstance ([1224a22](https://github.com/GeorgeV220/VoteRewards/commit/1224a22602a2a10bb28152627724098c4dbdfda2))


### BREAKING CHANGES

* Database schema has been changed

## [9.1.1](https://github.com/GeorgeV220/VoteRewards/compare/v9.1.0...v9.1.1) (2023-11-14)


### Bug Fixes

* Fixed commands in 1.20.2 ([0afb4cd](https://github.com/GeorgeV220/VoteRewards/commit/0afb4cd153adbf662f5f62bc665b3e860b5084b1))

# [9.1.0](https://github.com/GeorgeV220/VoteRewards/compare/v9.0.0...v9.1.0) (2023-10-27)


### Features

* Add support for VotifierPlus ([267decc](https://github.com/GeorgeV220/VoteRewards/commit/267deccd6feb8f5838e762be29fa83c4d4f5a687)), closes [#131](https://github.com/GeorgeV220/VoteRewards/issues/131)

# [9.0.0](https://github.com/GeorgeV220/VoteRewards/compare/v8.3.0...v9.0.0) (2023-07-04)


### Bug Fixes

* Fix issue with sending message to commandIssuer ([820ea17](https://github.com/GeorgeV220/VoteRewards/commit/820ea17480b4bdaa6d2fee384d8dae9473a55430))
* Handle exception and log warning message when failed to check for an update ([14894b8](https://github.com/GeorgeV220/VoteRewards/commit/14894b817b21b42c6c07fbb0d8ed60750a05730b))
* **player:** fix method calls and record field access ([98416cd](https://github.com/GeorgeV220/VoteRewards/commit/98416cd9ca55d5b704580a8314c895d3eca206e9))


### Features

* 1.20 support. ([ef01f4f](https://github.com/GeorgeV220/VoteRewards/commit/ef01f4fcd2f2574ad23145ef3ffd662219207ae9))
* add plugin.yml ([1f3289e](https://github.com/GeorgeV220/VoteRewards/commit/1f3289e938c87b74ff8b514a593f8c2b9d6f111e))
* **commands:** Update Commands to use new User class and changed CommandExecutor to CommandIssuer ([2d53b60](https://github.com/GeorgeV220/VoteRewards/commit/2d53b60000843e3d6087a0715b1148809b5b90b8))
* **player:** Add PlayerDataManager class and User class modifications ([3abcbcc](https://github.com/GeorgeV220/VoteRewards/commit/3abcbcc23baa7463e157442a3be4bccfaabae2e0))
* Refactor AuthMe and PAPI hooks to use VoteReward instance and PlayerDataManager ([d34410e](https://github.com/GeorgeV220/VoteRewards/commit/d34410ea1421b6a2251890be3ca28be930fe4307))
* Refactor VoteReward class and improve database handling ([b958149](https://github.com/GeorgeV220/VoteRewards/commit/b958149c9d9b8f53e5195cb902494d9785561647))
* Remove unused IDatabaseType interface and UserVoteData class ([ea3ed58](https://github.com/GeorgeV220/VoteRewards/commit/ea3ed5810eb9f3b80bfb230f7b7663d7c82feddd))
* Remove VoteRewardExtension, extension.yml, and VoteRewardTest ([bfa10bd](https://github.com/GeorgeV220/VoteRewards/commit/bfa10bd6a80509d173e7b1811d56cb5eca9f4fc8))


### BREAKING CHANGES

* Numerous internal changes have been made. Player data
from version 8.x is no longer supported. To proceed, it is necessary to
create a backup using the command "voterewards backup", remove the old
data, install the new version, and finally perform a restoration using
the command "voterewards restore <restore file name>".

# [8.3.0](https://github.com/GeorgeV220/VoteRewards/compare/v8.2.2...v8.3.0) (2023-05-11)


### Bug Fixes

* Build tests ([be41aae](https://github.com/GeorgeV220/VoteRewards/commit/be41aae4dfc064b94acaa5c52ccc9f0adaf750c9))
* gradle.yml setup java nodejs version ([c1211e6](https://github.com/GeorgeV220/VoteRewards/commit/c1211e68fd489dae0754b0ad9eed6bf291578fee))
* Make OptionsUtil.getOldPaths unmodifiable ([556593f](https://github.com/GeorgeV220/VoteRewards/commit/556593fafe41fd5d621afb3dd5790e17fb727bf9))
* Player name ([e433ccb](https://github.com/GeorgeV220/VoteRewards/commit/e433ccb8ba7c0d469d405e8383669fceb11ab692))


### Features

* PlaceholderAPI support to the reward system. ([#126](https://github.com/GeorgeV220/VoteRewards/issues/126)) ([5f766e3](https://github.com/GeorgeV220/VoteRewards/commit/5f766e363fd42b8ebf7550a4fa509161bf391b0a))

## [8.2.2](https://github.com/GeorgeV220/VoteRewards/compare/v8.2.1...v8.2.2) (2022-12-27)


### Bug Fixes

* update-versions.sh ([f60699a](https://github.com/GeorgeV220/VoteRewards/commit/f60699aeab7cc30caf71d58b24f67b9e573438f2))

## [8.2.1](https://github.com/GeorgeV220/VoteRewards/compare/v8.2.0...v8.2.1) (2022-12-14)


### Bug Fixes

* **Updater:** Fixed updater message. ([006303f](https://github.com/GeorgeV220/VoteRewards/commit/006303fd9267406e83750a43fc1fdb2c7eb44894))

# [8.2.0](https://github.com/GeorgeV220/VoteRewards/compare/v8.1.0...v8.2.0) (2022-12-12)


### Bug Fixes

* **Metrics:** Added config value for metrics ([74f2615](https://github.com/GeorgeV220/VoteRewards/commit/74f2615578f708cab6d74737a9f4a58f9c61325f))


### Features

* **1_19_R2:** Added 1.19.3(v1_19_R2) support ([92497f3](https://github.com/GeorgeV220/VoteRewards/commit/92497f38bbc3c7cf5e781f806fcecefac182b8dc))

# [8.1.0](https://github.com/GeorgeV220/VoteRewards/compare/v8.0.2...v8.1.0) (2022-11-28)


### Bug Fixes

* **gradle.properties:** Changed API version from 1.18 to 1.13 ([fa2a725](https://github.com/GeorgeV220/VoteRewards/commit/fa2a725e3a37ea200e21af34a63e4a4fad37b725))


### Features

* **VoteReward:** Change package. ([320e266](https://github.com/GeorgeV220/VoteRewards/commit/320e26691b37d46a77119a7d6e6014cc987b48b7))

## [8.0.2](https://github.com/GeorgeV220/VoteRewards/compare/v8.0.1...v8.0.2) (2022-11-28)


### Bug Fixes

* **Updater:** Changed updater player messages to be more specific which plugin is sending the message. ([e43d590](https://github.com/GeorgeV220/VoteRewards/commit/e43d5906820538e52a77ac3581eece4933a5a6e9))

## [8.0.1](https://github.com/GeorgeV220/VoteRewards/compare/v8.0.0...v8.0.1) (2022-11-25)


### Bug Fixes

* **VoteRewardPlugin:** Fixed an issue that caused voterewards to not load ([14ec9b8](https://github.com/GeorgeV220/VoteRewards/commit/14ec9b839ebba1f7f661e8eb9b6e654a33033448))

# [8.0.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.13.5...v8.0.0) (2022-11-23)


### Features

* **Extension:** VoteRewards can now load as an extension ([512962a](https://github.com/GeorgeV220/VoteRewards/commit/512962aaf12f85193dd006635b7ba690b6a8c53b))


### BREAKING CHANGES

* **Extension:** A lot of API changes

## [7.13.5](https://github.com/GeorgeV220/VoteRewards/compare/v7.13.4...v7.13.5) (2022-11-23)


### Bug Fixes

* **VoteParty:** VoteParty rewards ([519f347](https://github.com/GeorgeV220/VoteRewards/commit/519f3471f821db412b53805d49ce5c243cb24836))

## [7.13.4](https://github.com/GeorgeV220/VoteRewards/compare/v7.13.3...v7.13.4) (2022-10-28)


### Bug Fixes

* **Updater:** Updater URL ([d2b659e](https://github.com/GeorgeV220/VoteRewards/commit/d2b659efff4d6255ddb633a71d2598208fd1222b))

## [7.13.3](https://github.com/GeorgeV220/VoteRewards/compare/v7.13.2...v7.13.3) (2022-10-28)


### Bug Fixes

* **Updater:** Updater URL ([435ad37](https://github.com/GeorgeV220/VoteRewards/commit/435ad37db76d68dc671ce1ccf1c243ae8d87c268))

## [7.13.2](https://github.com/GeorgeV220/VoteRewards/compare/v7.13.1...v7.13.2) (2022-10-28)


### Bug Fixes

* **HologramCommand:** Arguments ([abc9940](https://github.com/GeorgeV220/VoteRewards/commit/abc9940e1c97b5a09e4c6e7112ea289537e8cd39))

## [7.13.1](https://github.com/GeorgeV220/VoteRewards/compare/v7.13.0...v7.13.1) (2022-10-28)


### Bug Fixes

* **HologramCommand:** Arguments ([0023d58](https://github.com/GeorgeV220/VoteRewards/commit/0023d58733054ad83f549e59a2df77daa2d8cdad))

# [7.13.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.12.3...v7.13.0) (2022-10-28)


### Features

* **Holograms:** HolographicDisplays hook ([bea31a2](https://github.com/GeorgeV220/VoteRewards/commit/bea31a2c5c98ba3f8121b1c3e6c1bb8259e46f27))

## [7.12.3](https://github.com/GeorgeV220/VoteRewards/compare/v7.12.2...v7.12.3) (2022-10-26)


### Bug Fixes

* **VoteRewardPlugin:** Removed deprecated annotation ([d999b84](https://github.com/GeorgeV220/VoteRewards/commit/d999b84b54f2ef465b21c53ce49599146d583a7a))

## [7.12.2](https://github.com/GeorgeV220/VoteRewards/compare/v7.12.1...v7.12.2) (2022-10-26)


### Bug Fixes

* **build.gradle:** Change to GeorgeV22 repo for API ([7174319](https://github.com/GeorgeV220/VoteRewards/commit/717431999721efea6f50156ef6a24be4d2e066b8))

## [7.12.1](https://github.com/GeorgeV220/VoteRewards/compare/v7.12.0...v7.12.1) (2022-10-26)


### Bug Fixes

* **publish:** Publish source and javadocs ([61a44e2](https://github.com/GeorgeV220/VoteRewards/commit/61a44e2b6a477bd7f68b70b6dd0b6855fc9216ba))

# [7.12.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.11.0...v7.12.0) (2022-10-26)


### Features

* **publish:** Publish to GeorgeV22 repository ([1d52e04](https://github.com/GeorgeV220/VoteRewards/commit/1d52e047dbf518c9d4de034894d81b5622bb288f))

# [7.11.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.10.0...v7.11.0) (2022-09-27)


### Features

* **Updater:** Auto updater ([65bacba](https://github.com/GeorgeV220/VoteRewards/commit/65bacba459c88fafad1501cb447ef47d9eced69c))

# [7.10.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.9.0...v7.10.0) (2022-08-20)


### Features

* **Monthly Rewards:** Added monthly rewards ([18f5d85](https://github.com/GeorgeV220/VoteRewards/commit/18f5d85a9f8aca0a918e36e68b38179a77542e59))

# [7.9.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.8.0...v7.9.0) (2022-07-10)


### Bug Fixes

* **Lang:** Fix lang for the NPC command. ([9fc70f9](https://github.com/GeorgeV220/VoteRewards/commit/9fc70f93619c710e62fd0f963a3dfb272d1dcf28))


### Features

* **Updater:** Updater is does no longer need the version.md file ([9e5ae7f](https://github.com/GeorgeV220/VoteRewards/commit/9e5ae7f233c680656727650ea1586692efce7ac1))

# [7.8.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.7.1...v7.8.0) (2022-07-10)


### Bug Fixes

* **NPCAPI:** Fixed an error with NCPAPI when the plugin disables. ([dbd4fba](https://github.com/GeorgeV220/VoteRewards/commit/dbd4fbafb293463e350f230d7ad9121047b95b6a))


### Features

* **WorldGuardWrapper:** Added WorldEdit/WorldGuard support agaian ([8651b57](https://github.com/GeorgeV220/VoteRewards/commit/8651b575b61aa69c923a5127b4616d03dd289b82))

## [7.7.1](https://github.com/Project-Alterra/VoteRewards/compare/v7.7.0...v7.7.1) (2022-07-02)


### Bug Fixes

* **VoteRewardPlugin:** Fixed MySQL and PostgreSQL instances ([5958037](https://github.com/Project-Alterra/VoteRewards/commit/5958037931d7ca392fdd79d129ac7edbc23502f1))

# [7.7.0](https://github.com/Project-Alterra/VoteRewards/compare/v7.6.0...v7.7.0) (2022-07-01)


### Bug Fixes

* **Server.xml:** change WORKING_DIRECTORY back to $PROJECT_DIR$ ([24a4ec7](https://github.com/Project-Alterra/VoteRewards/commit/24a4ec7cad7fb79a905184dcc14e79f18baca91f))


### Features

* **1.19:** Added 1.19 support ([6f0c237](https://github.com/Project-Alterra/VoteRewards/commit/6f0c2370bc5640a9d6cbe21d7b4c1b365e115009))

# [7.6.0](https://github.com/Project-Alterra/VoteRewards/compare/v7.5.0...v7.6.0) (2022-06-11)


### Bug Fixes

* **build:** Fixed build errors and some refactoring ([2a190ac](https://github.com/Project-Alterra/VoteRewards/commit/2a190ac3efe5acce8ba42648989dad09bea04c43))


### Features

* **DatabaseWrapper:** Added usage of API DatabaseWrapper ([6668813](https://github.com/Project-Alterra/VoteRewards/commit/666881392ddd45e06f52664faabffda94ab96043))

# [7.5.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.4.2...v7.5.0) (2022-04-10)


### Bug Fixes

* XSeries imports ([23bd440](https://github.com/GeorgeV220/VoteRewards/commit/23bd44071072d9931ee84cf31ac89e2a043b4754))


### Features

* Extensions ([021c66b](https://github.com/GeorgeV220/VoteRewards/commit/021c66b8bf0dbba5af1a6b34a0240344d2af5131))

## [7.4.2](https://github.com/GeorgeV220/VoteRewards/compare/v7.4.1...v7.4.2) (2022-04-04)


### Bug Fixes

* Skins on offline mode ([ae64ce0](https://github.com/GeorgeV220/VoteRewards/commit/ae64ce062819428352bf1d4d68c4fd5c353b9005))

## [7.4.1](https://github.com/GeorgeV220/VoteRewards/compare/v7.4.0...v7.4.1) (2022-04-04)


### Bug Fixes

* Add check if the hologram is hooked ([a00d71d](https://github.com/GeorgeV220/VoteRewards/commit/a00d71d04d980028479216e409e92dc99ef08700))

# [7.4.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.3.2...v7.4.0) (2022-04-03)


### Bug Fixes

* Optimize NPCAPI.java ([b9c2aff](https://github.com/GeorgeV220/VoteRewards/commit/b9c2afff67a259d2a70b72cae7149cae324a1d5c))


### Features

* **NPCCommand:** Fully working NPC command ([9f944e2](https://github.com/GeorgeV220/VoteRewards/commit/9f944e268d2306187db1e3cec27e5da7c96f2d7a))

## [7.3.2](https://github.com/GeorgeV220/VoteRewards/compare/v7.3.1...v7.3.2) (2022-04-02)


### Bug Fixes

* Null check to DeveloperInformListener ([a05ba19](https://github.com/GeorgeV220/VoteRewards/commit/a05ba198ecec05b323a253c98ab359f9abd31a24))

## [7.3.1](https://github.com/GeorgeV220/VoteRewards/compare/v7.3.0...v7.3.1) (2022-03-20)


### Bug Fixes

* Fixed unknown version on 1.18.2 ([f750b96](https://github.com/GeorgeV220/VoteRewards/commit/f750b9691496226ea7250c5026590095aaaebd4e))
* Holograms ([1c76fd1](https://github.com/GeorgeV220/VoteRewards/commit/1c76fd1c72e172d794305c33b2cf0e06b18f0b63))

# [7.3.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.2.1...v7.3.0) (2022-03-20)


### Features

* Holograms changes and build fix ([942afe7](https://github.com/GeorgeV220/VoteRewards/commit/942afe714703fbd3ae1b9acdc10c85e561b363b6))

## [7.2.1](https://github.com/GeorgeV220/VoteRewards/compare/v7.2.0...v7.2.1) (2022-03-14)


### Bug Fixes

* **Server.xml:** working directory path fixed ([8394a7e](https://github.com/GeorgeV220/VoteRewards/commit/8394a7e88d9b1d7bf635e48da62130cc0614baec))

# [7.2.0](https://github.com/GeorgeV220/VoteRewards/compare/v7.1.0...v7.2.0) (2022-03-14)


### Features

* **gradle.yml:** shadowJar ([7d8b8e2](https://github.com/GeorgeV220/VoteRewards/commit/7d8b8e217fbb82d5e896ce381b04289e36a797d6))

# [7.1.0](https://github.com/GeorgeV220/VoteRewards6/compare/v7.0.0...v7.1.0) (2022-03-13)


### Bug Fixes

* OptionsUtil ([2a35ae4](https://github.com/GeorgeV220/VoteRewards6/commit/2a35ae462db2f88bdc3093ec7742ceed6016d249))


### Features

* aikar's Commands ([d56b684](https://github.com/GeorgeV220/VoteRewards6/commit/d56b684e0bd1f76198454b6bfa207fcba36f2a9a))
* Command ([fe53b82](https://github.com/GeorgeV220/VoteRewards6/commit/fe53b8255c7bfb439e50ed9b4c83a42fef8a3fdf))

# [7.0.0](https://github.com/GeorgeV220/VoteRewards6/compare/v6.0.0...v7.0.0) (2022-03-13)


* chore!: JavaDocs and Sources ([54a0a30](https://github.com/GeorgeV220/VoteRewards6/commit/54a0a30fa15884e74e30e0fc5747b8888417daef))


### BREAKING CHANGES

* Now gradle will Generate JavaDocs and Sources

# 1.0.0 (2022-03-13)


### Build System

* **VoteRewards:** Add plugin files and corrected build instructions (build.gradle) ([bffdc79](https://github.com/GeorgeV220/VoteRewards6/commit/bffdc79444c7a094a42c63e999910e95ebe25985))


### BREAKING CHANGES

* **VoteRewards:** switched to Java 17 and Paper 1.18.2
