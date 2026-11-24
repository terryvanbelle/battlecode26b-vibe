package ratbot;

import static ratbot.BotTest.*;

/**
 * yl48's endgame king bar (Cfg.END_KING_TIME, 11-24). From END_KING (r1850), a lone king with 7 babies names a site
 * for a second king once the bank reaches King.endKingBar(round). That bar is min(END_KING_CHEESE, max(CASH_FLOOR,
 * 4 * (2000 - round - 55) + 10)): it falls from 390 at r1850 to 60 from r1945. sw71 needed END_KING_CHEESE 400
 * throughout. Replica 40357 arrows was lost at r2000 to a second king formed from a bank of 194.
 */
public class EndKingTest {
    public static void main(String[] args) throws Exception {
        reset(); install();
        check(Cfg.END_KING_TIME, "end king time: END_KING_TIME on (yl48)");
        check(Cfg.END_KING == 1850 && Cfg.END_KING_CHEESE == 400 && Cfg.CASH_FLOOR == 60,
              "end king time: setup: END_KING 1850, END_KING_CHEESE 400, CASH_FLOOR 60");
        check(King.endKingBar(1800) == 400, "end king time: the bar is END_KING_CHEESE 400 at r1800 (" + King.endKingBar(1800) + ")");
        check(King.endKingBar(1850) == 390, "end king time: 390 at r1850 (" + King.endKingBar(1850) + ")");
        check(King.endKingBar(1900) == 190, "end king time: 190 at r1900 (" + King.endKingBar(1900) + ")");
        check(King.endKingBar(1945) == 60 && King.endKingBar(1999) == 60,
              "end king time: CASH_FLOOR 60 from r1945 (" + King.endKingBar(1945) + ", " + King.endKingBar(1999) + ")");
        int f = -10000;   // no site opened yet
        check(King.endKing(1900, 1, 7, 190, f), "end king time: a lone king with 7 babies and 190 at r1900 names a site");
        check(!King.endKing(1900, 1, 7, 189, f), "end king time: ... but not with 189");
        check(King.endKing(1950, 1, 7, 60, f), "end king time: with 60 at r1950 it names a site (sw71 needed 400)");
        check(!King.endKing(1950, 1, 7, 59, f), "end king time: ... but not with 59");
        check(!King.endKing(1849, 1, 7, 1000, f), "end king time: not before END_KING");
        check(!King.endKing(1950, 2, 7, 1000, f), "end king time: not with two kings");
        check(!King.endKing(1950, 1, 6, 1000, f), "end king time: not with 6 babies");
        check(!King.endKing(1950, 1, 7, 1000, 1950 - Cfg.FORM_TTL), "end king time: not while the last site is open (FORM_TTL)");
        System.out.println("EndKingTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
