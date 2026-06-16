# BlankTestFragment Update Timing Explainer

This document explains exactly:

- when data in `BlankTestFragment` updates
- what updates automatically
- what updates only after a manual action
- whether auto sync updates the raw v2.3.1 panels
- how much raw data is shown in preview vs on demand
- how “latest” is chosen in the UI

This is the missing timing/refresh explanation.

---

## 1. Short answer

The most important thing to understand is that `BlankTestFragment` has **two different update mechanisms**:

1. **Automatic app-open sync**
   - when the fragment opens, it triggers a normal sync automatically
   - for `LUNA_BAND`, that normal sync uses SDK `ALL` mode in production

2. **Manual raw-capture session**
   - when you tap `DEFAULT`, `TODAY`, `HISTORY`, or `ALL`, the fragment starts a raw-capture session and clears the previous raw captured payloads for the v2.3.1 debug cards
   - only during that active capture session are the new raw v2.3.1 payload callbacks stored into the raw debug slots

So:

- **auto sync can update production data**
- **manual mode buttons are what make the new raw v2.3.1 payload cards refresh with a fresh captured session**

That is the main distinction.

---

## 2. What happens when the fragment opens

When `BlankTestFragment` is opened, `onViewCreated()` runs and does these relevant things:

1. calls `triggerSync()`
2. starts the dashboard polling loop
3. binds the raw payload viewer cards

### Important consequence

The fragment **does automatically trigger a sync on open**.

But that open-triggered sync **does not start a new raw SDK capture session** for the new v2.3.1 raw cards.

That means:

- the app may still sync real device data in the background
- but the raw v2.3.1 debug panels will not start a fresh session automatically just because the fragment opened

So if you are expecting:

"I opened the fragment, the app synced, why didn’t the raw v2.3.1 panel show fresh session-tagged data?"

The answer is:

- because the auto-open sync does not call `testStartSdkRawCaptureSession(...)`
- only the manual mode buttons do that

---

## 3. Two categories of data shown on the screen

To understand refresh timing, it helps to split the fragment UI into two categories.

## 3.1 Category A: dashboard values that simply read stored data

These parts of the fragment repeatedly poll stored data and refresh the UI:

- battery values
- remaining battery time
- fully charged time
- workout details
- body battery chart
- respiratory chart
- raw preview cards
- raw metadata summary cards

These are driven by the dashboard loop, which runs every **2 seconds** while the fragment view is alive.

So the screen itself refreshes every 2 seconds by reading the latest value currently stored in `WatchDataStore`.

This does **not** mean the device is sending new data every 2 seconds.

It means:

- the UI checks storage every 2 seconds
- if stored values changed, the screen updates

## 3.2 Category B: raw v2.3.1 capture-session panels

These include cards like:

- continuous HR raw
- continuous pressure raw
- sleep RRI raw
- sleep HRV raw
- continuous RRI raw
- post-workout heart-rate raw
- dev sport raw

These do **not** update merely because the fragment is polling.

They update only if:

1. the app has stored a raw payload into the corresponding store slot
2. and for most of these new v2.3.1 cards, storage only happens while a raw-capture session is active

So the polling loop only reflects what is already stored. It does not create the capture.

---

## 4. What starts a fresh raw-capture session

A fresh raw-capture session starts only when you tap one of these manual buttons:

- `DEFAULT`
- `TODAY`
- `HISTORY`
- `ALL`

When one of these is tapped, the fragment:

1. updates the sync status text
2. clears previous raw-capture keys for the v2.3.1 session-based payloads
3. stores a new capture session with:
   - session id
   - start time
   - expiry time
   - mode label
4. sends the chosen manual sync request

### Important result

If you want the raw v2.3.1 panels to reflect a **fresh, known sync session**, you should use the manual mode buttons.

That is the intended validation flow.

---

## 5. How long a manual raw-capture session stays active

The raw-capture session remains active for **10 minutes**.

That means:

- callbacks received within that 10-minute window are appended to the current session envelope
- callbacks received after that window are ignored by the session-based raw capture path

So “latest raw capture” in these debug cards means:

- latest callback captured during the currently active, not-yet-expired manual session

If no active session exists:

- the new callbacks are not appended to those session-based raw envelopes

---

## 6. Does auto sync update the raw v2.3.1 panels?

## 6.1 For the new v2.3.1 session-based raw panels

Usually: **not by itself**

Reason:

- the auto-open sync does not start `testStartSdkRawCaptureSession(...)`
- the v2.3.1 raw session-based save methods require an active capture session

So for cards like:

