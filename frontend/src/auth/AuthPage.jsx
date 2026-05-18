import { useEffect, useState } from "react";
import { authApi } from "./authApi.js";

export function AuthPage({ message, setMessage, onAuthenticated }) {
  const [mode, setMode] = useState("login");
  const [challengeId, setChallengeId] = useState(null);
  const [challengeMethod, setChallengeMethod] = useState(null);
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({ email: "", password: "", role: "BUYER" });
  const [code, setCode] = useState("");
  const [lockoutMessagePrefix, setLockoutMessagePrefix] = useState("");
  const [loginCooldownSeconds, setLoginCooldownSeconds] = useState(0);
  const [otpCooldownSeconds, setOtpCooldownSeconds] = useState(0);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (loginCooldownSeconds <= 0 && otpCooldownSeconds <= 0) {
      return undefined;
    }

    const timer = setInterval(() => {
      setLoginCooldownSeconds((seconds) => Math.max(0, seconds - 1));
      setOtpCooldownSeconds((seconds) => Math.max(0, seconds - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [loginCooldownSeconds, otpCooldownSeconds]);

  async function submitLogin(event) {
    event.preventDefault();
    setBusy(true);
    try {
      const response = await authApi.login(loginForm);
      if (response.requiresTwoFactor) {
        setChallengeId(response.challengeId);
        setChallengeMethod(response.twoFactorMethod);
        setMessage({
          type: "success",
          text: response.twoFactorMethod === "TOTP"
              ? "Enter the authenticator app code to continue."
              : "Enter the email OTP to continue."
        });
        return;
      }
      onAuthenticated(response);
    } catch (error) {
      const cooldown = parseCooldownSeconds(error.message);
      if (cooldown > 0) {
        setLockoutMessagePrefix("Maximum login attempts exceeded.");
        setLoginCooldownSeconds(cooldown);
        setMessage({ type: "error", text: "" });
      } else {
        setLockoutMessagePrefix("");
        setLoginCooldownSeconds(0);
        setMessage({ type: "error", text: error.message });
      }
    } finally {
      setBusy(false);
    }
  }

  async function submitRegister(event) {
    event.preventDefault();
    setBusy(true);
    try {
      await authApi.register(registerForm);
      setMode("login");
      setLoginForm({ email: registerForm.email, password: "" });
      setMessage({ type: "success", text: "Account created. You can log in now." });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setBusy(false);
    }
  }

  async function submitTwoFactor(event) {
    event.preventDefault();
    setBusy(true);
    try {
      const response = await authApi.verifyLogin2fa({ challengeId, code: code.trim() });
      onAuthenticated(response);
    } catch (error) {
      setCode("");
      const cooldown = parseCooldownSeconds(error.message);
      if (cooldown > 0) {
        setLockoutMessagePrefix("Maximum OTP attempts exceeded.");
        setOtpCooldownSeconds(cooldown);
        setMessage({ type: "error", text: "" });
        return;
      }
      setMessage({ type: "error", text: error.message });
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-layout">
      <section className="intro">
        <p className="eyebrow">BidMart</p>
        <h1>BidMart</h1>
        <p className="muted">BidMart tagline here.</p>
      </section>

      <section className="auth-card">
        {message && (
          <div className={`message ${message.type}`}>
            {loginCooldownSeconds > 0
                ? `${lockoutMessagePrefix} Try again in ${formatCooldown(loginCooldownSeconds)}.`
                : otpCooldownSeconds > 0
                    ? `${lockoutMessagePrefix} Try again in ${formatCooldown(otpCooldownSeconds)}.`
                : message.text}
          </div>
        )}

        {!challengeId && (
          <div className="tabs">
            <button className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Login</button>
            <br/>
            {/*<button className={mode === "register" ? "active" : ""} onClick={() => setMode("register")}>Register</button>*/}
          </div>
        )}

        {mode === "login" && !challengeId && (
          <form className="panel" onSubmit={submitLogin}>
            <h2>Login</h2>
            <label>Email
              <input type="email" value={loginForm.email} onChange={(event) => setLoginForm({ ...loginForm, email: event.target.value })} required />
            </label>
            <label>Password
              <input type="password" value={loginForm.password} onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })} required />
            </label>
            <p style={{ textAlign: "center" }}>
              Don't have an account? Register{" "}
              <span
                  onClick={() => setMode("register")}
                  style={{ color: "gray", cursor: "pointer" }}
              >
                here
              </span>
            </p>
            <button disabled={busy}>{busy ? "Working..." : "Login"}</button>
          </form>
        )}

        {mode === "register" && !challengeId && (
          <form className="panel" onSubmit={submitRegister}>
            <h2>Create Account</h2>
            <label>Email
              <input type="email" value={registerForm.email} onChange={(event) => setRegisterForm({ ...registerForm, email: event.target.value })} required />
            </label>
            <label>Password
              <input type="password" minLength={8} value={registerForm.password} onChange={(event) => setRegisterForm({ ...registerForm, password: event.target.value })} required />
            </label>
            <label>Role
              <select value={registerForm.role} onChange={(event) => setRegisterForm({ ...registerForm, role: event.target.value })}>
                <option value="BUYER">Buyer</option>
                <option value="SELLER">Seller</option>
                {/*<option value="ADMINISTRATOR">Administrator first account only</option>*/}
              </select>
            </label>
            <button disabled={busy}>{busy ? "Working..." : "Register"}</button>
          </form>
        )}

        {challengeId && (
          <form className="panel" onSubmit={submitTwoFactor}>
            <h2>Two-Factor Code</h2>
            <p className="muted">
              {challengeMethod === "TOTP"
                  ? "Enter the current code from your authenticator app."
                  : "Enter the email OTP that we've sent. This may take a while."}
            </p>
            <label>Code
              <input
                  value={code}
                  inputMode="numeric"
                  maxLength={6}
                  onChange={(event) => setCode(event.target.value.replace(/\D/g, "").slice(0, 6))}
                  required
              />
            </label>
            <div className="row">
              <button disabled={busy}>{busy ? "Verifying..." : "Verify"}</button>
              <button type="button" className="secondary" onClick={() => {
                setChallengeId(null);
                setChallengeMethod(null);
              }}>Back</button>
            </div>
          </form>
        )}
      </section>
    </main>
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
