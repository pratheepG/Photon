import axios from "axios";

const BASIC_USERNAME = "4a7f3176-f664-45b1-89f4-21f6840889b3";
const BASIC_PASSWORD = "2d1b4ddf-d708-414d-8db5-05687a2da57b";


function createBasicAuthHeader() {
  const token = `${BASIC_USERNAME}:${BASIC_PASSWORD}`;
  return `Basic ${btoa(token)}`;
}

let jwtToken = null;

export const setJwtToken = (token) => {
  jwtToken = token;
  if (token) {
    localStorage.setItem("jwtToken", token);
  } else {
    localStorage.removeItem("jwtToken");
  }
};

export const getJwtToken = () => {
  if (!jwtToken) {
    jwtToken = localStorage.getItem("jwtToken");
  }
  return jwtToken;
};

function getReportingParams() {
  const navigatorInfo = window.navigator;
  const screenInfo = window.screen;
  const userAgent = navigatorInfo.userAgent;

  return {
    iP: "192.168.1.1",
    applicationId: "APP_SERVER_CONSOLE",
    deviceId: navigatorInfo.deviceMemory ? `DEVICE-${navigatorInfo.deviceMemory}` : "DEVICE-UNKNOWN",
    deviceName: navigatorInfo.platform || "Unknown Device",
    deviceType: /Mobi/i.test(userAgent) ? "Mobile" : "Desktop",
    deviceOs: navigatorInfo.platform || "Unknown OS",
    osVersion: detectOSVersion(userAgent),
    userAgent: userAgent,
  };
}

function detectOSVersion(userAgent) {
  let os = "Unknown OS";

  if (/Windows NT 10\.0/i.test(userAgent)) os = "Windows 10";
  else if (/Windows NT 11\.0/i.test(userAgent)) os = "Windows 11";
  else if (/Mac OS X (\d+[_\.]\d+)/i.test(userAgent)) {
    os = "macOS " + RegExp.$1.replace("_", ".");
  }
  else if (/Android (\d+(\.\d+)?)/i.test(userAgent)) {
    os = "Android " + RegExp.$1;
  }
  else if (/iPhone OS (\d+[_\.]\d+)/i.test(userAgent)) {
    os = "iOS " + RegExp.$1.replace("_", ".");
  }
  else if (/iPad.*OS (\d+[_\.]\d+)/i.test(userAgent)) {
    os = "iPadOS " + RegExp.$1.replace("_", ".");
  }
  else if (/Linux/i.test(userAgent)) os = "Linux";

  return os;
}

const api = axios.create({
  baseURL: "http://localhost:8085",
  withCredentials: false,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  async (config) => {
    let token = getAccessToken();
    const expiryTime = getExpiryTime();

    if (expiryTime && Date.now() >= expiryTime) {
      token = await refreshAccessToken();
    }

    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    } else {
      config.headers["Authorization"] = createBasicAuthHeader();
    }

    config.headers["Reportingparams"] = JSON.stringify(getReportingParams());

    return config;
  },
  (error) => Promise.reject(error)
);


api.interceptors.response.use(
  (response) => response,
  async (error) => {
    let message = "Something went wrong!";
    if (error.response?.data) {
      const data = error.response.data;
      message = data.errMessage || data.responseData?.message || data.message || message;
    }

    const status = error.response?.status;
    const isLoginPage = window.location.pathname.includes("/login");

    if ( !isLoginPage && ( status === 401 || status === 403 || message === "INVALID_JWT_TOKEN" || message === "SUSPICIOUS_ACTIVITY")) {
      broadcastError("Your session has expired or is invalid. Please login again.", true);
      logoutAndRedirect();
    } else {
      broadcastError(message, false);
    }

    return Promise.reject(error);
  }
);




let logoutTimer = null;

export function setSession({ accessToken, refreshToken, expiryDuration }) {
  // expiryDuration is in MINUTES
  const expiryTime = Date.now() + expiryDuration * 60 * 1000;

  localStorage.setItem("accessToken", accessToken);
  localStorage.setItem("refreshToken", refreshToken);
  localStorage.setItem("expiryTime", expiryTime);

  if (logoutTimer) clearTimeout(logoutTimer);

  logoutTimer = setTimeout(() => {
    logoutAndRedirect("Session expired. Please login again.");
  }, expiryDuration * 60 * 1000); // convert minutes to ms
}

export function clearSession() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("expiryTime");

  if (logoutTimer) {
    clearTimeout(logoutTimer);
    logoutTimer = null;
  }
}


function getAccessToken() {
  return localStorage.getItem("accessToken");
}
function getRefreshToken() {
  return localStorage.getItem("refreshToken");
}
function getExpiryTime() {
  return parseInt(localStorage.getItem("expiryTime") || "0", 10);
}

async function logoutAndRedirect() {
  try {
    await api.post("/authentication/logout");
  } catch (err) {
    console.warn("Logout endpoint not available, skipping...");
  }

  clearSession();
  localStorage.clear();
  sessionStorage.clear();

  document.cookie.split(";").forEach((c) => {
    document.cookie = c
      .replace(/^ +/, "")
      .replace(/=.*/, `=;expires=${new Date().toUTCString()};path=/`);
  });

  window.location.replace("/");
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    logoutAndRedirect();
    return null;
  }

  try {
    const response = await axios.get("http://localhost:8085/authentication/refresh-claims-token", { params: { refreshToken } });

    if (response.data?.responseData?.accessToken) {
      const { accessToken, refreshToken: newRefresh, expiryDuration } = response.data.responseData;
      setSession({ accessToken, refreshToken: newRefresh, expiryDuration });
      return accessToken;
    }
  } catch (err) {
    console.error("Refresh token failed ❌", err);
    logoutAndRedirect();
  }
  return null;
}


let errorListeners = [];

export function subscribeToErrors(listener) {
  errorListeners.push(listener);
  return () => {
    errorListeners = errorListeners.filter((l) => l !== listener);
  };
}

function broadcastError(message) {
  errorListeners.forEach((listener) => listener(message));
}

export default api;