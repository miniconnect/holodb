package hu.webarticum.holodb.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.rdmsframework.storage.NamedResource;
import hu.webarticum.miniconnect.rdmsframework.storage.NamedResourceStore;

public class GenericNamedResourceStore<T extends NamedResource> implements NamedResourceStore<T> {
    
    private final ImmutableList<String> names;

    private final Map<String, T> resourcesByName;
    
    
    private GenericNamedResourceStore(
            ImmutableList<String> names, Map<String, T> resourceMap) {
        this.names = names;
        this.resourcesByName = resourceMap;
    }
    
    @SafeVarargs
    public static <T extends NamedResource> GenericNamedResourceStore<T> of(T... resources) {
        return from(Arrays.asList(resources));
    }

    public static <T extends NamedResource> GenericNamedResourceStore<T> from(
            Iterable<? extends T> resources) {
        List<String> namesBuilder = new ArrayList<>();
        Map<String, T> resourceMap = new HashMap<>();
        for (T resource : resources) {
            String name = resource.name();
            if (resourceMap.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate name: " + name);
            }
            namesBuilder.add(name);
            resourceMap.put(name, resource);
        }
        ImmutableList<String> names = ImmutableList.fromCollection(namesBuilder);
        return new GenericNamedResourceStore<>(names, resourceMap);
    }
    

    @Override
    public ImmutableList<String> names() {
        return names;
    }

    @Override
    public ImmutableList<T> resources() {
        return ImmutableList.fromCollection(resourcesByName.values());
    }

    @Override
    public boolean contains(String name) {
        return resourcesByName.containsKey(name);
    }

    @Override
    public T get(String name) {
        return resourcesByName.get(name);
    }

}