- continuous HR raw
- continuous pressure raw
- sleep RRI raw
- sleep HRV raw
- continuous RRI raw
- post-workout HR raw
- dev sport raw

the answer is:

- **they are meant to be refreshed through a manual capture session**

## 6.2 What you may still see after auto sync

You may still see old data remain on the cards if:

- a previous manual session had already stored payloads
- the fragment is just reading those stored values again

That does **not** necessarily mean the auto sync produced new raw captures.

It may just be showing the last stored session data.

So if you want to be sure the raw panel is showing data from *this* test run:

- start a manual session
- note the mode and start time shown in the card/bottom sheet

---

## 7. Does manual sync update the raw v2.3.1 panels?

Yes, this is the main intended path.

When you press:

- `DEFAULT`
- `TODAY`
- `HISTORY`
- `ALL`

the fragment:

1. clears the previous raw session values
2. starts a new capture session
3. sends the corresponding SDK request
4. the incoming callbacks are stored into the raw envelope
5. the dashboard loop notices the store changed and refreshes the preview cards within about 2 seconds

So the full timing is:

- manual button pressed
- SDK callbacks arrive asynchronously
- payloads are saved
- UI reflects them on the next polling cycle

---

## 8. Does the fragment update automatically after data is stored?

Yes, but in a very specific sense.

The fragment has a background loop that:

- keeps running while the view is alive
- rereads the relevant values every **2 seconds**

So if the store changes, the fragment usually shows the updated data within about 2 seconds.

That means the fragment **does automatically redraw from stored data**.

But it does **not** mean every category of stored data is automatically being refreshed by the device or sync layer.

You have to separate:

- **UI polling**
- from **actual data capture**

The UI polling is automatic.
The raw v2.3.1 capture session is manual.

---

## 9. What updates even without pressing a manual button

The following can still update when the fragment opens or when other app flows store fresh values:

- battery-related values
- workout values
- body battery chart
- respiratory chart
- any other store-backed values that are written from non-session-based callbacks or other app flows

These are not dependent on the v2.3.1 raw-capture session.

So if your question is:

"Can some parts of BlankTestFragment update automatically even if I never press the manual sync buttons?"

The answer is:

- **yes**, many non-session-based dashboard values can

If your question is:

"Will the new raw v2.3.1 payload cards definitely capture fresh callback payloads without pressing a manual button?"

The answer is:

- **no**, not reliably, because those depend on the manual capture session

---

## 10. Which parts are manual-session-based vs always-stored

## 10.1 Manual-session-based raw payloads

These are cleared and recaptured through the manual mode buttons:

- dev sport raw
- continuous heart rate raw
- continuous pressure raw
- sleep RRI raw
- sleep HRV raw
- continuous RRI raw
- post-workout heart-rate raw

These are tied to the session window and the selected sync mode label.

## 10.2 Not using the same session gating

These are stored directly and are not dependent on the same session envelope logic:

- body battery raw/chart data
- respiratory raw/chart data
- some battery/workout/debug values

So the fragment is really a mix of:

- session-based raw capture panels
- always-readable store-backed panels

---

## 11. How much data is shown directly on the fragment

The raw preview cards in the main fragment show only a **bounded preview**.

For the v2.3.1 raw cards, the preview size is:

- **300 characters max**

The preview card also shows useful metadata, such as:

- number of captures
- number of valid captures
- session label like `TODAY`, `HISTORY`, `ALL`, or `DEFAULT`
- whether the shown payload is the latest valid one or latest captured one
- capture timestamp

So the fragment intentionally does **not** show the full raw JSON inline.

That is how it avoids UI overload and ANR risk.

---

## 12. How much data is shown on demand

When you tap a raw card, the app opens `RawSdkPayloadBottomSheet`.

That bottom sheet shows:

- session info
- mode
- started time
- updated time
- captured callback count
- valid callback count
- selected payload time
- latest callback time
- pretty-printed selected payload
- optional capture log for multiple callbacks

The bottom sheet does **not** truncate to 300 chars like the dashboard preview.

Instead:

- it builds the full snapshot text
- then paginates it into chunks of about **3500 characters per page**

So:

- preview on main screen = small and safe
- on-demand full view = large, paged, detailed

---

## 13. Is the on-demand payload the latest payload?

Not always the literal latest callback.

This is very important.

For payload types that have validity rules, the resolver chooses:

- **latest valid payload**, if one exists
- otherwise **latest captured payload**

Meaning:

- if the newest callback is zero-only
- but a slightly older callback in the same session had real data
- then the preview and the selected on-demand payload will show the **latest valid** one

This is intentional.

