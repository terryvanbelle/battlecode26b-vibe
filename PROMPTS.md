# Prompt record

Owner prompts for this project, in chronological order, recorded verbatim (spelling, punctuation and whitespace
preserved). Append-only. Each entry is `## <number>. <date> <time> PDT` followed by the prompt as typed. Prompts fired
by a `/loop` are not recorded.

## 1. 2026-10-27 21:50 PDT

We are going to build a world-class champion MIT Battlecode bot.  Battlecode is a contest where the contestants implement bots to play against other bots in an arena.  Each year’s rules are different from prior years, but they all share some common features.  We have built bots for several prior years already (Github repositories, in order of attempt:  battlecode22-vibe, battlecode26-vibe, battlecode25-vibe, battlecode21-vibe, battlecode20-vibe, battlecode24-vibe, battlecode23-vibe, and battlecode19-vibe.  Each attempt was built on previous attempts, so weight the findings of later projects higher than earlier ones).  Read through the code and documentation for these projects thoroughly to learn what has already been done, and what has worked or not worked.  Pay particular attention to files called RESEARCH.md, LEARNINGS.md, DESIGN.md, METHOD.md, and TRAINING_ALGORITHM.md.   Feel free to steal any code that might be useful to you.  The repository battlecode-vibe also has many files at the top level that will be helpful to you.  You are forbidden from reading post-mortems from the current year.  Post-mortems from any other year are fair game.

We’re not participating in an actual Battlecode tournament, we’re practicing.  In an actual tournament, you would have two sources of data:  local fights against old versions of yourself, and online scrimmages against a variety of opponents in the tournament standings.  We can’t perfectly replicate this latter source of data, but we should try to get as close as possible. 
Please do a thorough check of the web, especially github, for competitor bots from the relevant year that are publicly accessible.  Download all of them to serve as your benchmark.  You may not read their code, except to initially verify that they don’t pose any security risk, but you are encouraged to analyze their tactics in games.

Build a replica of the official Battlecode judging infrastructure using galaxy as a base.  You will use this infrastructure for your online scrimmages.  Enter your current competitor and the downloaded bots in the ladder and challenge bots slightly better and worse than you to scrimmages.  In the spare VM cycles, have random bots on the ladder do the same.  Ensure that none of this work has any effect on the official Battlecode infrastructure.

When you have thoroughly read all recommended repositories, formulate your own TRAINING_ALGORITHM.md file.  This file should be concise, complete, and formulated in year-agnostic terms.  Please do not simply copy a previous year’s TRAINING_ALGORITHM file.

You should start by building a strong, robust foundation in the basics:  good economy management; ensuring that your bots can move freely and efficiently to their destinations without getting stuck; board exploration; exploiting map symmetries; effective combat; and ensuring no bytecode overruns.  These basics are fundamental.  If you don’t have them right, nothing built on top of them will be effective.  Invest time at the beginning in building a good code architecture and good tools for understanding everything that happens in a game replay file.  Ensure that all moves your bot makes are symmetric when switching sides.  Generate graphs that illustrate your progress, and keep them up to date.  Write comprehensive unit tests for the bot and all tooling, keep them up to date, and run them after every change.

Make sure that your attempts are a good combination of incremental tweaks and big swings.  If you get stuck for ideas, review principles that have worked in other years.  There will be times when no attempts are successful for a long period.  At those times, it’s important to keep trying new things, and to not give up.  If you believe that a complete rewrite will help, then you should do so.  At no time should you stop and wait for me to give you a new idea.

Starting with this one, save all of my prompts in a document called PROMPTS.md.  Note that the user is in the PDT timezone.

This year we will compete again in Battlecode 2026.  Store all results in a new Github repository called battlecode26b-vibe.  Start from scratch, and do not look into or reuse anything from the original battlecode26-vibe, except that you may add the original battlecode26-vibe bot to your benchmark.  The purpose of this iteration is to see how much we’ve improved since the last time we tried 2026, so reusing any learnings from the previous 2026 effort would taint the findings.  Download the rules and begin.

## 2. 2026-10-28 06:15 PDT

