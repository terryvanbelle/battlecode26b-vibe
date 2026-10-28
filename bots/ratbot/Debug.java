package ratbot;

/** Indicator-string telemetry. Tags are stable so replay tools can grep them. */
public final class Debug {
    public static final boolean ON = false;

    /** Overrun locator: mark(k) at turn stages; the first stage reached after the round advanced is kept. */
    public static int stage, ovStage = -1, startRound;
    public static boolean crossed;

    public static int lastBc, hotStage, hotBc;   // the costliest stage segment this turn

    public static void begin(int round) { startRound = round; stage = 0; crossed = false; lastBc = 0; hotBc = 0; }

    public static void mark(int k) {
        int bc = battlecode.common.Clock.getBytecodeNum();
        if (!crossed && G.rc.getRoundNum() != startRound) { crossed = true; ovStage = stage * 100 + k; }
        else if (bc - lastBc > hotBc) { hotBc = bc - lastBc; hotStage = stage * 100 + k; }
        lastBc = bc;
        stage = k;
    }

    public static void indicator() {
        StringBuilder sb = new StringBuilder(96);
        if (G.king) {
            sb.append("K sp=").append(King.spawned).append(" tr=").append(King.traps).append(" inc=").append(Econ.rate100()).append(" sur=").append(Econ.surplus()).append(" km=").append(King.mineWhy).append(Comms.numMines);
        } else {
            sb.append(Baby.role == Baby.FIGHT ? "F" : Baby.role == Baby.ATTACK ? "A" : "G")
              .append(" dl=").append(Baby.delivered).append(" mm=").append(Baby.myMineCount)
              .append(" bc=").append(Baby.bitesCat)
              .append(" br=").append(Baby.bitesRat)
              .append(" tr=").append(Baby.trapsPlaced)
              .append(" rt=").append(Baby.ratTraps).append(" nap=").append(Baby.naps).append(" th=").append(Baby.throwsDone)
              .append(" dg=").append(Nav.digs).append(" fl=").append(Flow.builds).append(":").append(Flow.done).append(":").append(Flow.lastTurns).append(":").append(Flow.steps).append(" mv=").append(Baby.why);
        }
        if (Baby.raidTarget != null) sb.append(" rz=").append(Baby.raidTarget.x).append(',').append(Baby.raidTarget.y);
        sb.append(" sym=").append(Sym.mask);
        if (Sym.contradictions > 0) sb.append(" SYMX=").append(Sym.contradictions);
        if (!G.king && Baby.lowBc > 0) sb.append(" lb=").append(Baby.lowBc);
        if (RobotPlayer.overruns > 0) sb.append(" OVR=").append(RobotPlayer.overruns).append(" OVS=").append(ovStage);
        if (RobotPlayer.nearMiss > 0) sb.append(" NEAR=").append(RobotPlayer.nearMiss);
        if (RobotPlayer.exceptions > 0) sb.append(" EXC=").append(RobotPlayer.exceptions);
        sb.append(" hot=").append(hotStage).append(':').append(hotBc);
        sb.append(" mbc=").append(RobotPlayer.maxBc);
        G.rc.setIndicatorString(sb.toString());
    }
}
