# Play disclosures worksheet — Hello Photography

Prepared for the **new** Google Play application `com.saymii.hellophotography`, version `1.0.1` / code `4`. The owner is **Simon Solomon**; public support/privacy contact: **simonsolomon436@gmail.com**. This is a completion and verification worksheet, not a submitted declaration or legal approval. Fill the Console for the exact signed release and actual services, not a debug build or a future feature plan.

## Current release design

- Photographers, Models, and Everyone roles, preferences, Room favorites/collections, recently viewed guides, and academy progress are device-local.
- Pose recommendations run through offline rules. There is no configured AI API, user-photo upload, cloud sign-in, remote catalog, publisher analytics, or cloud synchronization.
- The app bundles 39 owner-supplied AI-generated posing references for offline reading. They are static app assets; it does not access the user's photo library or send photos to an AI model.
- Notifications are optional local reminders. Android 13+ permission is requested on opt-in. No FCM/push service is configured.
- Pose text and feedback use Android's share chooser. Nothing is automatically posted to a publisher service. A receiving app and external GitHub/browser links have their own processing.
- The Play release excludes AdMob and UMP SDKs. Debug/test components are not evidence about the Play release; audit the release dependency graph and merged manifest.
- Billing catalog scaffolding remains. Empty product IDs do not enable real purchases. The purchase and entitlement backend is not connected. All 39 AI photo guides and 48 original sample posing guides are free to read, including four premium-labeled collection previews. Advanced Academy courses show syllabus previews, while their premium lessons remain locked. A release with configured catalog queries must also assess the actual Play SDK behavior.
- Android backup is disabled and backup/transfer exclusion rules are provided. In-app deletion clears app-managed local data and reminders. There is no cloud account to delete.

## Evidence required before submitting answers

Record the AAB SHA-256, version code, build date, exact dependencies, test device/API, and auditor in the release record. Never record tokens, purchase credentials, or raw personal traffic.

