package tetris.replay;

import java.io.Serializable;

public class ReplayEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long timeMs;
    private final ReplayEventType type;

    public ReplayEvent(long timeMs, ReplayEventType type) {
        this.timeMs = timeMs;
        this.type = type;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public ReplayEventType getType() {
        return type;
    }
}