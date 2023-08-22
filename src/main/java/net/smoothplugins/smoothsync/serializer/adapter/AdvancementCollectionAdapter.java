package net.smoothplugins.smoothsync.serializer.adapter;

import com.google.gson.*;
import org.bukkit.advancement.Advancement;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;

public class AdvancementCollectionAdapter implements JsonSerializer<HashMap<Advancement, Collection<String>>>, JsonDeserializer<HashMap<Advancement, Collection<String>>> {

    @Override
    public JsonElement serialize(HashMap<Advancement, Collection<String>> map, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        for (Advancement advancement : map.keySet()) {
            JsonArray array = new JsonArray();
            for (String criteria : map.get(advancement)) {
                array.add(criteria);
            }

            object.add(context.serialize(advancement).getAsString(), array);
        }

        return object;
    }

    @Override
    public HashMap<Advancement, Collection<String>> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        HashMap<Advancement, Collection<String>> map = new HashMap<>();
        object.entrySet().forEach(entry -> {
            JsonArray array = entry.getValue().getAsJsonArray();
            map.put(context.deserialize(new JsonPrimitive(entry.getKey()), Advancement.class), context.deserialize(array, Collection.class));
        });

        return map;
    }
}
