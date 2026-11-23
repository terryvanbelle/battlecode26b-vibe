package ratbot;

import static ratbot.BotTest.*;

/**
 * The cheese a bite spends to kill (Baby.finishCheese, King.biteCheese), moved out of BotTest on 11-23 so that a
 * candidate with another rule can override this file alone (test/BOT/CheeseTest.java; tools/unit-tests.sh BOT).
 * The champion yk1 pays k*k for +k damage; the engine needs only (k-1)^2 + 1 (fixed in yl5).
 */
public class CheeseTest {
    public static void main(String[] args) {
        check(Baby.finishCheese(10, 1000) == 0 && Baby.finishCheese(13, 1000) == 9 && Baby.finishCheese(16, 1000) == 0,
              "bite cheese: a baby pays k*k only when it makes the kill");
        check(Baby.finishCheese(13, 100) == 0, "bite cheese: none below the floor");
        check(King.biteCheese(18, 1000) == 64, "bite cheese: the king pays the least that kills");
        check(King.biteCheese(100, 5000) == 0 && King.biteCheese(18, 350) == 0 && King.biteCheese(21, 5000) == 0,
              "bite cheese: nothing for a bite that does not kill or is over budget");
        System.out.println("CheeseTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
