package ratbot;

import static ratbot.BotTest.*;

/**
 * The cheese a bite spends to kill (Baby.finishCheese, King.biteCheese), moved out of BotTest on 11-23 so that a
 * candidate with another rule can override this file alone (test/BOT/CheeseTest.java; tools/unit-tests.sh BOT).
 * The champion yk1 pays k*k for +k damage; the engine needs only (k-1)^2 + 1 (fixed in yl5).
 */
public class CheeseTest {
    public static void main(String[] args) {
        // the baby's finishing bite: k*k for +k (k up to FINISH_K) from FINISH_FLOOR cheese. Read from Cfg since the
        // sw54 audit (11-23): sw56 plays FINISH_K 8 and FINISH_FLOOR 100, where this test had sw53's 5 and 200 built in
        int fk = Cfg.FINISH_K, ff = Cfg.FINISH_FLOOR, rich = Math.max(1000, ff);
        check(Baby.finishCheese(10, rich) == 0 && Baby.finishCheese(13, rich) == (3 <= fk ? 9 : 0)
              && Baby.finishCheese(10 + fk, rich) == fk * fk && Baby.finishCheese(11 + fk, rich) == 0,
              "bite cheese: a baby pays k*k only when it makes the kill (FINISH_K " + fk + ")");
        if (ff > 0) check(Baby.finishCheese(13, ff - 1) == 0 && Baby.finishCheese(13, ff) == (3 <= fk ? 9 : 0),
              "bite cheese: none below the floor (FINISH_FLOOR " + ff + ")");
        check(King.biteCheese(18, 1000) == 64, "bite cheese: the king pays the least that kills");
        check(King.biteCheese(100, 5000) == 0 && King.biteCheese(18, 350) == 0 && King.biteCheese(21, 5000) == 0,
              "bite cheese: nothing for a bite that does not kill or is over budget");
        System.out.println("CheeseTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
