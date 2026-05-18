import { useState } from "react";
import { authApi } from "./authApi.js";

export function AuthPage({ message, setMessage, onAuthenticated }) {
  const [mode, setMode] = useState("login");
  const [challengeId, setChallengeId] = useState(null);
  const [challengeMethod, setChallengeMethod] = useState(null);
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({ email: "", password: "", role: "BUYER" });
  const [code, setCode] = useState("");
  const [busy, setBusy] = useState(false);

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
      setMessage({ type: "error", text: error.message });
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
      const response = await authApi.verifyLogin2fa({ challengeId, code });
      onAuthenticated(response);
    } catch (error) {
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
        {message && <div className={`message ${message.type}`}>{message.text}</div>}

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
              <input value={code} inputMode="numeric" onChange={(event) => setCode(event.target.value)} required />
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
