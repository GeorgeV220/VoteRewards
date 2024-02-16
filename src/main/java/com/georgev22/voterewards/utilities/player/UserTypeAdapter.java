package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.voterewards.VoteReward;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserTypeAdapter implements JsonSerializer<User>, JsonDeserializer<User> {

    /**
     * Converts a JSON string to a {@link User} object.
     *
     * @param json The JSON string representing the serialized {@link User} object.
     * @return A {@link User} object deserialized from the provided JSON string.
     * @throws IllegalArgumentException If the provided JSON string is null or empty.
     */
    public static User fromJson(String json) {
        if (json == null) throw new IllegalArgumentException("Json cannot be null");
        if (json.isEmpty()) throw new IllegalArgumentException("Json cannot be empty");
        return VoteReward.getInstance().getGson().fromJson(json, User.class);
    }

    /**
     * Converts a {@link User} object to its JSON representation.
     *
     * @param User The {@link User} object to be converted.
     * @return A JSON string representing the serialized {@link User} object.
     * @throws IllegalArgumentException If the provided {@link User} object is null.
     */
    public static String toJson(User User) {
        if (User == null) throw new IllegalArgumentException("Part cannot be null");
        return VoteReward.getInstance().getGson().toJson(User);
    }

    @Override
    public User deserialize(@NotNull JsonElement jsonElement, Type type, @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        UUID uuid = UUID.fromString(jsonObject.get("playerUUID").getAsString());
        User user = new User(uuid);
        user.addCustomData("votes", jsonObject.getAsJsonPrimitive("votes").getAsInt());
        user.addCustomData("daily", jsonObject.getAsJsonPrimitive("daily").getAsInt());
        user.addCustomData("last", jsonObject.getAsJsonPrimitive("last").getAsLong());
        user.addCustomData("voteparty", jsonObject.getAsJsonPrimitive("voteparty").getAsInt());
        user.addCustomData("totalvotes", jsonObject.getAsJsonPrimitive("totalvotes").getAsInt());
        JsonArray services = jsonObject.getAsJsonArray("services");
        List<String> servicesList = new ArrayList<>();
        for (JsonElement service : services) {
            servicesList.add(service.getAsJsonPrimitive().getAsString());
        }
        user.addCustomData("services", servicesList);
        HashObjectMap<String, Long> servicesLastVote = new HashObjectMap<>();
        for (Map.Entry<String, JsonElement> service : jsonObject.getAsJsonObject("servicesLastVote").entrySet()) {
            servicesLastVote.put(service.getKey(), service.getValue().getAsLong());
        }
        user.addCustomData("servicesLastVote", servicesLastVote);
        return user;
    }

    @Override
    public JsonElement serialize(@NotNull User user, Type type, @NotNull JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("playerUUID", user.getId().toString());
        jsonObject.addProperty("votes", user.votes());
        jsonObject.addProperty("daily", user.dailyVotes());
        jsonObject.addProperty("last", user.lastVote());
        jsonObject.addProperty("voteparty", user.voteparty());
        jsonObject.addProperty("totalvotes", user.totalVotes());
        JsonArray services = new JsonArray();
        for (String service : user.services()) {
            services.add(new JsonPrimitive(service));
        }
        jsonObject.add("services", services);
        JsonObject servicesLastVote = new JsonObject();
        for (Map.Entry<String, Long> service : user.servicesLastVote().entrySet()) {
            servicesLastVote.addProperty(service.getKey(), service.getValue());
        }
        jsonObject.add("servicesLastVote", servicesLastVote);

        return jsonObject;
    }
}
