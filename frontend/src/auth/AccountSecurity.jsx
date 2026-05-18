import { useEffect, useState } from "react";
import { QRCodeCanvas } from "qrcode.react";
import { authApi } from "./authApi.js";

export function AccountSecurity({ setMessage }) {
  const [settings, setSettings] = useState(null);
  const [challenge, setChallenge] = useState(null);
  const [code, setCode] = useState("");
  const [method, setMethod] = useState("EMAIL_OTP");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

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
        code
      });
      setChallenge(null);
      await loadSettings();
      setMessage({ type: "success", text: "Two-factor settings updated." });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
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
            <input value={code} inputMode="numeric" onChange={(event) => setCode(event.target.value)} required />
          </label>
          <button disabled={busy}>{busy ? "Confirming..." : "Confirm"}</button>
        </form>
      )}
    </section>
  );
}
