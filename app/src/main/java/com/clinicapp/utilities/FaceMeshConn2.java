package com.clinicapp.utilities;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

public final class FaceMeshConn2 {

    @AutoValue
    public abstract static class Connection {
        public Connection() {
        }

        static Connection create(int start, int end) {
            return new AutoValue2(start, end);
        }

        public abstract int start();

        public abstract int end();
    }

    public static final ImmutableSet<Connection> FACE_SQUARE_FOREHEAD =
            ImmutableSet.of(
                    Connection.create(109, 338),
                    Connection.create(338, 337),
                    Connection.create(337, 108),
                    Connection.create(108, 109));

    public static final ImmutableSet<Connection> FACE_SQUARE_FOREHEAD_2 =
            ImmutableSet.of(
                    Connection.create(372, 2),
                    Connection.create(2, 143),
                    Connection.create(143, 10),
                    Connection.create(10, 372));

    public static final ImmutableSet<Connection> FACE_SQUARE_CHIN =
            ImmutableSet.of(
                    Connection.create(187, 2),
                    Connection.create(2, 411),
                    Connection.create(411, 152),
                    Connection.create(152, 187));

    public static final ImmutableSet<Connection> FACE_SQUARE_LEFT =
            ImmutableSet.of(
                    Connection.create(349, 345),
                    Connection.create(345, 433),
                    Connection.create(433, 287),
                    Connection.create(287, 349));

    public static final ImmutableSet<Connection> FACE_SQUARE_RIGHT =
            ImmutableSet.of(
                    Connection.create(120, 116),
                    Connection.create(116, 213),
                    Connection.create(213, 57),
                    Connection.create(57, 120));

}
