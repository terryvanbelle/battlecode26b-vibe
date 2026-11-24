package ratbot;

import battlecode.common.GameConstants;
import static ratbot.BotTest.*;

/**
 * yl41's king before the cut (Cfg.CUT_KING_FROM 1000, CUT_KING_MAX 3, CUT_KING_BANK 1500; 11-24). The engine allows
 * no new king after round 1200 once a team has 2, and kings weigh 0.5 of the war points at round 2000. From round 1000
 * to King.FORM_LAST_ROUND, with fewer than 3 kings, FORM_BABIES babies and 1500 banked, King.cutKing names one more
 * formation site without the income test. Replica rift vs AdamTan12_Icarus 39663 (r1000: 2 kings, 43 babies, 2240
 * banked) and 39670 (2 kings, 43 babies, 2947) formed no third king and lost 46-53 on points; one more king makes it 51-48.
 */
public class CutKingTest {
    public static void main(String[] args) throws Exception {
        check(Cfg.CUT_KING_FROM == 1000 && Cfg.CUT_KING_MAX == 3 && Cfg.CUT_KING_BANK == 1500,
              "cut king: CUT_KING_FROM 1000, CUT_KING_MAX 3, CUT_KING_BANK 1500 (yl41)");
        int b = Cfg.FORM_BABIES, c = Cfg.CUT_KING_BANK, old = -1000, last = King.FORM_LAST_ROUND;
        check(King.cutKing(1000, 2, b, c, old), "cut king: two kings, FORM_BABIES babies, the bar banked, round 1000: a site is due");
        check(King.cutKing(1100, 1, b, c, old), "cut king: a lone king gets its second the same way");
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
        System.out.println("CutKingTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