I'd like you to structure  the progress/ directory like the one in battlecode23-vibe

## 3. 2026-10-28 06:28 PDT

Can you give me a summary of everything that's happened since I started you up last night?

## 4. 2026-10-28 06:35 PDT

elo.png doesn't look like battlecode23's elo.png

## 5. 2026-10-28 06:38 PDT

I just switched you from Medium effort to Ultracode effort.  I'd like you to mark this as a milestone on any graph with a time dimension, so we can track whether it affected performance

## 6. 2026-10-28 06:50 PDT

Your TRAINING_ALGORITHM looks a little thin.  I'd like you to model it after the one in battlecode19-vibe

## 7. 2026-10-28 07:44 PDT

progress/progress.png also doesn't look like it does in bc23

## 8. 2026-10-28 08:47 PDT

You don't need to put all the candidates on progress.png, just the accepted ones.  What bots are on the ladder panel?  Are they appropriately calibrated to our level?  Are you using the replica for all games against benchmark bots?

## 9. 2026-10-28 12:28 PDT

I'd like you to do an audit of ReplayDump.java, checking to ensure it's capable of extracting all available information, and that it's being used effectively

## 10. 2026-10-28 12:51 PDT

I'd like you to start a file called BEST_PRACTICES.md in the battlecode-vibe repository.  Start it off with a note that you should ensure that you have a replay dumper that is thorough and complete, and is being used effectively.

## 11. 2026-10-28 13:03 PDT

An addition to BEST_PRACTICES, point 1:  Often the replays strip out debug information like indicator strings.  To get around this problem, you can smuggle out some additional per-robot information in the low-order bits of the bytecode count by artificially adding some no-op steps

## 12. 2026-10-28 18:25 PDT

You're saying mines aren't symmetric?  They look pretty symmetric to me.  On which map are they not?

## 13. 2026-10-28 18:28 PDT

I want to dig into this further to make sure you're not engaging in faulty reasoning.  By what process did you come to the conclusion "Mines aren’t mirrored through symmetry. A raider that sees an enemy-side mine doesn’t tell us its twin on our side. I’ll add an off-by-default switch for that." ?

## 14. 2026-10-28 18:30 PDT

The court will ignore the witness's comment.  From now on, please assume that all maps are always symmetrical in all aspects

## 15. 2026-10-28 19:20 PDT

Ah, I see what you meant now, and yes, I did misunderstand.  I now understand that when you said "Mines aren’t mirrored through symmetry", you meant "Our bot isn't taking advantage of mine symmetry"

## 16. 2026-10-28 19:26 PDT

Please update progress/ after every acceptance of a new candidate

## 17. 2026-10-31 06:13 PDT

I'd like you to report our recent win rate against each of our panel opponents

## 18. 2026-10-31 06:16 PDT

It seems to me that some bots on our panel are too easy, and some are too hard.  I'd like you to recalibrate the panel

## 19. 2026-10-31 06:29 PDT

I guess the only bot on the panel that really raised my eyebrows was abcdezyxwv_bestsofar, which is far ahead of us on the replica, BT ratings, and as shown in matches.  And yet, I see a lot of work spent trying to beat it

## 20. 2026-10-31 06:32 PDT

Let's add that as a point to the BEST_PRACTICES doc in battlecode-vibe.  Spend your diagnostic time on losses against opponents in the 25-75% band

## 21. 2026-10-31 15:17 PDT

If you can get an advantage by breaking symmetry, then do so

## 22. 2026-11-01 19:20 PST

I can't access the replica.  I get the message "That episode was not found. Go Home?"

## 23. 2026-11-01 19:23 PST

Who said you couldn't prune /srv/bc26-galaxy?

## 24. 2026-11-03 10:55 PST

I notice our replica ratings have been exactly 1561 for the past couple of days.  Have we stopped running ranked games on the replica?

## 25. 2026-11-03 11:07 PST

Do BT ratings also need to be updated?

## 26. 2026-11-05 10:43 PST

Please continue

## 27. 2026-11-05 17:28 PST

