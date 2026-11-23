# Local champions vs the replica: win rate vs xk6 against win rate vs AlexT101_usqualsbot

**Owner request (11-23 11:48 PDT):** a scatterplot and correlation between local champions and the panel. For each
champion, compare its win rate vs xk6 (local) with its win rate vs AlexT101_usqualsbot (replica). Use a sample of 10
champions evenly spaced over time, with 5 games against each opponent.

![scatter](champ-corr.png)

## Results (11-23 12:19 PDT)

| bot | crowned (PDT) | local vs xk6 | replica vs AlexT101_usqualsbot | replica games: existing + top-up | tournament rating |
|---|---|---|---|---|---|
| xr49 | 11-21 09:13 | 3-2 | 19-26 (42%) | 45 + 0 (n3 panel 42, ranked 3) | 1438 ± 10 |
| xu2 | 11-21 15:07 | 3-2 | 3-2 (60%) | 3 + 2 | 1462 ± 8 |
| xw9 | 11-21 19:55 | 2-3 | 3-2 (60%) | 0 + 5 | 1500 ± 8 |
| ya0 | 11-22 00:55 | 3-2 | 2-4 (33%) | 6 + 0 | 1515 ± 8 |
| yb9 | 11-22 06:17 | 2-3 | 2-7 (22%) | 9 + 0 | 1503 ± 7 |
| yd1 | 11-22 12:56 | 2-3 | 2-4 (33%) | 6 + 0 | 1530 ± 10 |
| ye4 | 11-22 16:57 | 3-2 | 3-2 (60%) | 0 + 5 | 1537 ± 9 |
| sw38 | 11-22 22:36 | 3-2 | 2-3 (40%) | 0 + 5 | 1534 ± 11 |
| yk1 | 11-23 06:36 | 3-2 | 3-2 (60%) | 3 + 2 | 1578 ± 10 |
| sw54 | 11-23 10:34 | 4-1 | 4-1 (80%) | 3 + 2 | 1666 ± 13 |

**Correlations over the 10 champions:**

| pair | Pearson r (95% CI) | permutation p | Spearman ρ (p) | leave one out |
|---|---|---|---|---|
| local rate vs xk6 ~ replica rate | **+0.63** (−0.01 to +0.90) | 0.057 | +0.58 (0.088) | +0.37 (without sw54) to +0.83 (without xw9) |
| tournament rating ~ replica rate | +0.53 (−0.15 to +0.87) | 0.110 | +0.34 (0.335) | +0.10 (without sw54) to +0.67 (without xu2) |
| tournament rating ~ local rate vs xk6 | +0.52 (−0.17 to +0.86) | 0.128 | +0.44 (0.217) | |

