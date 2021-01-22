package com.heretere.hch.core.internal.util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.heretere.hch.core.internal.map.ConfigMap;

public final class ConfigMapperUtils {
    private static final Pattern PERIOD_MATCHER = Pattern.compile("^(.*)\\.\\s*(.*)$");

    private ConfigMapperUtils() {
        throw new IllegalStateException("Utility Class.");
    }

    public static String getEverythingAfterLastPeriod(final @NotNull String name) {
        final Matcher matcher = ConfigMapperUtils.PERIOD_MATCHER.matcher(name);

        if (!matcher.find()) {
            return name;
        }

        return matcher.group(2);
    }

    public static String getEverythingBeforeLastPeriod(final @NotNull String name) {
        final Matcher matcher = ConfigMapperUtils.PERIOD_MATCHER.matcher(name);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    public static @NotNull ConfigMap flattenMap(final @NotNull ConfigMap configMap) {
        final ConfigMap newMap = new ConfigMap();

        configMap.forEach((key, value) -> {
            if (!key.contains(".")) {
                newMap.put(key, value);
            }
        });

        return newMap;
    }

    public static @NotNull ConfigMap structureMap(final @NotNull ConfigMap configMap) {
        return ConfigMapperUtils.recursiveStructureMap("", new ConfigMap(), configMap);
    }

    private static @NotNull ConfigMap recursiveStructureMap(
            final @NotNull String parentKey,
            final @NotNull ConfigMap configMap,
            final @NotNull Map<?, ?> rawMap
    ) {
        final String dottedParentKey = parentKey + (parentKey.isEmpty() ? "" : ".");

        rawMap.forEach((key, value) -> {
            final String childKey = dottedParentKey + key;
            if (value instanceof Map) {
                configMap.put(childKey, new ConfigMap());
                ConfigMapperUtils.recursiveStructureMap(childKey, configMap, (Map<?, ?>) value);
            } else {
                configMap.put(childKey, value);
            }

            Optional.ofNullable(configMap.get(parentKey))
                .ifPresent(map -> ((ConfigMap) map).put(key.toString(), value));
        });

        return configMap;
    }

    @SuppressWarnings("unchecked")
    public static @NotNull ConfigMap deepMerge(final @Nullable ConfigMap original, final @NotNull ConfigMap newMap) {
        if (original == null) {
            return newMap;
        }

        original.keySet()
            .forEach(key -> {
                if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                    final Map<?, ?> originalChildRaw = (Map<?, ?>) Objects.requireNonNull(original.get(key));
                    final Map<?, ?> newChildRaw = (Map<?, ?>) Objects.requireNonNull(newMap.get(key));

                    final ConfigMap originalChild = originalChildRaw instanceof ConfigMap
                        ? (ConfigMap) originalChildRaw
                        : new ConfigMap(originalChildRaw);

                    final ConfigMap newChild = newChildRaw instanceof ConfigMap
                        ? (ConfigMap) newChildRaw
                        : new ConfigMap(newChildRaw);

                    original.put(
                        key,
                        deepMerge(
                            originalChild,
                            newChild
                        )
                    );

                } else if (newMap.get(key) instanceof Collection && original.get(key) instanceof Collection) {
                    final Collection<Object> originalChild = (Collection<Object>) Objects.requireNonNull(
                        original.get(key)
                    );
                    final Collection<Object> newChild = (Collection<Object>) Objects.requireNonNull(
                        newMap.get(key)
                    );

                    newChild.stream()
                        .filter(each -> !originalChild.contains(each))
                        .forEach(originalChild::add);
                } else {
                    Optional.ofNullable(newMap.get(key))
                        .ifPresent(value -> original.put(key, value));
                }
            });

        return original;
    }

    public static @NotNull List<@NotNull SimpleImmutableEntry<@NotNull String, @NotNull List<String>>> extractComments(
            final @NotNull List<@NotNull SimpleImmutableEntry<@NotNull String, @NotNull List<String>>> comments,
            final @NotNull ConfigMap configMap
    ) {
        final List<String> commentKeys = new ArrayList<>();

        configMap.forEach((key, value) -> {
            if (key.contains("_comments") && value instanceof Collection) {
                commentKeys.add(key);
                comments.add(
                    new SimpleImmutableEntry<>(
                            key.replace("_comments", ""),
                            ConfigMapperUtils.convertCollection((Collection<?>) value)
                    )
                );
            } else if (value instanceof Map) {
                ConfigMapperUtils.extractComments(
                    comments,
                    value instanceof ConfigMap
                        ? (ConfigMap) value
                        : new ConfigMap((Map<?, ?>) value)
                );
            }
        });

        commentKeys.forEach(configMap::remove);

        return comments;
    }

    private static List<String> convertCollection(final @NotNull Collection<?> collection) {
        return collection.stream()
            .map(Object::toString)
            .collect(Collectors.toList());
    }
}
