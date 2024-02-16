package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.database.DatabaseWrapper;
import com.georgev22.library.database.DatabaseWrapper.DatabaseObject;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.maps.ObjectMap.Pair;
import com.georgev22.library.maps.ObservableObjectMap;
import com.georgev22.library.maps.UnmodifiableObjectMap;
import com.georgev22.library.yaml.ConfigurationSection;
import com.georgev22.library.yaml.file.YamlConfiguration;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.OptionsUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@SuppressWarnings("UnusedReturnValue")
public class PlayerDataManager {

    private final VoteReward voteReward = VoteReward.getInstance();

    private final File entitiesDirectory;
    private final DatabaseWrapper database;
    private final String collection;
    private final ObservableObjectMap<UUID, User> loadedEntities = new ObservableObjectMap<>();

    /**
     * Constructor for the PlayerManager class
     *
     * @param obj            the object to be used for storage (DatabaseWrapper or File)
     * @param collectionName the name of the collection to be used for MONGODB and SQL, null for other types
     */
    public PlayerDataManager(Object obj, @Nullable String collectionName) {
        this.collection = collectionName;
        if (obj instanceof File folder) {
            this.entitiesDirectory = folder;
            this.database = null;
            if (!this.entitiesDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                this.entitiesDirectory.mkdirs();
            }
        } else if (obj instanceof DatabaseWrapper databaseWrapper) {
            this.entitiesDirectory = null;
            this.database = databaseWrapper;
        } else {
            this.entitiesDirectory = null;
            this.database = null;
        }
    }


    public List<User> playersData() {
        return new ArrayList<>(this.getLoadedEntities().values());
    }

    public CompletableFuture<User> load(UUID entityId) {
        return exists(entityId)
                .thenCompose(exists -> {
                    if (exists) {
                        return CompletableFuture.supplyAsync(() -> {
                            if (entitiesDirectory != null) {
                                File file = new File(entitiesDirectory, entityId + ".yml");
                                if (!file.exists()) {
                                    return new User(entityId);
                                }
                                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                                User playerData = new User(entityId);
                                playerData.addCustomData("votes", yamlConfiguration.getInt("votes", 0));
                                playerData.addCustomData("daily", yamlConfiguration.getInt("daily", 0));
                                playerData.addCustomData("last", yamlConfiguration.getLong("last", 0L));
                                playerData.addCustomData("voteparty", yamlConfiguration.getInt("voteparty", 0));
                                playerData.addCustomData("totalvotes", yamlConfiguration.getInt("totalvotes", 0));
                                playerData.addCustomData("services", yamlConfiguration.getStringList("services"));

                                HashObjectMap<String, Long> servicesLastVoteMap = new HashObjectMap<>();
                                ConfigurationSection servicesLastVote = yamlConfiguration.getConfigurationSection("servicesLastVote");

                                if (servicesLastVote != null) {
                                    for (String key : servicesLastVote.getKeys(false)) {
                                        long value = servicesLastVote.getLong("servicesLastVote." + key);
                                        servicesLastVoteMap.put(key, value);
                                    }
                                }
                                playerData.addCustomData("servicesLastVote", servicesLastVoteMap);

                                this.loadedEntities.append(playerData.getId(), playerData);
                                return playerData;
                            } else if (database != null) {
                                Pair<String, List<DatabaseObject>> retrievedData = database.retrieveData(collection, Pair.create("entity_id", entityId.toString()));
                                Optional<User> optionalEntity = retrievedData.value().stream()
                                        .filter(databaseObject -> databaseObject.data().get("data") != null)
                                        .map(databaseObject -> UserTypeAdapter.fromJson((String) databaseObject.data().get("data")))
                                        .findFirst();
                                User entity = optionalEntity.orElseGet(() -> new User(entityId));
                                loadedEntities.append(entityId, entity);
                                return entity;
                            } else {
                                return new User(entityId);
                            }
                        });
                    } else {
                        return createEntity(entityId);
                    }
                }).handle((data, throwable) -> {
                    if (throwable != null) {
                        this.voteReward.getLogger().log(Level.SEVERE, "Error while trying to load " + data.getId() + " player", throwable);
                        return null;
                    }
                    return data;
                }).thenApply(data -> {
                    if (data != null) {
                        if (OptionsUtil.DEBUG_LOAD.getBooleanValue()) {
                            this.voteReward.getLogger().info("Successfully loaded " + data.getId() + " player");
                        }
                        return data;
                    }
                    return null;
                });
    }


