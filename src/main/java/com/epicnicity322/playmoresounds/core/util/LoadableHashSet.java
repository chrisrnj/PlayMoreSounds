package com.epicnicity322.playmoresounds.core.util;

import java.util.HashSet;

/**
 * A {@link HashSet} you can use to tell if it was populated or not.
 *
 * @param <E> The element of the {@link HashSet}.
 */
public class LoadableHashSet<E> extends HashSet<E>
{
    private boolean loaded = false;

    /**
     * @return If this set was already populated.
     */
    public boolean isLoaded()
    {
        return loaded;
    }

    /**
     * Set if this set is populated.
     */
    public void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }
}
