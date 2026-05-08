const accessKey = "bidmart.accessToken";
const refreshKey = "bidmart.refreshToken";

export const tokenStore = {
  get() {
    return {
      accessToken: localStorage.getItem(accessKey),
      refreshToken: localStorage.getItem(refreshKey)
    };
  },

  set(tokens) {
    localStorage.setItem(accessKey, tokens.accessToken);
    localStorage.setItem(refreshKey, tokens.refreshToken);
  },

  clear() {
    localStorage.removeItem(accessKey);
    localStorage.removeItem(refreshKey);
  }
};