    public CompletableFuture<Void> save(User entity) {
        return CompletableFuture.runAsync(() -> {
            if (entitiesDirectory != null) {
                File file = new File(entitiesDirectory, entity.getId() + ".yml");
                try {
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                    yamlConfiguration.set("votes", entity.getCustomData("votes"));
                    yamlConfiguration.set("daily", entity.getCustomData("daily"));
                    yamlConfiguration.set("last", entity.getCustomData("last"));
                    yamlConfiguration.set("voteparty", entity.getCustomData("voteparty"));
                    yamlConfiguration.set("totalvotes", entity.getCustomData("totalvotes"));
                    yamlConfiguration.set("services", entity.getCustomData("services"));
                    Map<String, Long> servicesLastVote = entity.getCustomData("servicesLastVote");
                    for (Map.Entry<String, Long> entry : servicesLastVote.entrySet()) {
                        yamlConfiguration.set("servicesLastVote." + entry.getKey(), entry.getValue());
                    }

                    yamlConfiguration.save(file);
                } catch (IOException e) {
                    this.voteReward.getLogger().log(Level.SEVERE, "Error while trying to save " + entity.getId() + " player", e);
                }
            } else if (database != null) {
                exists(entity.getId()).thenAccept(result -> {
                    ObjectMap<String, Object> entityData = new HashObjectMap<>();
                    entityData.append("entity_id", entity.getId().toString());
                    entityData.append("data", UserTypeAdapter.toJson(entity));
                    if (result) {
                        database.updateData(collection, Pair.create("entity_id", entity.getId().toString()), Pair.create("$set", entityData.removeEntry("entity_id")), null);
                    } else {
                        database.addData(collection, Pair.create(entity.getId().toString(), entityData));
                    }
                });
            }
            this.loadedEntities.append(entity.getId(), entity);
        }).handle((unused, throwable) -> {
            if (throwable != null) {
                this.voteReward.getLogger().log(Level.SEVERE, "Error while trying to save " + entity.getId() + " player", throwable);
                return null;
            }
            return unused;
        }).thenAccept(unused -> {
            if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                this.voteReward.getLogger().info("Successfully saved " + entity.getId() + " player");
            }
        });
    }


    public CompletableFuture<Void> delete(User entity) {
        return CompletableFuture.runAsync(() -> {
            //noinspection DuplicatedCode
            if (entitiesDirectory != null) {
                File file = new File(entitiesDirectory, entity.getId() + ".yml");
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            } else if (database != null) {
                exists(entity.getId()).thenAccept(result -> {
                    if (result) {
                        database.removeData(collection, Pair.create("entity_id", entity.getId().toString()), null);
                        this.voteReward.getLogger().info("Deleted User: " + entity.getId());
                    }
                });
            }
            this.loadedEntities.remove(entity.getId());
        });
    }


    public CompletableFuture<User> createEntity(UUID entityId) {
        return CompletableFuture.completedFuture(this.loadedEntities.append(entityId, new User(entityId)).get(entityId));
    }


    public CompletableFuture<Boolean> exists(UUID entityId) {
        return CompletableFuture.supplyAsync(() -> {
            if (entitiesDirectory != null) {
                return new File(entitiesDirectory, entityId + ".yml").exists();
            } else if (database != null) {
                return database.exists(collection, Pair.create("entity_id", entityId), null);
            } else {
                return false;
            }
        });
    }


    public CompletableFuture<User> getEntity(UUID entityId) {
        if (this.loadedEntities.containsKey(entityId)) {
            return CompletableFuture.completedFuture(this.loadedEntities.get(entityId));
        }

        return load(entityId);
    }


    public void saveAll() {
        ObjectMap<UUID, User> entities = new ObservableObjectMap<UUID, User>().append(loadedEntities);
        entities.forEach((uuid, entity) -> save(entity));
    }


    public void loadAll() {
        //noinspection DuplicatedCode
        List<UUID> entityIDs = new ArrayList<>();
        if (entitiesDirectory != null) {
            File[] files = this.entitiesDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                Arrays.stream(files).forEach(file -> entityIDs.add(UUID.fromString(file.getName().replace(".yml", ""))));
            }
        } else if (database != null) {
            Pair<String, List<DatabaseObject>> data = database.retrieveData(collection, Pair.create("entity_id", null));
            data.value().forEach(databaseObject -> entityIDs.add(UUID.fromString(String.valueOf(databaseObject.data().get("entity_id")))));
        }
        entityIDs.forEach(this::load);
    }


    public UnmodifiableObjectMap<UUID, User> getLoadedEntities() {
        return new UnmodifiableObjectMap<>(this.loadedEntities);
    }


    /**
     * Get the total votes until the next cumulative reward
     *
     * @return Integer total votes until the next cumulative reward
     */
    public int votesUntilNextCumulativeVote(User user) {
        ConfigurationSection configurationSection = this.voteReward.getConfig().getConfigurationSection("Rewards.Cumulative");
        if (configurationSection == null) {
            return 0;
        }
        int votesUntil = 0;
        for (String b : configurationSection.getKeys(false)) {
            int cumulative = Integer.parseInt(b);
            if (cumulative <= user.votes()) {
                continue;
            }
            votesUntil = cumulative - user.votes();
            break;
        }
        return votesUntil;
    }
}
