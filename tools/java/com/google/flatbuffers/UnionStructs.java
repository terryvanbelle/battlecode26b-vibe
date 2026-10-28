package com.google.flatbuffers;

import java.nio.ByteBuffer;

/** Reads struct-typed members of a union vector (the 2026 schema's Turn.actions). Lives in this package because the
 *  needed Table accessors are protected. vtableOffset is the field's vtable slot (Turn.actions = 26). */
public final class UnionStructs {
    public static int pos(Table t, int vtableOffset, int j) {
        int o = t.__offset(vtableOffset);
        int v = t.__vector(o) + j * 4;
        return v + t.bb.getInt(v);
    }
    public static ByteBuffer bb(Table t) { return t.bb; }
}