**Per map** (local: the 10 champions' games vs xk6; replica: the 21 top-up games vs AlexT101_usqualsbot):

| map | local wins | replica wins |
|---|---|---|
| DefaultLarge | 6 of 10 | 4 of 6 |
| ZeroDay | 1 of 10 | 1 of 6 |
| dirtpassageway | 10 of 10 | 3 of 3 |
| rift | 4 of 10 | 3 of 3 |
| wallsofparadis | 7 of 10 | 0 of 3 |

**What it says:**
- **The association is positive, but this sample cannot confirm it.** Local win rate vs xk6 and replica win rate vs
  AlexT101_usqualsbot correlate at r +0.63, and the tournament rating with the replica rate at +0.53. With 10
  points of about 5 games each, both 95% intervals reach zero (permutation p 0.06 and 0.11).
- **sw54 carries much of it.** It is the best of the 10 on every measure: 4-1 locally, 4-1 on the replica and the top
  rating. Without it, r falls to +0.37 and +0.10.
- **The weakest on both are the 11-22 morning champions,** yb9 and yd1: 2-3 locally, and 22% and 33% on the replica.
- **Pooled,** the 3 champions that went 2-3 locally are 7-13 (35%) on the replica. The 7 that went 3-2 or better are
  36-40 (47%), or 17-14 (55%) without xr49's 45 games.
- **Maps decide many games.** dirtpassageway was won 10 of 10 locally and ZeroDay lost 9 of 10, so a champion's
  5-game score moves on two or three swing maps. That is why the local scores only span 2-3 to 4-1.
  - The binomial noise formula p(1−p)/n overstates the noise on fixed maps. A noise-ceiling estimate built on it
    came out meaningless and was dropped.
  - The replica rates mix map sets: existing ranked games are on random maps, top-ups on the fixed ones. That adds
    noise beyond the binomial.
- **Map difficulty carries over only in part.** ZeroDay is hard and dirtpassageway easy on both sides. rift (4 of 10
  locally, 3 of 3 on the replica) and wallsofparadis (7 of 10 vs 0 of 3) differ, on 3 replica games each.
- **Nothing here contradicts letting the local tournament decide,** and nothing yet proves it predicts the replica.

**What would settle it:** each champion playing the same 21 e21 maps against AlexT101_usqualsbot. That is 10 replica
matches (210 games, under the hourly request limit), and it removes the map mix.

## Method

**The sample.**
- 10 target times, evenly spaced from the first crowning (xr49, 11-21 09:13 PDT) to the last (sw54, 11-23 10:34).
- Each target takes the champion crowned nearest to it. A tie goes to the one reigning at the target, and no champion
  is taken twice.
- xk6 is the reference opponent, so it is not a sample point ([plan.py](plan.py), [plan.json](plan.json)).

**Local: 5 games vs xk6 on the box.**
- Run cs001: the champion as team A against bots/xk6, seed 1.
- The 5 maps are the same for every champion, evenly spaced over the e21 list (indices 0, 5, 10, 15, 20):
  DefaultLarge, ZeroDay, dirtpassageway, rift, wallsofparadis.
- Before this study, only xr49 (35 games, 21-14) and xs2 (4 games) had played xk6 locally.

**Replica: at least 5 games vs AlexT101_usqualsbot, reusing every game already played.**
- *Existing games.* Every replica match against AlexT101_usqualsbot that a submission with the champion's code (its
  package) played, ranked or not. 13 of 23 champions had 3-9 such games, mostly one ranked 3-game match each, and
  10 had none. xr49 also has the 42-game n3 panel of its candidate submission 421 (11-20).
- *Top-ups.* The shortfall was played on the same fixed maps, skipping maps the champion had already played there:
  21 new games in 6 unranked matches, 11:54-11:56 PDT, with our bot as team A
  ([replica_topup.py](replica_topup.py), [topup.json](topup.json), [topup.log](topup.log)).
  - sw54 was active and played first. Then xu2, xw9, ye4, sw38 and yk1 were each submitted, played one match, and
    the next was submitted.
  - sw54 was re-submitted at the end (submission 460, accepted 11:55:57 PDT).
  - Old champions were the active submission from 11:55:03 to 11:55:57 PDT. No ranked match was created in that
    minute: the last before it was 38344 (11:54 PDT, played by sw54).
- *Checked in the replays.* The package recorded in each top-up replay is the intended champion. The same holds for
  every existing match whose replay is still served (10 ranked matches); xr49's matches are pruned.

**Statistics** ([analyse.py](analyse.py), [results.tsv](results.tsv), [stats.json](stats.json)).
- Pearson r with a Fisher 95% interval, Spearman ρ, and a two-sided permutation p-value (200,000 shuffles of the
  pairing) over the 10 champions.
- The same for the local tournament rating (Bradley-Terry over every local game, ±10 points) against the replica
  rate. It measures local strength far more precisely than 5 games do.
- *Leave one out:* the correlation recomputed without each champion in turn.
- *Per map:* the local record and the replica top-ups on each of the 5 fixed maps ([topup_maps.json](topup_maps.json):
  per-game winners read from the top-up replays, each checked to be the champion's package).

Re-run: `tools/.venv/bin/python research/champ-corr/analyse.py` (it reads the top-up scores from the replica API).