Never, ever ask me for my permission to try something like this.  Never, ever stop trying new ideas

## 28. 2026-11-05 17:28 PST

You just wasted two hours sitting there when you could have been doing something useful

## 29. 2026-11-06 05:41 PST

I'd like you to re-read the top-level documents in the battlecode-vibe repository, making sure to filter out anything associated with 2026

## 30. 2026-11-11 05:39 PST

I think you might be stuck because you're too afraid to make a mistake, and so you're stuck at a local maximum.  What can we do to change that?  My recommendation would be for you to look for ideas that seem like good ideas, even if they don't pan out in the panel evaluation.  I'm open to ideas from you as well.  It's OK with me if you get worse, if by getting worse you get to a state where you can potentially get better than you are currently

## 31. 2026-11-11 05:45 PST

High level idea:  If one or more opponents uses a very effective tactic against us, then it's worth some short-term pain to learn how to use that tactic ourselves.  I'd be OK if you temporarily got worse in the process of learning it.

## 32. 2026-11-12 07:42 PST

You still seem to be spinning your wheels.  I see a lot of analysis, but no launches.  I'd like you to focus on one problem with your bot behavior that's common among panel games and that's leading to losses.  I'd like you to focus on fixing that behavior until you start winning those games.  It's OK with me if doing so incurs losses elsewhere.  It's OK with me if you launch something that's slightly worse than the current champion.  We need to break out of this local maximum.

## 33. 2026-11-12 09:47 PST

OK, shutdown time.  I'd like you to bring down the replica and all running processes on the VM.  Update LEARNINGS, push all open state to Github, delete local files.

## 34. 2026-11-18 13:20 PST

I'm starting you up again to continue you work after the shutdown.  Before continuing, I'd like you to read docs/bc26a0-lessons.md, which is a doc of advice placed there by another project you and I worked on in the interim.  Follow anything you think useful.  You should know that we figured out a way to hook up my Windows box for extra compute.  Look into the repository terryvanbelle/battlecode26-alpha0 for details, and I can also help you hook into it

(mid-turn) bc26a0 is shut down for now

(mid-turn) Remember that you're only allowed to play panel games through the replica, which you'll need to start up again

## 35. 2026-11-18 13:40 PST

Don't play panel matches on the box, for the sake of realism.  In an actual tournament, you will be heavily constrained by how many games you can play on the actual environment, and we want to replicate that experience

(mid-turn, 13:45) I would like you to find ways to use the box as much as possible, given that constraint

## 36. 2026-11-18 14:00 PST

(13:55) Have you fixed all the bugs?

