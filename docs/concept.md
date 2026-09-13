# Concept

## Problem

Training volume, counted as hard sets per muscle group per week, is the single
variable most lifting programmes are built around. Most logging apps record
individual workouts and exercises well, but they answer the question "what did
I lift today" rather than "which muscle groups are still short of their weekly
range". Answering the second question with a general purpose app means adding
up sets by hand at the end of the week.

## Goal of the app

The user defines a weekly target range of hard sets per muscle group, logs
their sets as they train, and sees at any point in the week which muscle
groups are below, inside or above that range. The app also flags when an
accumulation block has run long enough that a deload week is due.

## Users

A single user who trains with weights several times a week and follows a
structured programme. There is no account, no sharing and no server. The app
is used on one device.

## The challenge the app supports

Reaching and holding a weekly set range per muscle group across an
accumulation block, then deloading before fatigue accumulates further. The
target range is the user's own, the app does not prescribe it.

## Data recorded

Per logged entry:

- date
- exercise name
- the muscle group the exercise trains directly
- the muscle groups it trains indirectly
- number of hard sets, repetitions per set and load in kilograms
- reps in reserve, optional

Per goal:

- mesocycle phase, bulk, maintenance or cut
- number of accumulation weeks before a deload is due
- one weekly set range per tracked muscle group

## How the information is evaluated

1. Entries are assigned to the training week that contains their date. A
   training week always starts on Monday, so weeks are comparable.
2. Each set counts once for the muscle group it trains directly and half for
   each muscle group it trains indirectly. Counting indirect work at a
   fraction avoids both ignoring it and double counting it.
3. The accumulated fractional sets per muscle group are compared against the
   target range: below the lower bound, inside the range, or above the upper
   bound.
4. Progress towards the lower bound is expressed as a ratio between zero and
   one, which drives the progress indicators. The overall weekly figure is the
   mean of those ratios across the tracked muscle groups.
5. A week counts as a deload when its total volume drops to at most half of
   the trailing average of the preceding weeks. Consecutive weeks that are not
   deloads are counted as the current accumulation block, and once that block
   reaches the configured length the app reports that a deload is due.

## Screens

- Dashboard, the current week, overall progress, deload advice and the
  breakdown per muscle group.
- Goal, the phase, block length and weekly set ranges.
- Log sets, the entry form for one block of straight sets.
- History, past weeks with their total volume and deload markers.
- Week detail, the breakdown of one selected past week.

## Out of scope

Exercise database with prescribed movements, rest timers, cardio, nutrition,
one rep max estimation, cloud sync, multi user support, wearable integration.
These are listed so that the report can argue the scope was bounded on
purpose rather than left unfinished.
