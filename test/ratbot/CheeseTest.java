package ratbot;

import static ratbot.BotTest.*;

/**
 * The cheese a bite spends to kill (Baby.finishCheese, King.biteCheese), moved out of BotTest on 11-23 so that a
 * candidate with another rule can override this file alone (test/BOT/CheeseTest.java; tools/unit-tests.sh BOT).
 * The champions since yk1 pay k*k for +k damage; the engine needs only (k-1)^2 + 1 (fixed in yl5's bundle, and alone as
 * FINISH_LEAST after the sw58 audit).
 */
public class CheeseTest {
    public static void main(String[] args) {
        // the baby's finishing bite: k*k for +k (k up to FINISH_K) from FINISH_FLOOR cheese. Read from Cfg since the
        // sw54 audit (11-23): sw56 plays FINISH_K 8 and FINISH_FLOOR 100, where this test had sw53's 5 and 200 built in
        int fk = Cfg.FINISH_K, ff = Cfg.FINISH_FLOOR, rich = Math.max(1000, ff);
        boolean least = BankTest.flag("FINISH_LEAST");          // the sw58 audit's fix: (k-1)^2 + 1 for +k, not k*k
        int pay3 = least ? 5 : 9, payK = least ? (fk - 1) * (fk - 1) + 1 : fk * fk;
        check(Baby.finishCheese(10, rich) == 0 && Baby.finishCheese(13, rich) == (3 <= fk ? pay3 : 0)
              && Baby.finishCheese(10 + fk, rich) == payK && Baby.finishCheese(11 + fk, rich) == 0,
              "bite cheese: a baby pays " + (least ? "(k-1)^2 + 1" : "k*k") + " only when it makes the kill (FINISH_K " + fk + ")");
        if (ff > 0) check(Baby.finishCheese(13, ff - 1) == 0 && Baby.finishCheese(13, ff) == (3 <= fk ? pay3 : 0),
              "bite cheese: none below the floor (FINISH_FLOOR " + ff + ")");
        // the engine (InternalRobot.bite): a bite does 10 + ceil(sqrt(cheese)), so +k needs only (k-1)^2 + 1, not k*k.
        // Every payment must kill; it is also the least that kills only on bots with FINISH_LEAST (the sw58 audit's
        // fix newcomer), read by name as BankTest reads its switches
        boolean kills = true, isLeast = true;
        for (int hp = 11; hp <= 10 + fk; hp++) {
            int c = Baby.finishCheese(hp, rich);
            kills &= c > 0 && 10 + (int) Math.ceil(Math.sqrt(c)) >= hp;
            isLeast &= c > 0 && 10 + (int) Math.ceil(Math.sqrt(c - 1)) < hp;
        }
        check(kills, "bite cheese: every finishing payment kills (10 + ceil(sqrt c) >= hp for hp 11 to " + (10 + fk) + ")");
        if (fk >= 2) check(isLeast == least, "bite cheese: a baby's payment is " + (least ? "the least that kills (FINISH_LEAST)"
                                                                                     : "k*k, more than the least that kills (no FINISH_LEAST)"));
        boolean kingKills = true;
        int kingPays = 0;
        for (int hp = 11; hp <= 30; hp++) {
            int c = King.biteCheese(hp, 5000);
            if (c == 0) continue;
            kingPays++;
            kingKills &= 10 + (int) Math.ceil(Math.sqrt(c)) >= hp;
        }
        check(kingPays > 0 && kingKills, "bite cheese: every payment of the king kills (" + kingPays + " target healths paid for)");
        check(King.biteCheese(18, 1000) == (least ? 50 : 64), "bite cheese: the king pays " + (least ? "the least that kills, 50"
                                                                                                  : "k*k, 64") + ", for +8");
        // over budget: +8 (hp 18) costs 64 (k*k) or 50 (FINISH_LEAST), paid only from that much above KING_BITE_FLOOR
        int c18 = least ? 50 : 64, kf = Cfg.KING_BITE_FLOOR;
        check(King.biteCheese(100, 5000) == 0 && King.biteCheese(18, kf + c18 - 1) == 0 && King.biteCheese(18, kf + c18) == c18
              && King.biteCheese(21, 5000) == 0,
              "bite cheese: nothing for a bite that does not kill or is over budget (+8 costs " + c18 + " above " + kf + ")");
        System.out.println("CheeseTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
