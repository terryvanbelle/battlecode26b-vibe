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
