package ratbot;

import static ratbot.BotTest.*;

/**
 * sw70's terrain from symmetry (Cfg.SYM_FILL 200: u5x's Sym.fill/backfill of 11-02, ported to yl32 on 11-24). At the
 * end of a baby's turn, with Sym.FILL_LEFT bytecodes to spare, up to SYM_FILL remembered tiles a turn copy their seen
 * wall or dirt onto unseen twins where every live symmetry agrees; the twin stays unseen, so sensing it overwrites
 * the guess. On rift (local yl32 mirror and replica 39577) raiders planned a 22-tile Flow path from (26,6) to (47,3)
 * straight through the x=37-38 wall, the twin of our own x=21-22 wall (Flow takes unknown tiles as open).
 */
public class SymFillTest {
    static int at(int x, int y) { return Sym.mem[x + y * G.W]; }

    public static void main(String[] args) throws Exception {
        check(Cfg.SYM_FILL == 200, "sym fill: SYM_FILL 200 (sw70; " + Cfg.SYM_FILL + ")");

        // one symmetry left (ROT): a seen wall's unseen twin becomes a wall, still unseen
        reset(); W = 30; H = 20; install();
        Sym.mask = Sym.ROT;
        Sym.record(5, 3, true, false, false);
        check(Sym.fillDue(), "sym fill: a pass is due after a wall is first seen");
        Sym.backfill(10000);
        check((at(24, 16) & Sym.WALL) != 0 && (at(24, 16) & Sym.SEEN) == 0,
              "sym fill: the ROT twin (24,16) of a seen wall is a wall, unseen (" + at(24, 16) + ")");
        // dirt too, on the next pass (a newly seen dirt tile marks the pass dirty)
        Sym.record(7, 2, false, true, false);
        check(Sym.fillDue(), "sym fill: a newly seen dirt tile makes a pass due");
        Sym.backfill(10000);
        check((at(22, 17) & Sym.DIRT) != 0 && (at(22, 17) & Sym.SEEN) == 0, "sym fill: dirt is copied to the ROT twin (22,17)");
        // a seen open tile leaves its twin unknown
        Sym.record(8, 8, false, false, false);
        Sym.backfill(10000);
        check(at(21, 11) == 0, "sym fill: an open tile's twin stays unknown (" + at(21, 11) + ")");
        // dirt dug since: sensing the twin open overwrites the guess, and later passes skip it (seen)
        Sym.record(22, 17, false, false, false);
        Sym.backfill(10000);
        check((at(22, 17) & (Sym.DIRT | Sym.WALL)) == 0 && (at(22, 17) & Sym.SEEN) != 0,
              "sym fill: sensing the twin open overwrites the guessed dirt, and a pass does not restore it");
        check(!Sym.fillDue(), "sym fill: no pass due once a pass ends with nothing new seen");

        // two live symmetries (ROT, HOR): a twin is filled only where both agree
        reset(); W = 30; H = 20; install();
        Sym.mask = Sym.ROT | Sym.HOR;
        Sym.record(5, 3, true, false, false);
        Sym.backfill(10000);
        check((at(24, 16) & Sym.WALL) == 0 && (at(5, 16) & Sym.WALL) == 0,
              "sym fill: two live symmetries and one wall: no twin is filled");
        Sym.record(24, 3, true, false, false);   // the VER image: under ROT or HOR the remaining twins are walls
        check(Sym.mask == (Sym.ROT | Sym.HOR), "sym fill: setup: the mask keeps ROT and HOR");
        Sym.backfill(10000);
        check((at(24, 16) & Sym.WALL) != 0 && (at(5, 16) & Sym.WALL) != 0,
              "sym fill: with both images seen, (24,16) and (5,16) are walls under either symmetry");

        // the bytecode guard: under FILL_LEFT left the pass waits and stays due
        reset(); W = 30; H = 20; install();
        Sym.mask = Sym.ROT;
        Sym.record(5, 3, true, false, false);
        clock(Sym.FILL_LEFT - 1);
        Sym.backfill(10000);
        check((at(24, 16) & Sym.WALL) == 0 && Sym.fillDue(), "sym fill: under FILL_LEFT bytecodes nothing is filled and the pass stays due");
        clock(1 << 30);
        Sym.backfill(10000);
        check((at(24, 16) & Sym.WALL) != 0, "sym fill: with bytecodes back the pass fills");

        // SYM_FILL tiles a call: a wall at index 5 + 13 * 30 = 395 is reached on the second call of 200
        reset(); W = 30; H = 20; install();
        Sym.mask = Sym.ROT;
        Sym.record(5, 13, true, false, false);
        Sym.backfill(Cfg.SYM_FILL);
        check((at(24, 6) & Sym.WALL) == 0, "sym fill: the first " + Cfg.SYM_FILL + " tiles of a pass stop short of tile 395");
        Sym.backfill(Cfg.SYM_FILL);
        check((at(24, 6) & Sym.WALL) != 0, "sym fill: the second call fills its twin (24,6)");

        System.out.println("SymFillTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
