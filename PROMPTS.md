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
