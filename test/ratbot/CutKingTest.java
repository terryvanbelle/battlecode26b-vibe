package ratbot;

import battlecode.common.GameConstants;
import static ratbot.BotTest.*;

/**
 * sw96's king before the cut (sw95's switch with a guard, 11-25; Cfg.CUT_KING_FROM 1000, CUT_KING_MAX 3, CUT_KING_BANK
 * 1200, CUT_KING_MIN 2). The engine allows no new king after round 1200 once a team has 2, and kings weigh 0.5 of the
 * war points at round 2000. From round 1000 to King.FORM_LAST_ROUND, with exactly 2 kings, FORM_BABIES babies and 1200
 * banked, King.cutKing names the third king's formation site without the income test (sw95 also named a lone king's
 * second, from 600 banked: two sites in 25 rounds and three starving kings in its pipes smoke). Replica rift vs AdamTan12_Icarus 39663 (r1000: 2 kings, 43 babies, 2240
 * banked) and 39670 (2 kings, 43 babies, 2947) formed no third king and lost 46-53 on points; one more king makes it 51-48.
 */
public class CutKingTest {
    /** King.formation() for the lead king (slot 0) at war on an open 40x40 map at round r, k kings (1 or 2), rat cost
     *  60 (19 babies with 2 kings), the given bank, no mine known, no income measured: true when it named a site. */
    static boolean opened(int bank, int r, int k) throws Exception {
        reset(); W = 40; H = 40; install();
        type = battlecode.common.UnitType.RAT_KING; me = new battlecode.common.MapLocation(8, 8);
        round = r; G.startTurn(); G.coop = false;
        King.slot = 0; King.start = me; King.formSite = null; King.formStart = -1000;
        Comms.numKings = k; Comms.kings[0] = me; Comms.kings[1] = k > 1 ? new battlecode.common.MapLocation(31, 31) : null; Comms.numMines = 0;
        global = bank; ratCost = 60;
        King.formation();
        boolean named = King.formSite != null && shared[Comms.FORM_T] != 0;
        King.slot = -1; King.formSite = null; King.formStart = -1000; Comms.numKings = 0;
        return named;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.CUT_KING_FROM == 1000 && Cfg.CUT_KING_MAX == 3 && Cfg.CUT_KING_BANK == 1200 && Cfg.CUT_KING_MIN == 2,
              "cut king: CUT_KING_FROM 1000, CUT_KING_MAX 3, CUT_KING_BANK 1200, CUT_KING_MIN 2 (sw96)");
        check(Cfg.CUT_KING_BANK == Cfg.FORM3_BANK, "cut king: the bar is the generic rule's third-king bank (FORM3_BANK)");
        int b = Cfg.FORM_BABIES, c = Cfg.CUT_KING_BANK, old = -1000, last = King.FORM_LAST_ROUND;
        check(King.cutKing(1000, 2, b, c, old), "cut king: two kings, FORM_BABIES babies, the bar banked, round 1000: a site is due");
        check(!King.cutKing(1100, 1, b, c, old) && !King.cutKing(1100, 1, 60, 5000, old),
              "cut king: a lone king gets no second from the cut rule, however rich (CUT_KING_MIN 2; sw95 named one)");
        check(!King.cutKing(999, 2, b, c, old), "cut king: not before CUT_KING_FROM");
        check(King.cutKing(last, 2, b, c, old) && !King.cutKing(last + 1, 2, b, c, old), "cut king: up to FORM_LAST_ROUND only");
        check(!King.cutKing(1100, 3, 60, 5000, old), "cut king: not at CUT_KING_MAX kings");
        check(!King.cutKing(1100, 2, b - 1, c, old), "cut king: not under FORM_BABIES babies");
        check(!King.cutKing(1100, 2, b, c - 1, old), "cut king: not under CUT_KING_BANK banked");
        check(!King.cutKing(1100, 2, b, c, 1100 - Cfg.FORM_TTL) && King.cutKing(1101, 2, b, c, 1100 - Cfg.FORM_TTL),
              "cut king: a timed-out site is renamed only after FORM_TTL");
        check(King.cutKing(1000, 2, 43, 2240, old) && King.cutKing(1000, 2, 43, 2947, old),
              "cut king: 39663 and 39670 at r1000 would name a site");
        // the engine's cut and the formation's own caps
        check(King.FORM_LAST_ROUND < GameConstants.RAT_KING_CUTOFF_ROUND && GameConstants.MAX_NUMBER_OF_RAT_KINGS_AFTER_CUTOFF == 2,
              "cut king: the last site opens before the engine's cut (" + King.FORM_LAST_ROUND + " < " + GameConstants.RAT_KING_CUTOFF_ROUND + ")");
        check(Cfg.CUT_KING_MAX <= Math.min(King.formCap(false), Comms.KING_SLOTS) && Cfg.CUT_KING_MAX <= GameConstants.MAX_NUMBER_OF_RAT_KINGS,
              "cut king: formation's cap (FORM_MAX, KING_SLOTS) and the engine admit CUT_KING_MAX kings");
        check(Cfg.FORM_WAR_FROM <= Cfg.CUT_KING_FROM, "cut king: formation's war gate is open by CUT_KING_FROM");
        // formation() itself (sw95): the lead king at war, round 1100, 2 kings, 19 babies by the rat cost (60), no mine
        // known: at the bar the cut-king branch names a site (FORM_T stamped); one cheese under it the generic branch
        // still stops at FORM3_BANK (1200), as in sw91
        check(opened(Cfg.CUT_KING_BANK, 1100, 2), "cut king: formation() names a site on the bar at round 1100");
        check(!opened(Cfg.CUT_KING_BANK - 1, 1100, 2) && Cfg.CUT_KING_BANK - 1 < Cfg.FORM3_BANK,
              "cut king: under the bar (and under FORM3_BANK) no site");
        check(!opened(Cfg.CUT_KING_BANK, Cfg.CUT_KING_FROM - 1, 2) && Cfg.CUT_KING_BANK < Cfg.FORM_RICH_BANK,
              "cut king: before CUT_KING_FROM the bar opens nothing (the generic branch still asks for income; under FORM_RICH_BANK)");
        check(!opened(Cfg.CUT_KING_BANK, King.FORM_LAST_ROUND + 1, 2), "cut king: after FORM_LAST_ROUND no site");
        check(!opened(2000, 1100, 1) && 2000 < Cfg.FORM_RICH_BANK,
              "cut king: a lone king with 2000 banked at round 1100 names no site (sw95 named its second here)");
        System.out.println("CutKingTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
