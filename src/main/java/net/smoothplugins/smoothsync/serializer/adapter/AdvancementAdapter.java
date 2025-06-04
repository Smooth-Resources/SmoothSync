package net.smoothplugins.smoothsync.serializer.adapter;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.lang.reflect.Type;

public class AdvancementAdapter implements JsonSerializer<Advancement>, JsonDeserializer<Advancement> {

    @Override
    public Advancement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Bukkit.getAdvancement(NamespacedKey.minecraft(jsonElement.getAsString()));
    }

    @Override
    public JsonElement serialize(Advancement advancement, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(advancement.getKey().getKey());
    }
}
