package ratbot;

import static ratbot.BotTest.*;

/**
 * sw72 (sw68 + sw69) has Cfg.PREY_WEAKEST, sw68's prey by health, so the champion sw69's nearest-prey test
 * (test/ratbot/PreyNearestTest) does not apply to it: PreyWeakestTest covers its Baby.prey.
 */
public class PreyNearestTest {
    public static void main(String[] args) throws Exception {
        check(Cfg.PREY_WEAKEST, "prey nearest: replaced on sw72 by PREY_WEAKEST (PreyWeakestTest)");
        System.out.println("PreyNearestTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
