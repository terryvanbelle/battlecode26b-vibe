package ratbot;

import battlecode.common.*;

import java.util.HashSet;
import java.util.Set;

import static ratbot.BotTest.*;

/** yl12 = sw53 + Cfg.RAID_POST: a raider takes the enemy twin of a contested home mine (within RAID_POST of the front),
 *  else the twin of its pick (`bash tools/unit-tests.sh yl12`; move to test/ratbot/ if yl12 is crowned). */
public class RaidPostTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    public static void main(String[] args) throws Exception {
        // home (3,3) on a 30x30 rotation map (enemy start (26,26)): (5,5) is ours, 19 steps behind the front;
        // (12,12) and (14,10) are ours and contested
        MapLocation back = p(5, 5);
        Set<MapLocation> got = new HashSet<>(), twins = new HashSet<>();
        for (int s = 0; s < 6; s++) {
            reset(); W = 30; H = 30; install(); round = 400; G.startTurn();
            Sym.mask = Sym.ROT; G.coop = false;
            Baby.home = p(3, 3); Baby.role = Baby.GATHER; Baby.postMargin = 0;
            Baby.myMineCount = 0; Baby.mineShift = 0; Baby.slot = s;   // slot bits 12-13 = 0: a raider
            Comms.numMines = 0;
            for (MapLocation m : new MapLocation[] {back, p(12, 12), p(14, 10)}) Comms.mines[Comms.numMines++] = m;
            got.add(Baby.pickMine());
        }
        for (MapLocation m : new MapLocation[] {p(12, 12), p(14, 10)}) twins.add(G.mirror(m, Sym.ROT));
        check(got.equals(twins), "raid post: gatherer raiders hold only the twins of the contested home mines " + got);
        // no contested mine known: the twin of the pick, as sw53
        reset(); W = 30; H = 30; install(); round = 400; G.startTurn();
        Sym.mask = Sym.ROT; G.coop = false;
        Baby.home = p(3, 3); Baby.role = Baby.GATHER; Baby.postMargin = 0;
        Baby.myMineCount = 0; Baby.mineShift = 0; Baby.slot = 0;
        Comms.numMines = 0; Comms.mines[Comms.numMines++] = back;
        MapLocation one = Baby.pickMine();
        check(one.equals(G.mirror(back, Sym.ROT)), "raid post: none contested: the twin of the pick " + one);
        Comms.numMines = 0; Sym.mask = Sym.ROT | Sym.HOR | Sym.VER;
        System.out.println("RaidPostTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
