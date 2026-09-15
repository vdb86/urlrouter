# URL Router

<p align="center">
  <img src="URLRouter.png" alt="URLRouter app icon" width="120" height="120" />
</p>

<p align="center">
  A lightweight Android app that intercepts links and routes them to the right browser automatically.
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.urlrouter.app">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="80"/>
  </a>
</p>

---

*Dedicated to my son Mihajlo.*

*Thank you [HumanMade](https://humanmade.com/) and [Altis](https://www.altis-dxp.com/) for making the development of this app possible.*

---

## 💬 Feedback, bug reports & feature requests

Found a bug or have an idea? This is the place to let me know.

- 🐞 **Found a bug?** [Report a bug](../../issues/new?template=bug_report.yml) and tell us what happened, what you expected, and your device / Android version.
- 💡 **Have an idea?** [Request a feature](../../issues/new?template=feature_request.yml) - I read every suggestion.
- 👍 **Want something that's already been suggested?** Browse [bug reports](../../issues?q=is%3Aissue+label%3Abug) and [feature requests](../../issues?q=is%3Aissue+label%3Aenhancement) and add a 👍 or a comment so I know it matters to you.

Please search the [open issues](../../issues) first to avoid duplicates.

---

## What it does

URL Router registers itself as a browser. When you open a link anywhere on Android, URL Router receives it first, evaluates your rules, and silently forwards the URL to the correct browser - with no visible UI when a rule matches.

If no rule matches, a minimal browser chooser appears so you can pick manually.

## Features

- **Rule-based routing** - route URLs by exact hostname, wildcard hostname (`*.example.com`), URL prefix, substring match, or full regex
- **Rule priority** - exact hostname → wildcard → prefix → regex → contains; first match wins
- **Default browser** - optionally define a fallback browser that opens when no rule matches, skipping the chooser entirely
- **Minimal chooser** - a clean bottom sheet with only the browsers you want; fully customisable appearance
- **No recent apps entry** - URL Router disappears after routing; it never appears in your app switcher
- **Long-press to create rule** - long-press any browser in the chooser to automatically create a rule for that domain
- **Browser management** - enable/disable browsers, set display order, rescan installed browsers
- **Appearance settings** - configure the chooser with grid or list display mode, icon size, text size and colour, vertical position, corner radius, padding, and background colour with a full HSV colour picker including transparency
- **Import / Export** - back up and restore your entire configuration using the system file picker
- **Diagnostics** - paste any URL to see exactly which rule would match and which browser would open it

## Default browser

Under **Browser Management** you can designate a default browser. When enabled, any link that doesn't match a routing rule is sent directly to the default browser - the chooser never appears. This is useful if you have one browser you use for everything except a handful of specific sites you've created rules for.

The default browser is shown with a **Default** badge in the browser list and can be changed or disabled at any time.

## Appearance

The browser chooser is fully customisable under **Appearance**:

- **Display mode** - grid (icons with optional labels) or vertical list
- **Show browser icons** - toggle icons on or off; when enabled, set the icon size
- **Show browser names** - toggle labels on or off; when enabled, set the text colour and text size
- **Alignment** - left, centre, or right
- **Vertical position** - move the chooser up from the bottom of the screen (0% = bottom, 100% = top)
- **Background colour** - full HSV colour picker with a hue bar, saturation/brightness panel, transparency slider, and hex input
- **Corner radius** - from sharp corners to a fully rounded sheet; all corners are rounded when the sheet is elevated
- **Padding and item spacing** - fine-tune the layout
- **Preview** - see your changes live before saving, using your real installed browsers

## Import / Export

Your entire configuration - routing rules, browser order, enabled browsers, and appearance settings - can be exported to a JSON file and restored later. Tap **Export** to save the file anywhere on your device (Downloads, Google Drive, etc.) using the system file picker. Tap **Import** to select a previously exported file and restore it.

## Screenshots

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200"/>
</p>

## Requirements

- Android 10 (API 29) or higher
- One or more browsers installed

## Setup

1. Install URL Router
2. Open **Settings → Apps → Default apps → Browser** and select **URL Router**
3. Open URL Router and go to **Browser Management** to scan your installed browsers
4. Add routing rules under **Routing Rules**

[GPL-3.0](LICENSE)

## Contributing

Issues and pull requests are welcome. Please open an issue before submitting a large change.
