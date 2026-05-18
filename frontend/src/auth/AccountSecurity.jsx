import { useEffect, useState } from "react";
import { QRCodeCanvas } from "qrcode.react";
import { authApi } from "./authApi.js";

export function AccountSecurity({ setMessage }) {
  const [settings, setSettings] = useState(null);
  const [challenge, setChallenge] = useState(null);
  const [code, setCode] = useState("");
  const [method, setMethod] = useState("EMAIL_OTP");
  const [otpCooldownSeconds, setOtpCooldownSeconds] = useState(0);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  useEffect(() => {
    if (otpCooldownSeconds <= 0) {
      return undefined;
    }

    const timer = setInterval(() => {
      setOtpCooldownSeconds((seconds) => Math.max(0, seconds - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [otpCooldownSeconds]);

  async function loadSettings() {
    try {
      setSettings(await authApi.getTwoFactorSettings());
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    }
  }

  async function begin(action) {
    setBusy(true);
    try {
      const response = await authApi.beginTwoFactor(action, method);
      setChallenge({ action, ...response });
      setCode("");
      setOtpCooldownSeconds(0);
      setMessage({
        type: "success",
        text: response.method === "TOTP" ? "Enter the authenticator code to continue." : "Verification code sent."
      });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setBusy(false);
    }
  }

  async function confirm(event) {
    event.preventDefault();
    if (!challenge) return;

    setBusy(true);
    try {
      await authApi.confirmTwoFactor(challenge.action, {
        challengeId: challenge.challengeId,
        code: code.trim()
      });
      setChallenge(null);
      await loadSettings();
      setMessage({ type: "success", text: "Two-factor settings updated." });
    } catch (error) {
      setCode("");
      const cooldown = parseCooldownSeconds(error.message);
      if (cooldown > 0) {
        setOtpCooldownSeconds(cooldown);
        setMessage({ type: "error", text: "" });
      } else {
        setMessage({ type: "error", text: error.message });
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="panel">
      <div className="section-heading">
        <h2>Two-Factor Authentication</h2>
        <button className="secondary" onClick={loadSettings}>Refresh</button>
      </div>

      <p className="muted">
        Status: {settings?.enabled ? "enabled" : "disabled"}.
        Method: {settings?.method || "none"}.
        {settings?.pendingMethod ? ` Pending: ${settings.pendingMethod}.` : ""}
      </p>

      <label>2FA Method
        <select value={method} onChange={(event) => setMethod(event.target.value)}>
          <option value="EMAIL_OTP">Email OTP</option>
          <option value="TOTP">Authenticator App</option>
        </select>
      </label>

      <div className="row wrap">
        <button disabled={busy} onClick={() => begin("enable")}>Enable</button>
        <button disabled={busy} className="secondary" onClick={() => begin("change")}>Change Method</button>
        <button disabled={busy} className="secondary" onClick={() => begin("disable")}>Disable</button>
      </div>

      {challenge && (
        <form className="subform" onSubmit={confirm}>
          {otpCooldownSeconds > 0 && (
            <div className="message error">
              Maximum OTP attempts exceeded. Try again in {formatCooldown(otpCooldownSeconds)}.
            </div>
          )}
          <p className="muted">Enter the {challenge.method} code to {challenge.action} 2FA.</p>
          {challenge.method === "TOTP" && challenge.totpSecret && (
            <div className="message success">
              {challenge.totpUri && (
                <div className="qr-code">
                  <QRCodeCanvas value={challenge.totpUri} size={180} />
                </div>
              )}
              <p>Authenticator setup key: <strong>{challenge.totpSecret}</strong></p>
              <p className="muted">Open this URI from an authenticator app if your device supports it:</p>
              <a href={challenge.totpUri}>{challenge.totpUri}</a>
            </div>
          )}
          <label>Code
            <input
                value={code}
                inputMode="numeric"
                maxLength={6}
                onChange={(event) => setCode(event.target.value.replace(/\D/g, "").slice(0, 6))}
                required
            />
          </label>
          <button disabled={busy}>{busy ? "Confirming..." : "Confirm"}</button>
        </form>
      )}
    </section>
  );
}

function parseCooldownSeconds(message) {
  const match = message.match(/Try again in (?:(\d+) minutes?(?: (\d+) seconds?)?|(\d+) seconds?)\./i);
  if (!match) {
    return 0;
  }
  const minutes = Number(match[1] || 0);
  const seconds = Number(match[2] || match[3] || 0);
  return minutes * 60 + seconds;
}

function formatCooldown(totalSeconds) {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  if (minutes === 0) {
    return `${seconds} second${seconds === 1 ? "" : "s"}`;
  }
  if (seconds === 0) {
    return `${minutes} minute${minutes === 1 ? "" : "s"}`;
  }
  return `${minutes} minute${minutes === 1 ? "" : "s"} ${seconds} second${seconds === 1 ? "" : "s"}`;
}
