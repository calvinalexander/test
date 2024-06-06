package com.clinicapp.utilities;

import com.google.mediapipe.solutions.facemesh.FaceMeshConnections;

public class AutoValue2 extends FaceMeshConn2.Connection {
    private final int start;
    private final int end;

    AutoValue2(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int start() {
        return this.start;
    }

    public int end() {
        return this.end;
    }

    public String toString() {
        return "Connection{start=" + this.start + ", end=" + this.end + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FaceMeshConnections.Connection)) {
            return false;
        } else {
            FaceMeshConnections.Connection that = (FaceMeshConnections.Connection)o;
            return this.start == that.start() && this.end == that.end();
        }
    }

    public int hashCode() {
        int h$ = 1;
        h$ = h$ * 1000003;
        h$ ^= this.start;
        h$ *= 1000003;
        h$ ^= this.end;
        return h$;
    }
}
