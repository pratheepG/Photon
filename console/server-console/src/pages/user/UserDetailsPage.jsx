import { useEffect, useState } from "react";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { Box, Paper, Grid, Avatar, Typography, Divider, Chip, Button, CircularProgress, List, ListItem, ListItemText, Tooltip, Stack, Alert } from "@mui/material";
import api from '../../services/api';



const formatDate = (d) => {
  if (!d) return "-";
  try {
    return new Date(d).toLocaleString();
  } catch {
    return d;
  }
};

const UserRow = ({ label, value }) => (
  <Box sx={{ mb: 1 }}>
    <Typography variant="caption" color="text.secondary">{label}</Typography>
    <Typography variant="body1">{value ?? "-"}</Typography>
  </Box>
);

const UserDetailsPage = () => {
  const { userId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const stateUser = location?.state?.user || null;

  const [user, setUser] = useState(stateUser);
  const [loading, setLoading] = useState(!stateUser);
  const [error, setError] = useState(null);
  const [tempPwdLoading, setTempPwdLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    if (stateUser) return; // have full user from navigation state
    if (!userId) {
      setError("No user selected.");
      setLoading(false);
      return;
    }

    const fetchUser = async () => {
      setLoading(true);
      setError(null);
      try {
        // Try a common detail endpoint; adapt if your backend expects a different path.
        const resp = await api.get(`/user/${userId}`);
        if (resp.data?.success && resp.data?.responseData) {
          setUser(resp.data.responseData);
        } else {
          // fallback: maybe your API exposes get-user by id
          const resp2 = await api.get(`/user/get-user/${userId}`);
          if (resp2.data?.success && resp2.data?.responseData) {
            // if responseData is array, use first item
            const r = Array.isArray(resp2.data.responseData) ? resp2.data.responseData[0] : resp2.data.responseData;
            setUser(r);
          } else {
            setError("User not found.");
          }
        }
      } catch (err) {
        console.error("Failed to fetch user details", err);
        setError("Failed to fetch user details.");
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [userId, stateUser]);

  const handleRequestTempPassword = async () => {
    if (!user?.userName) return;
    setTempPwdLoading(true);
    setSuccessMessage(null);
    try {
      const resp = await api.get("/authentication/request-for-temp-password", {
        params: { userName: user.userName },
      });
      if (resp.data?.success) {
        setSuccessMessage("Temporary password requested successfully. Check configured channels (SMS/Email).");
      } else {
        setError(resp.data?.errMessage || "Failed to request temporary password.");
      }
    } catch (err) {
      console.error("Request temp password failed", err);
      setError("Failed to request temporary password.");
    } finally {
      setTempPwdLoading(false);
    }
  };

  const handleBack = () => {
    navigate(-1);
  };

  if (loading) {
    return <Box sx={{ display: "flex", justifyContent: "center", mt: 10 }}><CircularProgress /></Box>;
  }

  if (error) {
    return (
      <Box sx={{ width: "80vw", p: 4 }}>
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
        <Button variant="contained" onClick={handleBack}>Back</Button>
      </Box>
    );
  }

  if (!user) {
    return (
      <Box sx={{ width: "80vw", p: 4 }}>
        <Typography>No user data available.</Typography>
        <Button variant="contained" onClick={handleBack} sx={{ mt: 2 }}>Back</Button>
      </Box>
    );
  }

  return (
    <Box sx={{ width: "80vw", minHeight: "80vh", padding: "20px", backgroundColor: "#f6f9fc" }}>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3 }}>
        <Typography variant="h4">User Details</Typography>
        <Button variant="outlined" onClick={handleBack}>Back</Button>
      </Box>

      {successMessage && <Alert severity="success" sx={{ mb: 2 }}>{successMessage}</Alert>}

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
              <Avatar sx={{ width: 80, height: 80 }}>{(user.firstName || user.userName || "U").charAt(0)}</Avatar>
              <Box>
                <Typography variant="h6">{(user.firstName || "") + (user.lastName ? ` ${user.lastName}` : "") || user.userName}</Typography>
                <Typography variant="body2" color="text.secondary">{user.userName || "—"}</Typography>
              </Box>
            </Box>

            <Divider sx={{ my: 2 }} />

            <UserRow label="User ID" value={user.userId || user.id || "-"} />
            <UserRow label="Tenant" value={user.tenantId || "-"} />
            <UserRow label="Phone" value={user.primaryPhoneNumber || user.phoneNumber || "-"} />
            <UserRow label="Email" value={user.primaryEmailAddress || user.email || "-"} />
            <UserRow label="Sex" value={user.sex || user.gender || "-"} />
            <UserRow label="DOB" value={formatDate(user.dob)} />
            <UserRow label="Status" value={user.isEnabled ? "Enabled" : "Disabled"} />

            <Stack direction="row" spacing={1} sx={{ mt: 2, flexWrap: "wrap" }}>
              {user.roles?.map((r) => (
                <Chip key={r.id || r.roleId} label={r.name || r.roleId} size="small" />
              ))}
            </Stack>

            <Divider sx={{ my: 2 }} />

            <Box sx={{ display: "flex", gap: 1 }}>
              <Button
                variant="contained"
                onClick={() => navigate(`/users/${user.userId || user.id}/edit`, { state: { user } })}
              >
                Edit User
              </Button>

              {user.userName && (
                <Tooltip title="Request temporary password for this user">
                  <span> {/* span to allow disabled tooltip behavior */}
                    <Button
                      variant="outlined"
                      onClick={handleRequestTempPassword}
                      disabled={tempPwdLoading}
                    >
                      {tempPwdLoading ? "Requesting..." : "Request Temp Password"}
                    </Button>
                  </span>
                </Tooltip>
              )}
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" sx={{ mb: 1 }}>Addresses</Typography>
            <Divider sx={{ mb: 2 }} />
            {Array.isArray(user.address) && user.address.length ? (
              <List dense>
                {user.address.map((a) => (
                  <ListItem key={a.id || a.streetName}>
                    <ListItemText
                      primary={`${a.type || ""} — ${a.streetName || ""}${a.houseNumber ? ", " + a.houseNumber : ""}`}
                      secondary={`${a.city || ""}${a.district ? ", " + a.district : ""}${a.state ? ", " + a.state : ""} ${a.pin ? "- " + a.pin : ""}`}
                    />
                  </ListItem>
                ))}
              </List>
            ) : (
              <Typography>No addresses available.</Typography>
            )}
          </Paper>

          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" sx={{ mb: 1 }}>Electronic Addresses</Typography>
            <Divider sx={{ mb: 2 }} />
            {Array.isArray(user.electronicAddress) && user.electronicAddress.length ? (
              <List dense>
                {user.electronicAddress.map((ea) => (
                  <ListItem key={ea.id || ea.value}>
                    <ListItemText
                      primary={`${ea.type || ""} — ${ea.value || ""}`}
                      secondary={ea.countryCode ? `+${ea.countryCode}` : ""}
                    />
                  </ListItem>
                ))}
              </List>
            ) : (
              <Typography>No electronic addresses available.</Typography>
            )}
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 1 }}>Metadata</Typography>
            <Divider sx={{ mb: 2 }} />
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">Created On</Typography>
                <Typography variant="body2">{formatDate(user.createdOn || user.createdAt)}</Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">Last Updated</Typography>
                <Typography variant="body2">{formatDate(user.updatedOn || user.modifiedAt)}</Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">MFA Enabled</Typography>
                <Typography variant="body2">{user.isMfaEnabled ? "Yes" : "No"}</Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="caption" color="text.secondary">Invalid Login Attempts</Typography>
                <Typography variant="body2">{user.inValidLoginAttempts ?? 0}</Typography>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default UserDetailsPage;