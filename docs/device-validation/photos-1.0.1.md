# Photo update device evidence

Captured from the signed Hello Photography 1.0.1 / code 4 release APK on the
API 28 software Android emulator on 2026-10-19. These are unedited Android
`screencap` PNGs, distinct from the native Robolectric store renders.

- `photo-guide-api28.png`: actual decoded photo guide 1001, full-screen entry,
  AI source label and credit.
- `photo-viewer-api28.png`: complete image at 100%, title, source label, zoom,
  reset, close and previous/next controls; position 1 of the 39 supplied photos.

The actual signed APK installed successfully as an update, retained the saved
Photographer role, opened photo 1001, zoomed to 150%, reset to 100%, moved to
photo 2/39, and closed back to the original guide. This is a software emulator
without KVM, not physical-device performance or 16 KB runtime validation.
The planned 48-photo collection remains missing two female and seven group files.

APK SHA-256: `e68a891409f7f2615728f9fad3b46ded5a8ef4bddd507c274118085825cca2b4`.
