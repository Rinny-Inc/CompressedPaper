package net.minecraft.server;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import net.minecraft.util.com.google.gson.JsonDeserializationContext;
import net.minecraft.util.com.google.gson.JsonDeserializer;
import net.minecraft.util.com.google.gson.JsonElement;
import net.minecraft.util.com.google.gson.JsonObject;
import net.minecraft.util.com.google.gson.JsonSerializationContext;
import net.minecraft.util.com.google.gson.JsonSerializer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

public class BanEntrySerializer implements JsonDeserializer<UserCacheEntry>, JsonSerializer<UserCacheEntry> {
    private final UserCache userCache;

    public BanEntrySerializer(UserCache usercache) {
        this.userCache = usercache;
    }
    
    @Override
    public JsonElement serialize(UserCacheEntry userCacheEntry, Type type, JsonSerializationContext jsonserializationcontext) {
    	JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", userCacheEntry.a().getName());
        UUID uuid = userCacheEntry.a().getId();
        jsonObject.addProperty("uuid", uuid == null ? "" : uuid.toString());
        jsonObject.addProperty("expiresOn", UserCache.a.format(userCacheEntry.b()));

        return jsonObject;
    }

    @Override
    public UserCacheEntry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsondeserializationcontext) {
    	if (!jsonElement.isJsonObject()) {
            return null;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement nameElement = jsonObject.get("name");
        JsonElement uuidElement = jsonObject.get("uuid");
        JsonElement expiresOnElement = jsonObject.get("expiresOn");

        if (nameElement == null || uuidElement == null) {
            return null;
        }

        String name = nameElement.getAsString();
        String uuidString = uuidElement.getAsString();

        if (name.isEmpty() || uuidString.isEmpty()) {
            return null;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }

        Date expirationDate = null;
        if (expiresOnElement != null) {
            try {
                expirationDate = UserCache.a.parse(expiresOnElement.getAsString());
            } catch (ParseException ignored) {}
        }

        return new UserCacheEntry(userCache, new GameProfile(uuid, name), expirationDate, null);
    }
}
