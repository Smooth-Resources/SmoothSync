package net.smoothplugins.smoothsync.serializer.adapter;

import com.google.gson.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;

public class PotionEffectAdapter implements JsonSerializer<PotionEffect>, JsonDeserializer<PotionEffect> {

    @Override
    public PotionEffect deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PotionEffect(
                PotionEffectType.getByName(jsonObject.get("type").getAsString()),
                jsonObject.get("duration").getAsInt(),
                jsonObject.get("amplifier").getAsInt(),
                jsonObject.get("ambient").getAsBoolean(),
                jsonObject.get("particles").getAsBoolean(),
                jsonObject.get("icon").getAsBoolean()
        );
    }

    @Override
    public JsonElement serialize(PotionEffect potionEffect, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", potionEffect.getType().getName());
        jsonObject.addProperty("duration", potionEffect.getDuration());
        jsonObject.addProperty("amplifier", potionEffect.getAmplifier());
        jsonObject.addProperty("ambient", potionEffect.isAmbient());
        jsonObject.addProperty("particles", potionEffect.hasParticles());
        jsonObject.addProperty("icon", potionEffect.hasIcon());
        return jsonObject;
    }
}
