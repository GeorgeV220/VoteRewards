/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.georgev22.voterewards.utilities.maven;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Resolves {@link MavenLibrary} annotations for a class, and loads the dependency
 * into the classloader.
 */
@NotNull
public final class LibraryLoader {

    private final Method ADD_URL_METHOD;
    private final Plugin plugin;

    {
        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public LibraryLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Resolves all {@link MavenLibrary} annotations on the given object.
     *
     * @param object the object to load libraries for.
     */
    public void loadAll(Object object) {
        loadAll(object.getClass());
    }

    /**
     * Resolves all {@link MavenLibrary} annotations on the given class.
     *
     * @param clazz the class to load libraries for.
     */
    public void loadAll(Class<?> clazz) {
        MavenLibrary[] libs = clazz.getDeclaredAnnotationsByType(MavenLibrary.class);
        if (libs == null) {
            return;
        }

        for (MavenLibrary lib : libs) {
            load(lib.groupId(), lib.artifactId(), lib.version(), lib.repo().url());
        }
    }

    public void load(String groupId, String artifactId, String version) {
        load(groupId, artifactId, version, "https://repo1.maven.org/maven2");
    }

    public void load(String groupId, String artifactId, String version, String repoUrl) {
        load(new Dependency(groupId, artifactId, version, repoUrl));
    }

    public void load(Dependency d) {
        Bukkit.getLogger().info(String.format("[" + plugin.getName() + "] Loading dependency %s:%s:%s from %s", d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getRepoUrl()));
        String name = d.getArtifactId() + "-" + d.getVersion();

        File saveLocation = new File(getLibFolder(), name + ".jar");
        if (!saveLocation.exists()) {

            try {
                Bukkit.getLogger().info("[" + plugin.getName() + "] Dependency '" + name + "' is not already in the libraries folder. Attempting to download...");
                URL url = d.getUrl();

                try (InputStream is = url.openStream()) {
                    Files.copy(is, saveLocation.toPath());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Bukkit.getLogger().info("[" + plugin.getName() + "] Dependency '" + name + "' successfully downloaded.");
        }

        if (!saveLocation.exists()) {
            throw new RuntimeException("[" + plugin.getName() + "] Unable to download dependency: " + d.toString());
        }

        URLClassLoader classLoader = (URLClassLoader) plugin.getClass().getClassLoader();
        try {
            ADD_URL_METHOD.invoke(classLoader, saveLocation.toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException("[" + plugin.getName() + "] Unable to load dependency: " + saveLocation.toString(), e);
        }

        Bukkit.getLogger().info("[" + plugin.getName() + "] Loaded dependency '" + name + "' successfully.");
    }

    private File getLibFolder() {
        File pluginDataFolder = plugin.getDataFolder();

        File libs = new File(pluginDataFolder, "libraries");
        if (libs.mkdirs()) {
            Bukkit.getLogger().info("[" + plugin.getName() + "] libraries folder created!");
        }
        return libs;
    }

    @NotNull
    private static final class Dependency {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String repoUrl;

        public Dependency(String groupId, String artifactId, String version, String repoUrl) {
            this.groupId = Objects.requireNonNull(groupId, "groupId");
            this.artifactId = Objects.requireNonNull(artifactId, "artifactId");
            this.version = Objects.requireNonNull(version, "version");
            this.repoUrl = Objects.requireNonNull(repoUrl, "repoUrl");
        }

        public String getGroupId() {
            return this.groupId;
        }

        public String getArtifactId() {
            return this.artifactId;
        }

        public String getVersion() {
            return this.version;
        }

        public String getRepoUrl() {
            return this.repoUrl;
        }

        public URL getUrl() throws MalformedURLException {
            String repo = this.repoUrl;
            if (!repo.endsWith("/")) {
                repo += "/";
            }
            repo += "%s/%s/%s/%s-%s.jar";

            String url = String.format(repo, this.groupId.replace(".", "/"), this.artifactId, this.version, this.artifactId, this.version);
            return new URL(url);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Dependency)) return false;
            final Dependency other = (Dependency) o;
            return this.getGroupId().equals(other.getGroupId()) &&
                    this.getArtifactId().equals(other.getArtifactId()) &&
                    this.getVersion().equals(other.getVersion()) &&
                    this.getRepoUrl().equals(other.getRepoUrl());
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getGroupId().hashCode();
            result = result * PRIME + this.getArtifactId().hashCode();
            result = result * PRIME + this.getVersion().hashCode();
            result = result * PRIME + this.getRepoUrl().hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "LibraryLoader.Dependency(" +
                    "groupId=" + this.getGroupId() + ", " +
                    "artifactId=" + this.getArtifactId() + ", " +
                    "version=" + this.getVersion() + ", " +
                    "repoUrl=" + this.getRepoUrl() + ")";
        }
    }


}