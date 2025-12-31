export function buildReportingParams(appId?: string) {
  const deviceId = typeof localStorage !== "undefined" ? (localStorage.getItem("sdk_device_id") || (() => { const id = crypto.randomUUID(); localStorage.setItem("sdk_device_id", id); return id; })()) : "device-unknown";
  return {
    iP: undefined,
    applicationId: appId,
    deviceId,
    deviceName: typeof navigator !== "undefined" ? navigator.platform : "node",
    deviceType: typeof navigator !== "undefined" && /Mobi|Android|iPhone|iPad/.test(navigator.userAgent || "") ? "Mobile" : "Desktop",
    deviceOs: typeof navigator !== "undefined" ? navigator.userAgent : "node",
    osVersion: "",
    userAgent: typeof navigator !== "undefined" ? navigator.userAgent : ""
  };
}

export function encodeReportingHeader(obj: any) {
    try {
    if (typeof btoa !== "undefined") return btoa(unescape(encodeURIComponent(JSON.stringify(obj))));
    return Buffer.from(JSON.stringify(obj)).toString('base64');
  } catch (e) { return ""; }
}