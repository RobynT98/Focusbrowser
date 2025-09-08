# FocusBrowser (Android)

Lightweight allowlist WebView browser for work/school. Built entirely from mobile using Acode + GitHub Actions.

## Edit the policy
`app/src/main/assets/policy.json` controls:
- `start_url`
- `allowlist` host suffixes (e.g., `office.com` also allows `login.office.com`).

## Build (GitHub Actions)
1. Push this repo to GitHub.
2. Go to **Actions** → **Android CI** → **Run workflow** (or push to `main`).
3. Download the artifact `FocusBrowser-debug-apk` → install on your phone.
