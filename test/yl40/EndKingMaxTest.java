package ratbot;

import static ratbot.BotTest.*;

/**
 * yl40's endgame king past the second (Cfg.END_KING_MAX 3, 11-24). King.endKing named an endgame formation site only
 * for a lone king (kings < 2); with END_KING_MAX 3 a second king's lead names one too, so we can end the game with a
 * third king. Kings weigh 0.5 of the war points at round 2000: replica rift vs AdamTan12_Icarus 39670 (2 kings to 3,
 * 52 babies and 3008 banked at r1850) and 39663 (2 to 2, 30 babies and 652 banked) were both lost 46-53 on points and
 * turn 51-48 with one more king.
 */
public class EndKingMaxTest {
    public static void main(String[] args) throws Exception {
        check(Cfg.END_KING_MAX == 3, "end king max: END_KING_MAX 3 (yl40; " + Cfg.END_KING_MAX + ")");
        int r = Cfg.END_KING, c = Cfg.END_KING_CHEESE, old = -1000;
        check(King.endKing(r, 2, 7, c, old), "end king max: two kings, 7 babies, the bar banked, round END_KING: a site is due");
        check(King.endKing(r, 1, 7, c, old), "end king max: a lone king still gets its site");
        check(!King.endKing(r, 3, 50, 5000, old), "end king max: three kings: no endgame site");
        check(!King.endKing(r - 1, 2, 7, c, old), "end king max: not before END_KING");
        check(!King.endKing(r, 2, 6, c, old), "end king max: not with 6 babies");
        check(!King.endKing(r, 2, 7, c - 1, old), "end king max: not under END_KING_CHEESE banked");
        check(!King.endKing(r + Cfg.FORM_TTL, 2, 7, c, r) && King.endKing(r + Cfg.FORM_TTL + 1, 2, 7, c, r),
              "end king max: a timed-out site is renamed only after FORM_TTL");
        // the two replica games: r1850 with 2 kings
        check(King.endKing(1850, 2, 52, 3008, old) && King.endKing(1850, 2, 30, 652, old),
              "end king max: 39670 (52 babies, 3008) and 39663 (30 babies, 652) at r1850 would name a site");
        // formation()'s own gates let the third king through at war by END_KING
        check(Cfg.END_KING_MAX <= Math.min(Math.max(Cfg.FORM_MAX, Cfg.FORM_MAX_WAR), Comms.KING_SLOTS),
              "end king max: formation's cap (FORM_MAX, KING_SLOTS) admits END_KING_MAX kings");
        check(Cfg.FORM_WAR_FROM <= Cfg.END_KING, "end king max: formation's war gate (FORM_WAR_FROM) is open by END_KING");
        System.out.println("EndKingMaxTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
