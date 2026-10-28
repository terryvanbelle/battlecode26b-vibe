package ratbot;

/** Indicator-string telemetry. Tags are stable so replay tools can grep them. */
public final class Debug {
    public static final boolean ON = false;

    public static void indicator() {
        StringBuilder sb = new StringBuilder(96);
        if (G.king) {
            sb.append("K sp=").append(King.spawned).append(" tr=").append(King.traps).append(" inc=").append(Econ.rate100()).append(" sur=").append(Econ.surplus());
        } else {
            sb.append(Baby.role == Baby.FIGHT ? "F" : Baby.role == Baby.ATTACK ? "A" : "G")
              .append(" dl=").append(Baby.delivered)
              .append(" bc=").append(Baby.bitesCat)
              .append(" br=").append(Baby.bitesRat)
              .append(" tr=").append(Baby.trapsPlaced)
              .append(" rt=").append(Baby.ratTraps).append(" nap=").append(Baby.naps).append(" th=").append(Baby.throwsDone)
              .append(" dg=").append(Nav.digs);
        }
        sb.append(" sym=").append(Sym.mask);
        if (Sym.contradictions > 0) sb.append(" SYMX=").append(Sym.contradictions);
        if (RobotPlayer.overruns > 0) sb.append(" OVR=").append(RobotPlayer.overruns);
        if (RobotPlayer.nearMiss > 0) sb.append(" NEAR=").append(RobotPlayer.nearMiss);
        if (RobotPlayer.exceptions > 0) sb.append(" EXC=").append(RobotPlayer.exceptions);
        sb.append(" mbc=").append(RobotPlayer.maxBc);
        G.rc.setIndicatorString(sb.toString());
    }
}