The UI is trying to show the most useful payload, not just the most recent empty shell.

### But does the bottom sheet still tell me what the actual latest callback was?

Yes.

The full snapshot includes:

- selected payload timestamp
- latest callback timestamp
- capture log

So you can still tell whether:

- the selected payload was the latest valid one
- while a newer callback existed but was zero-only

---

## 14. What does “latest” mean on the preview card

For the preview cards:

- if there is at least one valid capture in the session, the preview says `Showing latest valid payload`
- otherwise it says `Showing latest captured payload`

So the preview is biased toward usefulness.

This is why sometimes the preview may show a payload that is not the literal last callback received.

That is by design.

---

## 15. What happens if data gets updated elsewhere in the app

If some other part of the app updates store-backed values, then the fragment can reflect that on the next polling cycle.

But for the session-based raw v2.3.1 panels, store updates only happen when:

- an active raw-capture session exists
- and a matching callback arrives

So:

- general app sync/storage changes can update many dashboard elements
- but they do **not** automatically guarantee new raw v2.3.1 session payloads are being appended

---

## 16. What happens if I never press refresh in the bottom sheet

The bottom sheet loads its snapshot:

- once when it opens
- again only when you manually press `Refresh snapshot`

So the bottom sheet itself is **not continuously auto-refreshing**.

The main fragment polls every 2 seconds, but the opened bottom sheet does not keep rebuilding itself in the background.

If new captures arrive after the bottom sheet is already open:

- you need to press refresh in the bottom sheet to re-read the latest stored snapshot

So:

- fragment cards auto-refresh from store every 2 seconds
- bottom sheet snapshot refresh is manual

---

## 17. Practical rule-of-thumb matrix

| UI part | Updates automatically when fragment is open? | Needs manual sync button? | Notes |
|---|---|---|---|
| Battery/workout/store-backed dashboard values | Yes, polled every ~2 sec | No | Updates if underlying store changes |
| Body battery chart | Yes, polled every ~2 sec | No | Depends on body battery store value being updated elsewhere |
| Respiratory chart | Yes, polled every ~2 sec | No | Depends on respiratory raw/store being updated |
| v2.3.1 raw preview cards | Yes, redraw every ~2 sec from store | Yes, for fresh session-based capture | They show stored capture data, not necessarily auto-captured data |
| v2.3.1 metadata summary cards | Yes, redraw every ~2 sec from store | Yes, for fresh session-based capture | Same as above |
| Raw bottom sheet content | No continuous auto-refresh | No, but manual refresh button needed for new content | Loads on open, then refreshes only on demand |

---

## 18. If I want fresh raw data, what should I do?

Use this sequence:

1. Open `BlankTestFragment`
2. Tap the specific mode you want:
   - `DEFAULT`
   - `TODAY`
   - `HISTORY`
   - `ALL`
3. Wait for callbacks to arrive
4. Let the main fragment refresh naturally within about 2 seconds
5. Tap the raw card if you want the full payload
6. If the bottom sheet is already open and new callbacks come later, press `Refresh snapshot`

That is the cleanest way to ensure:

- you know which mode produced the data
- old raw captures were cleared first
- the payload timestamps belong to the current test session

---

## 19. If I rely only on auto sync, what can go wrong in understanding the UI?

You can easily misread the raw cards if you rely only on auto sync, because:

- the app-open sync does happen
- but the raw session-based panels may still show old stored session data
- or no new captured session data at all

So auto sync is good for:

- normal app behavior
- production data refresh behavior

Manual sync buttons are better for:

- raw payload verification
- mode-specific testing
- proving exactly what the SDK returned in a known session

---

## 20. Best verbal explanation to give someone

If you need to explain this quickly, say:

The fragment refreshes its dashboard from local store values every two seconds, so the screen itself is always polling. But the new raw v2.3.1 payload cards are session-based debug capture panels. They do not start a fresh capture just because the fragment opens. A fresh raw capture begins only when I tap one of the manual sync mode buttons, which clears the previous raw session and starts a new 10-minute capture window. The preview cards show only a 300-character preview plus metadata, while the full raw payload is opened on demand in a paged bottom sheet. For payload selection, the UI prefers the latest valid payload, not just the latest callback, because the latest callback can be zero-only.

---

## 21. Final takeaway

The core truth is:

- **the fragment UI auto-refreshes**
- **the new raw v2.3.1 capture is manual-session-based**
- **preview is small and automatic**
- **full raw is on demand and manually refreshed**
- **“latest shown” often means latest valid, not necessarily literal latest callback**

If you remember those five rules, the whole fragment behavior becomes much easier to explain.
