package net.smoothplugins.smoothsync.module;

import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import net.smoothplugins.smoothbase.serializer.Serializer;
import net.smoothplugins.smoothsync.serializer.adapter.AdvancementCollectionAdapter;
import org.bukkit.advancement.Advancement;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;

public class SerializerModule extends AbstractModule {

    @Override
    protected void configure() {
        HashMap<Type, Object> additionalAdapters = new HashMap<>();
        additionalAdapters.put(new TypeToken<HashMap<Advancement, Collection<String>>>(){}.getType(), new AdvancementCollectionAdapter());
        bind(Serializer.class).toInstance(new Serializer(additionalAdapters));
    }
}