(13:58, on the box's WSL memory cap) Yeah, I lowered that because a higher limit (20G) was thrashing the box.  I can try raising it a bit more

I'd like your initial focus to be fixing all bugs, writing more tests, and then refactoring the code to make it as clean and simple as possible so that bugs can't hide.  After that, I'd like to see where we're at against the panel

(14:20) Once you've completed all your bugfixes, make sure that you remove the bug flags and dead code that results.  I understand why you want the flags while you're establishing that the bugfixes are effective, but after that it's just clutter

(15:18, after an audit verifier loaded all 74 maps to count mines) You can use all the maps

(15:20) Treat K6/E1 as a feature to consider after you've finished the debugging and cleanup and initial eval

(19:14) Sounds good!  After the panel run, don't forget to update all the graphs in the progress/ directory

(22:02) Sounds good.  Once you're done, you should be in a good position to improve on a bot that is more or less bug-free, and clean.  Before you start improving it, I'd like you to re-read all the top-level documents in the battlecode-vibe repo and then your LEARNINGS doc.  The overall goal is to climb up the list of bots on the replica
(asked which repo) https://github.com/terryvanbelle/battlecode-vibe
(22:04) MISSED_IDEAS has its specifics labeled by year, so you can filter that one too
(22:09) It's quite possible that some of your previous ideas were good ideas, but didn't work because they were sabotaged by bugs you've now fixed
(23:23) Can you remove any candidate from the ELO graphs whose rating wasn't obtained via a panel?
(06:29) It doesn't look like you accomplished much overnight.  What did you work on?
(06:29) I want you to put together a script that periodically prods you to continue working.  It's not acceptable for you to sit idle for 7 hours
(06:29) Also, please report all times as PDT
(06:33) PDT, not PST.  I live in British Columbia
(08:27) Take a look at the VM disk and see if it has any leftover files from other years.  You can clean those out.  You can also clean out any leftover files from battlecode26-alpha0
(08:29) Approve deleting bc23-galaxy directories and loose home directory files before October
(10:58) Any bot that ties the current champion on the panel should be accepted
(16:37) I'd like you to add an item to terryvanbelle/battlecode-vibe/BEST_PRACTICES to the effect that a tie on the panel versus the current champion is sufficient to make it the new champion.  There was a reason why the tying challenger was given a panel run, and that evidence is sufficient to tip the balance
(18:56) I'm going to need the box for a few hours, so I'm shutting down the Ubuntu.  I'll bring it back up when I'm done
(18:57) Done
(20:58) OK, you can have the box again
(21:39) Can you give me a scatterplot for local advantage vs. panel advantage?
(21:43) Maybe archetype scores correlate better with panel scores?
(21:48) OK, that's fine, carry on
(06:14) Good morning!  How did last night go?
(06:45) Do you have enough data to build a basic model from the archetypes to predict panel performance?  I wonder if there are any other archetypes that might help there.  If we had such a model, we'd be able to put the box compute to good use vetting candidates.  Though it sounds like the panel hasn't been a bottleneck recently
(06:51) Yeah, that sounds like a good idea to me.  Two things jump out at me:  First, if panel scores are noisy, then that means we can't always trust a panel score to decide our champion either.  What we're really optimizing is performance against every opponent above us on every board, but we don't have enough data for that and we can't get it all, or at least not quickly.  Second:  We should really find more ways to exploit our compute situation, where we have abundant  local compute, and far more limited panel compute
(06:55) I think all three ideas are excellent.  I'd be thrilled if we could find a way to approximate our ultimate goal with local games
(07:20) I'm not surprised that you found the panel games deterministic.  We're deterministic, so the only other source of randomness would come from opponents, which isn't completely out of the question, but I would suspect that most of the top players are deterministic for the same reason that we are
(07:29) Just want to double check that things are still happening on the VM and box, and that you haven't stopped because of this discussion
(07:32) In an actual contest, this problem will be alleviated somewhat because usually only a small subset of the overall map set is available to contestants until the sprints.  So we'll have no choice but to optimize on a smaller map set.  Are you able to figure out which maps were initially available in 2026?
(07:37) Let's restrict ourselves to the 21 maps and see if that makes things more tractable.  Can you also restrict the replica to only allowing those maps?
(08:34) I'm in favor of replacing our panel with the bots that are above us in the rankings, with one caveat:  In the past you've sometimes started to obsess with analyzing games against bots far above us in the rankings, which wasn't productive.  So by all means evaluate us against bots above us, but for getting ideas, stick to bots in the 25% - 75% win range
(10:12) Maybe once we have a challenger to xk6, we can interleave 40-run batches between the incumbent and the challenger
(14:40) This is going to take far too long to evaluate each possible candidate.  How about we choose, say, the three bots immediately above us, and focus just on them?
(14:45) Can we get it down to 40 matches per build?  That way we can do it all in one batch
(06:14) Good morning!  How did last night go?
(06:14) I'd still like to find a more reliable way to predict panel success based on local success.  Do you have enough data to evaluate the archetypes we have as predictors, and maybe add a few more?  You can use averaged multiple seeds on panels to smooth out the target function if you like.  Until we've solved this problem, it feels like we're just basically guessing when we submit a bot to the panel
(06:14) To keep things tractable, the target remains the three opponent bots in the current panel
(06:14) Perhaps there are metrics obtained from the local games that would be good inputs to the prediction function
(06:23) I feel like the success of reinforcement learning as a general problem solving technique (e.g. AlphaGo) suggests that self-play can provide a useful signal.  Agreed that archetypes aren't providing necessary signal.  Do wins against, say, xi9 provide any signal?
(06:27) That approach sounds good to me, and tracks what most contestants do in actual contests.  I supported exploring the idea of archetypes, because it increased the diversity of data, but perhaps old school is best.  I'll be especially interested to see the data on this one
(06:30) Sounds good.  What I most want is your assessment of whether data from playing against previous champions can serve as a good predictor of panel success, as you did with the archetypes
(06:31) For other champions, you'll also want to check how correlated their contributions are to xi9.  High correlation == less signal
(06:54) Go ahead and make xk6 champion again.  In the future, I authorize you to make decisions like these.  I'll offer my opinion when I want a change from current procedure
(08:18) OK, clearly this isn't working, and we need to try something new.  If local performance doesn't predict panel performance, then adding the panel gate is just slowing down local space search.  Here's what I propose:  We're going to partially decouple local search from the replica.  We're going to run a continuous local tournament just on versions of our own bots, using Bradley-Terry to rank them.  Whenever a new local bot hits first place, we submit it to the replica without any gating.  We observe replica games to get new ideas to try in the local tournament.  All local modifications get applied to the local champion.  Use replica challenges judiciously to get maximum signal for new ideas
(08:21) I predict that this approach will sometimes lead to worse scores on the replica, but that it's necessary to traverse these valleys in the fitness landscape in order to reach the global maximum
(08:26) At each prod, I'd like a report of recent ideas tried, whether any new local bot took the top spot, and our current position in the ladder
(08:28) Actually, instead of just top spot, I'd like to see the top 5 in the local tournament, with any new champions noted
(10:43) Something's gone wrong with the replica.  Can you fix it?
(10:43) I'm good with that  [hourly low-disk replay prune]
(10:44) I'd like you to add a report of free VM space to the prod
(12:32) For each challenger, can you also show point difference from the champion, as well as points necessary to replace them?
(18:33) Please add graphs to progress/ for local ELO whenever we have a new champion.  Also graphs for replica ELO and position on the ladder.  Use wall clock time for the horizontal axis for all of them
(18:34) Make sure to keep these graphs up to date
(05:54) Good morning!  I see from the graphs that you've improved a lot on the local and replica ELO, and no improvement on the ranking.  I'm pretty happy with that so far.  I suspect that there's a large cliff in the fitness landscape between us and the top bots, and we're slowly climbing up it
(05:57) Yes, please do.  And let's get rid of the orange vertical bars, they're not really contributing
(06:03) Actually, instead of using the past 24h, can you just hard code the start of the more recent graphs to 8am Nov 21?  That's about when we started this new approach
(06:04) And can you add the ELO of the 10th, 15th and 20th place replica bots, for reference?
(06:09) Is it just me, or does it look like the 20th place ELO has been rising over time?
(06:14) I see, so basically those of us above the median are cannibalizing the ratings from those bots below the median, but the higher the bot goes the more successful it is at that
(06:23) In an actual tournament, are there any structural advantages to coming out on top early?  I think one of your advantages will be that you can get a competent bot on the boards much more quickly than a human can
(06:29) OK, you say that the human advantage is in new strategies.  I'm throwing that down as a challenge to you.  Can you start adding some big swings to the local tournament, instead of just incremental tunings?  Because we can play so many local games, it's low stakes to try something that's high-risk high-reward
(06:35) I think this is a great idea.  Can you also distinguish between big swings and incremental changes in your candidate naming convention?
(06:47) Looking at our last 7 submissions, I noticed they've all been submitted at a time that ends with a '6'.  This seems like it would be a big coincidence if it were random.  Are you doing something different now?
(06:48) Ah, it's the prod.  No worries, carry on
(06:49) Are we keeping the VM busy with the replica runs as well?
(06:51) No, I think I'd like to keep with our existing approach
(06:53) My thinking had been that if the VM was sitting idle for long stretches, I'd ask if you could run some local tournament matches on it to expand our local search.  But running the replica's ranked contests is a worthy use of the VM, so let's keep things as-is
(08:51) Can you add the size of the local tournament to the prod messages?
(09:12) Let's aim for about 50% incremental new ideas and 50% big swings in the local tournament additions.  Let's also aim for roughly 10 bots in the tournament
(09:45) I really like this methodology.  Can you update TRAINING_ALGORITHM so that it reflects the way that we do things now?  No need to preserve the original wording of the doc
(10:44) In the prod, is the top-5 table just a prefix of the Recent ideas tried table, potentially with the champion?  If so, it's not really serving any purpose, and can be removed
(18:59) A while back you mentioned that we have a blind spot, in that we can't learn defences against tactics that we don't run locally.  That's true, but you can always adopt the tactic that an opponent uses against us, and that should win locally.  Then you can work on learning a defence against it, right?
(19:03) Can you add another subgraph to rating-history.png?  I'd like to see our replica ELO minus the replica ELO's of the 10th, 15th, and 20th place bots
(19:31) Please set the minimum ELO on the top panel of rating-history.png to 1400
(06:31) Good morning!  What happened overnight?
(06:35) What happened to our categories?  Yesterday we had "incremental" vs. "swing".  Now I see "small" vs. "sparring"
(06:49) Something is still wrong with your sparring bots, since they're worse than the champion, and yet the bots they imitate beat our champion.  Can you see if you can figure out the behavioral difference that explains this discrepancy?
(07:18) Can you also do a bug audit on yk1?  If you find any bugs, fix them and add the fixed version to the local tournament.  Also make sure the unit tests are current
(08:48) OK, good.  I'd like you to add a clause to the prod telling it to check for bugs and test compliance/coverage whenever we pick a new champion
(09:41) Can you do something about the labels in local-elo.png?  They're reducing the amount of horizontal space available for the graph itself
(10:06) I'm not sure we need a separate sparring category in the local tournament.  For example, now that sw53 is champion, we've fixed that blind spot for good, since all future bots will descend from it
(10:25) Can you update TRAINING_ALGORITHM to cover our bugfix procedure for new champions?
(11:48) Can you do a scatterplot and correlation between local champions and on the panel?  For each champion, let's compare win rate vs. xk6 versus win rate vs. AlexT101_usqualsbot.  Don't do all champions, let's say a sample of 10, evenly spaced over time, with 5 games each opponent
(11:51) Do you not already have their win record vs. AlexT101_usqualsbot?
(12:45) That scatterplot gave better results than I was hoping for.  The last time we looked at this, we got a small negative correlation.  I think it's worth firming up the game count to get a more accurate picture
(13:24) If you need to tweak the replica's hourly game limits a bit to squeeze in a few more games for the scatter plot, then you have my permission to do so
(14:04) Please put the scatterplot in the progress/ directory
(14:33) Hmmm, not bad.  Can you do a fit model to map local Elo to replica Elo?  If you can do that, then we should be able to predict how long it will take for our local tournament to produce a model that ranks 15th, or 10th, or 1st
(19:48) I think maybe the bug checks are overkill.  We only need an incremental bug check for the new code added for the current champion version
(06:49) Can you take the milestone markers off of rating-history.png?
(12:02) Can I see the normal output from a prod, e.g. the tournament bots
(12:14) I notice the tournament size dropping below 10 recently.  Are you having difficulty finding new ideas to replenish it?
(12:16) Also, I see you referring to an "8-game check against sw71".  What is this referring to?
(12:17) OK, so it's only used to catch crashes and bytecode-limits?  I don't like the idea of adding an extra quality-based gate to the local tournament
(12:19) It's fine if you want to avoid wasting time on crashes, but don't take it beyond that
(12:19) If you're having difficulty finding ideas, I'd like you to re-read the top-level documents in the github repository terryvanbelle/battlecode-vibe
(13:22) This is insufficient output from a prod.  I want the same level of detail on every prod that  you were giving me this morning
(13:26) The last full report came over an hour ago, so don't use that as an excuse
(15:08) I just got an email about a security vulnerability in Gradle.  Can you check whether we're susceptible?
(17:36) Can you add Elo for #5 to rating-history.md?
