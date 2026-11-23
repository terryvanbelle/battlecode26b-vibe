package ratbot;

import static ratbot.BotTest.*;

/**
 * yl5's bite cheese (overrides test/ratbot/CheeseTest.java in `bash tools/unit-tests.sh yl5`): the least that kills.
 * The engine (InternalRobot.bite) deals 10 + ceil(sqrt(c)) for c cheese, so +k damage needs (k-1)^2 + 1.
 */
public class CheeseTest {
    /** The engine's damage for a bite spending c. */
    static int damage(int c) { return 10 + (c > 0 ? (int) Math.ceil(Math.sqrt(c)) : 0); }

    public static void main(String[] args) {
        boolean least = true;
        for (int hp = 11; hp <= 10 + Cfg.FINISH_K; hp++) {
            int c = Baby.finishCheese(hp, 1000);
            least &= c > 0 && damage(c) >= hp && damage(c - 1) < hp;
        }
        check(least, "bite cheese: a baby pays the least that kills, for every +k up to FINISH_K");
        check(Baby.finishCheese(10, 1000) == 0 && Baby.finishCheese(13, 1000) == 5 && Baby.finishCheese(16, 1000) == 0,
              "bite cheese: a baby pays (k-1)^2+1 only when it makes the kill");
        check(Baby.finishCheese(13, 100) == 0, "bite cheese: none below the floor");
        check(King.biteCheese(18, 1000) == 50 && damage(50) == 18 && damage(49) < 18, "bite cheese: the king pays the least that kills");
        check(King.biteCheese(18, 350) == 50, "bite cheese: 50 kills an 18-hp rat within a 50 budget (yk1 refused: 64)");
        check(King.biteCheese(20, 1000) == 82, "bite cheese: a 20-hp rat for 82 (yk1: 100)");
        check(King.biteCheese(100, 5000) == 0 && King.biteCheese(21, 5000) == 0,
              "bite cheese: nothing for a bite that does not kill or is over budget");
        System.out.println("CheeseTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
