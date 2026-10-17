# Existing-project upgrade: 0.2.0

This work upgrades the existing Kotlin/Compose application rather than creating a new repository or changing its package. Display branding is Hello Photography. `applicationId`, favorite pose IDs, `posemaster.db` schema version 1, and the legacy `preferences` file are retained. The upgrade adds a required `role` value without interpreting the old generic onboarding flag as a role; users choose once, retaining existing favorites and theme. New academy progress is stored separately in `academy`.

The free tier now allows 20 favorites. The database transaction permits edits to existing entries even when a legacy installation has more than 20; it does not delete old entries. Recently viewed guides are bounded to 12 IDs. Clearing data requires confirmation and cancels reminders before deleting Room rows, academy progress and preferences. Bundled content remains installed.

Original illustrations replace the former repeated placeholder figure. They show posture/category variation and label their origin. No archive/photo supplied in chat is imported, used as an artistic reference, bundled or uploaded to GitHub.

Daily inspiration is opt-in. Android 13+ permission is requested only after the user enables it. An inexact local alarm targets around 9am device-local time, with idle/battery delays possible. Boot/timezone changes reschedule only for an opted-in user. Notifications open an actual free bundled pose and use Amharic text when selected. No cloud push provider or exact-alarm privilege is needed.

Legal pages describe the current development build. Before publishing, supply hosted owner-specific policy/support documents, perform a release-device SDK/network audit, verify Data Safety and subscription disclosures, and review all translated content and curriculum. Feedback is shared through a user-selected Android app; no fake feedback API or Play rating link exists.

The checked-in debug APK is signed for development. Retain the same key to update an existing installation without losing data. A different key may trigger an installer conflict; never advise deleting user data before diagnosing that error. Production upload signing is separate; see RELEASE.md.