1. Run `./gradlew :app:dependencies --configuration releaseRuntimeClasspath`. Confirm no `play-services-ads`, Ads Lite, mediation, UMP, Firebase Analytics, Crashlytics, or unapproved network SDK is packaged.
2. Inspect `app/build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml` (AGP output location can vary; search `app/build/intermediates` if needed) and `bundletool dump manifest --bundle=app/build/outputs/bundle/release/app-release.aab --module=base`. Confirm no advertising-ID permission, ad provider, camera, microphone, location, contacts, or photo/media access appears unexpectedly. `INTERNET`, `BILLING`, local-notification/boot permissions may be expected; inspect the actual artifact.
3. Inspect startup, onboarding, all five tabs, search, assistant, favorites, lessons, notifications, and premium screens on a Play-installed release. Use a trusted device network profiler with normal TLS verification to establish which destinations are contacted and what information leaves the device. Absence of visible ads is not a traffic audit.
4. Review the current [Play Data Safety instructions](https://support.google.com/googleplay/android-developer/answer/10787469) and each included SDK's disclosure documentation. SDK changes can change answers.
5. Confirm the hosted policy names Hello Photography and Simon Solomon, gives a monitored private contact, describes actual practices, and is reachable without sign-in, geographic blocking, or downloads. Enter the same URL in the app configuration and Console. A source file path is not a hosted policy.
6. Keep a record of what was tested and any residual uncertainty. Reassess for every build that changes SDKs, accounts, purchases, ads, support submission, or cloud services.

## Candidate Data Safety answers

Only use these candidate answers after the evidence above supports them. "On-device only" means no off-device transmission; it is not a blanket exemption for SDK collection.

| Console topic | Candidate answer / action for the current release |
| --- | --- |
| Does your app collect or share any required user data types? | **No only if** the exact release audit confirms that neither app nor SDK sends reportable data off-device. Otherwise answer **Yes** and disclose each actual data type, purpose, sharing recipient, optionality, and processing. Do not guess. |
| Role, favorites, local collections, progress, recent history, search | Not declared as collected when they remain only on device. Do not mistake local storage for transmission. |
| Photos, videos, audio, contacts, precise location, health | Not requested or uploaded by the current app. Recheck merged permissions and content paths. |
| Feedback sharing | Android chooser transfers content only at the user's direction. Review Play's user-initiated transfer/sharing exception and actual receiving destination; do not claim an automatic publisher feedback service exists. Direct support email is described in the privacy policy and must follow the owner's real retention process. |
| Device identifiers, approximate location, diagnostics, app interactions | Expected advertising SDK collection is removed from the intended release. Confirm the dependency/traffic audit; assess any remaining SDK independently. |
| Purchase history / financial information | No active purchase flow or connected verification service in this release. Before enabling purchases, assess Play SDK behavior and backend purchase tokens, product/entitlement data, account IDs, retention, and sharing. Never collect or store payment card details in this app. |
| Encryption in transit | For any reportable transmitted data, confirm all relevant paths use secure transport. Do not select Yes solely because the manifest omits cleartext traffic. If no collection is reported, follow the actual Console branching. |
| Data deletion | Local deletion is available in Profile. No cloud account exists. If any server or support data is collected, disclose its real deletion process/retention; local deletion cannot erase third-party copies or Google Play records. |
| Independent security review / certification | Do not claim one. It has not been performed. |

## Other App content declarations

| Console area | Preparation |
| --- | --- |
| Privacy policy | Publish the owner-completed [policy](../legal/privacy-policy.md) at an active public HTTPS URL. The support email is supplied. A public hosting URL and owner review of the policy/audience are still required. |
| Ads | Select **No, my app does not contain ads** for the verified ads-free Play release. If any future release serves ads, update this declaration and complete consent/SDK work first. |
| App access | Core app is accessible without an account. Describe three-role onboarding. Reviewers can read all 48 sample posing guides, including four premium-labeled collection previews, and free beginner lessons. Advanced Academy syllabuses are visible but premium lesson bodies remain locked. Do not provide a fabricated login or claim reviewers can buy while billing is disabled. |
| Content rating | Complete the IARC questionnaire truthfully from actual content. Do not invent a rating. Photography education/illustrations do not automatically establish an age rating. |
| Target audience | Owner must select the intended age groups accurately. "Everyone" is an app role, not a child-directed or all-ages policy declaration. Children in the declared audience trigger additional Play Families/advertising/privacy obligations. |
| Account deletion | No account creation/sign-in is currently offered. If enabled later, implement both in-app and web account-deletion paths, data retention, and Console links before that release. |
| Financial/health/government/news declarations | Answer for actual features. The app teaches photography; do not label it a financial service because it has a future subscription. |
| Content rights | Only original labeled illustrations and text are shipped. User-supplied photos are prohibited. Retain rights evidence for any future licensed assets and model releases where needed. |
| AI | Recommendations are explicitly rule-based, offline suggestions. Do not advertise a working generative AI service. |
| Support | Enter the owner-confirmed public support email `simonsolomon436@gmail.com`. The issue tracker is public; instruct users not to post personal information. |

## If advertising is enabled later

First reintroduce approved SDKs only in the chosen variant, configure owner ad IDs outside source control, and use test IDs/devices before release. Implement and test consent, required privacy-options access, regional/age treatment, ad lifecycle cleanup, and appropriate placements. Read [Mobile Ads SDK data disclosures](https://developers.google.com/admob/android/privacy/play-data-disclosure) for the exact SDK versions. Typical data needing assessment includes IP-derived approximate location, device/advertising identifiers, app interactions, diagnostics, and ad performance information. The actual answers depend on configuration and the SDK; this list is not a submitted declaration. Update the public policy, Data Safety, and ads declaration before rollout.

## If billing/accounts are enabled later

Deploy authenticated, authorized verification with server-only credentials, current Google Play status checks, ownership binding, acknowledgement, expiry/revocation and RTDN handling. Add restore/manage-subscription controls and verified entitlement delivery. Document the real data: account ID/email if introduced, purchase token, product/subscription state, transaction records, and support/retention requirements. Explain account and server-data deletion without implying deleting the app cancels a Play subscription. Complete sandbox/Play test evidence before making purchase claims.
