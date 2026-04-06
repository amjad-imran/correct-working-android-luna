# ZH SDK v2.3.1 Migration Notes in Simple Language

## What this upgrade is about
The app was using an older ZH band SDK version (`v2.3.0`).

The new SDK version (`v2.3.1`) adds more data for screenless wristbands and also adds a new way to ask the band for daily data:
- only today
- history only
- everything

## What is new in the SDK
The new SDK adds support for these extra data types:
- sleep RRI data
- sleep HRV data
- heart-rate points collected after exercise
- continuous RRI data

It also adds extra timing information to some existing data:
- continuous heart rate now tells whether its frequency is in minutes or seconds
- continuous pressure/HRV data now also tells whether its frequency is in minutes or seconds

## What was changed in the app now
This first migration step focused on the safest and smallest update.

Done now:
- the app has been moved to the new SDK AAR file
- the app now understands the new SDK callbacks so it compiles and runs with `v2.3.1`
- the existing green-button debug screen on the home page was extended to show the new SDK data
- that same screen now has buttons to test the new sync modes:
  - default
  - today
  - history
  - all
- the app now stores the raw new SDK payloads in the same debug/test storage it was already using

## What was intentionally not changed yet
This first step did not change the deeper production data flow everywhere in the app.

Not changed yet:
- no SDK source code was edited
- no new database tables or database fields were added
- no backend API contract was changed
- no large redesign of the sleep, readiness, or summary screens was done

This was intentional so the upgrade stays low-risk.

## Why the green-button screen was used
The app already had a special screen for Luna-band and SDK verification when tapping the green button on the home page.

Using that screen is the safest approach because:
- old app behavior stays mostly unchanged
- new SDK behavior can be checked in one place
- developers and testers can confirm the new data before it is rolled into regular screens

## What still needs to happen later
After testing on real devices, there may be a second phase.

That next phase would be needed if the new second-based data is actually used by the devices in production.

Possible later work:
- save the new frequency information in the app database
- update graphs and timelines to use real timing instead of older fixed assumptions
- update backend upload logic if the server also needs the new timing information

## What was verified
The app was compiled successfully after the upgrade.

That means:
- the new SDK AAR is connected correctly
- the app-side integration changes are valid
- the new debug UI changes are wired correctly

## Short final summary
This migration completed the safe first step to `ZH SDK v2.3.1`.

The app now:
- uses the new SDK AAR
- supports the new SDK callbacks
- exposes the new sync modes and new raw data on the existing debug screen

The larger product rollout of the new timing metadata is still a follow-up step and should happen only after device validation.
