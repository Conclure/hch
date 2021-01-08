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

package com.heretere.hch.structure.backend;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SuppliedConfigField<T> extends ConfigSection implements ConfigField<T> {
    /**
     * The generic class type of this config field.
     */
    private final @NotNull Class<T> type;
    /**
     * The supplier to retrieve the value of this config field.
     */
    private final @NotNull Supplier<@NotNull T> getter;
    /**
     * The consumer to update the value of this config field.
     */
    private final @NotNull Consumer<@NotNull T> setter;

    /**
     * @param key      The key to this config field.
     * @param comments The comments attached to this config field.
     * @param type     The generic type of this config field.
     * @param getter   The supplier for this config field.
     * @param setter   this consumer for this config field.
     */
    public SuppliedConfigField(
        final @NotNull String key,
        final @NotNull List<@NotNull String> comments,
        final @NotNull Class<T> type,
        final @NotNull Supplier<@NotNull T> getter,
        final @NotNull Consumer<@NotNull T> setter
    ) {
        super(key, comments);

        Preconditions.checkState(!type.isPrimitive(), "Primitive types are not allowed. Invalid Key (%s).", key);

        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    @Override public @NotNull Optional<T> getValue() {
        return Optional.of(this.getter.get());
    }

    @Override public void setValue(final @NotNull T value) {
        this.setter.accept(value);
    }

    @Override public void setValueRaw(final @NotNull Object value) {
        this.setter.accept(this.getGenericType().cast(value));
    }

    @Override public @NotNull Class<T> getGenericType() {
        return this.type;
    }
}
