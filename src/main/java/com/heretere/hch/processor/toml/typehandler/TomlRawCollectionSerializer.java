/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.heretere.hch.processor.toml.typehandler;

import com.google.common.collect.Lists;
import com.heretere.hch.collection.ConfigList;
import com.heretere.hch.processor.MultiSerializer;
import com.heretere.hch.processor.exception.InvalidTypeException;
import org.jetbrains.annotations.NotNull;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

import java.util.Collection;
import java.util.List;

/**
 * This class is used to parse raw collections
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class TomlRawCollectionSerializer implements MultiSerializer<TomlParseResult, Collection> {
    @Override public @NotNull Collection deserialize(
        final @NotNull TomlParseResult parser,
        final @NotNull Class<?> exactType,
        final @NotNull String key
    ) throws InvalidTypeException {
        if (!parser.isArray(key)) {
            throw new InvalidTypeException();
        }

        TomlArray array = parser.getArray(key);

        if (array == null) {
            throw new InvalidTypeException();
        }

        return Lists.newArrayList(array.toList());
    }

    @Override public @NotNull Collection deserializeRaw(
        final @NotNull Class<?> exactType,
        final @NotNull Object value
    ) throws InvalidTypeException {
        return this.getGenericType().cast(value);
    }

    @Override public @NotNull List<String> serialize(final @NotNull Object value) {
        List<String> output = Lists.newArrayList();

        this.getGenericType().cast(value).forEach(item -> output.add(item.toString()));

        return output;
    }

    @Override public @NotNull Class<Collection> getGenericType() {
        return Collection.class;
    }
}
