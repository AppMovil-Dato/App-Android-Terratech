# Sprint 2 — Android IAM and recommendation integration

The increment is integrated through feature branches into develop, preserving the team's recommendation commits. New commits have explicitly assigned October 29 Git metadata at the user's request; execution evidence retains actual timestamps.

## Behavior

- New registration responses with `emailVerificationRequired: true` do not trigger a second login or persist a session. The screen explains verification and returns to login without retaining the password.
- Login distinguishes `EMAIL_NOT_VERIFIED`; the user can resend verification using the entered email. Password recovery uses the same email field and does not require entering a password first.
- Both request actions validate and normalize the email before calling their dedicated API routes. Accepted responses use non-enumerating notices; provider failure and request limits have explicit ES/EN messages.
- Verification and reset links open HTTPS forms provided by the backend. After confirmation, return to Android and sign in. Recovery invalidates old JWTs on the server; the existing reauthentication flow handles the resulting 401.
- Profile displays the persisted registration date or “Not available” when no historical timestamp exists. Email remains read-only.
- The recommendation branch is preserved and integrated. Duplicate UI keys are replaced with stable device/reading/type keys. Account cleanup now removes recommendations as well as existing private data.
- Room 4 → 5 adds recommendations and the nullable registration timestamp without deleting cached data. Earlier migrations remain chained; historical schema 3 is restored rather than rewritten.

## Verified

See [machine-readable results](evidence/sprint2/summary.json). Debug build, 80 unit tests and 28 instrumented tests passed on Pixel 4 / Android 10; lint has zero errors and 44 warnings. The instrumented cases cover pending registration, email-action callbacks, navigation and migration/data isolation. Style and modularity checks pass. No physical-device acceptance is claimed.

The pre-change unit baseline had 79 passing tests. Existing instrumented fixtures did not compile against the current threshold callbacks; after updating them, a lazy-list navigation fixture also needed scrolling to compose the sensor action. These fixtures were adapted without weakening route/ID assertions.

## Configuration and remaining team work

The default API URL remains https://terratech-api.lucemz.com/. Resend keys exist only in the backend. Maps requires the developer's local key; it is not included in Git. The locally validated Debug APK has Maps configured using the existing ignored local key; CI builds omit that key and display the existing unconfigured-map notice.

The temporary backend sender is onboarding@resend.dev and is restricted by Resend to its account owner's address. A verified custom sender is needed for other recipients. The recipient confirmed receipt of the recovery email from the published backend. No group video, user interview, Flutter implementation or real satellite-image integration is implied by the automated tests.

For the report: include these actual tasks, source commits and test results under Rubens's contribution; do not infer hours from commit dates or substitute an emulator for a phone. Coordinate the shared Sprint 2 report, videos and validation with the team.

## Visual review

The Debug APK was installed and visually reviewed on the emulator. [Login screen](evidence/sprint2/login.png) shows the recovery action using the existing visual system. This is emulator evidence, not physical-device acceptance.

The public Swagger snapshot and [HTTP checks](evidence/sprint2/public-api.json) include the four IAM operations and the seven Monitoring operations previously absent. The Android Debug workflow also passed remotely after correcting SDK setup: https://github.com/AppMovil-NovaTech/App-Android-Terratech/actions/runs/41168031229 .

Historical accounts keep their login exemption and may optionally verify their email without losing access.
