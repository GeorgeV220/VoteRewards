# VoteRewards
[![](https://img.shields.io/github/v/release/GeorgeV220/VoteRewards?label=LATEST%20VERSION&style=for-the-badge)](https://github.com/GeorgeV220/VoteRewards/releases/latest)
[![](https://img.shields.io/github/downloads/GeorgeV220/VoteRewards/total?style=for-the-badge)](https://github.com/GeorgeV220/VoteRewards/releases)
[![](https://img.shields.io/github/actions/workflow/status/GeorgeV220/VoteRewards/gradle.yml?style=for-the-badge&color=65C0A3)](https://github.com/GeorgeV220/VoteRewards/actions)

Vote Rewards is a simple plugin to reward your players when they vote for your server. Vote Rewards contains many
features like daily vote rewards and voteparty. I promise that the Vote Rewards will not cause problems to your server
and if you have a bug you can report it to the Discussion section, send me a private message or open an issue here

I uploaded the source code so that everyone can make their own modifications

You can use the source code to do whatever you want but do not upload sell it or upload it without my permission (except
github)

[![JetBrains](https://www.jetbrains.com/company/brand/img/jetbrains_logo.png)](https://jb.gg/OpenSourceSupport)

JetBrains supports this open source project by providing their tools and resources.

Visit [JetBrains](https://jb.gg/OpenSourceSupport) for more information.

# Adding VoteRewards as a dependency to your build system

### Maven

You can have your project depend on VoteRewards as a dependency through the following code snippets:

```xml

<project>
    <repositories>
        <repository>
            <id>reposilite-repository</id>
            <name>GeorgeV22 Repository</name>
            <url>https://repo.georgev22.com/releases</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.georgev22</groupId>
            <artifactId>voterewards</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

### Gradle

You can include VoteRewards into your gradle project using the following lines:

```groovy
repositories {
    maven {
        url "https://repo.georgev22.com/releases"
    }
}

dependencies {
    compileOnly "com.github.GeorgeV220:voterewards:10.0.0"
}
```

# Building VoteRewards

### Maven
VoteRewards can be built by running the following: `mvn package`. The resultant jar is built and written
to `target/voterewards-{version}.jar`.

The build directories can be cleaned instead using the `mvn clean` command.

If you want to clean (install) and build the plugin use `mvn clean package` (or `mvn clean install package`) command.

### Gradle
VoteRewards can be built by running the following: `gradle clean build shadowJar`. The resultant jar is built and written
to `build/libs/voterewards-{version}.jar`.

The build directories can be cleaned instead using the `gradle clean` command.

If you want to clean (install) and build the plugin use `gradle clean build shadowJar publishShadowPublicationToMavenLocal` command.


# Contributing

VoteRewards is an open source `GNU General Public License v3.0` licensed project. I accept contributions through pull
requests, and will make sure to credit you for your awesome contribution.
