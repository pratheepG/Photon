import { useEffect, useState } from "react";
import { Box, Paper, Typography, Button, CircularProgress, Alert, Grid, Dialog, DialogTitle, 
    DialogContent, DialogActions, TextField, Snackbar, IconButton, Divider, Tooltip } from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import StopIcon from "@mui/icons-material/Stop";
import CloseIcon from "@mui/icons-material/Close";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { getDeploymentById, publishDeployment, unpublishDeployment, uploadJar, updateDeployment } from "./service/deploymentService";

export default function DeploymentDetailsPage() {
  const { id: paramId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const initialFromState = (location && location.state && location.state.deployment) || null;
  const id = paramId;

  const [deployment, setDeployment] = useState(initialFromState);
  const [loading, setLoading] = useState(!initialFromState);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const [cfgOpen, setCfgOpen] = useState(false);
  const [dockerOpen, setDockerOpen] = useState(false);
  const [envText, setEnvText] = useState("");
  const [dockerText, setDockerText] = useState("");
  const [snack, setSnack] = useState({ open: false, message: "", severity: "success" });

  useEffect(() => {
      load();
  }, [id]);

  async function load() {
    setLoading(true);
    setError("");
    try {
      const data = await getDeploymentById(id);
      setDeployment(data);
      setEnvText(prettyEnv(data?.environment));
      setDockerText(data?.dockerfileContent || "");
    } catch (err) {
      console.error("load deployment failed", err);
      setError("Failed to load deployment details.");
    } finally {
      setLoading(false);
    }
  }

  function prettyEnv(env) {
    try {
      return JSON.stringify(env || {}, null, 2);
    } catch (e) {
      return String(env || "");
    }
  }

  const handlePublish = async () => {
    if (!deployment) return;
    setSaving(true);
    try {
      await publishDeployment(deployment.id);
      setSnack({ open: true, message: "Published successfully", severity: "success" });
      await load();
    } catch (err) {
      console.error("publish error", err);
      setSnack({ open: true, message: "Publish failed", severity: "error" });
    } finally {
      setSaving(false);
    }
  };

  const handleUnpublish = async () => {
    if (!deployment) return;
    setSaving(true);
    try {
      await unpublishDeployment(deployment.id);
      setSnack({ open: true, message: "Unpublished successfully", severity: "success" });
      await load();
    } catch (err) {
      console.error("unpublish error", err);
      setSnack({ open: true, message: "Unpublish failed", severity: "error" });
    } finally {
      setSaving(false);
    }
  };

  const handleJarSelect = async (e) => {
    const file = e.target.files && e.target.files[0];
    if (!file || !deployment) return;
    const form = new FormData();
    form.append("file", file);
    setSaving(true);
    try {
      await uploadJar(deployment.id, form);
      setSnack({ open: true, message: "Jar uploaded", severity: "success" });
      await load();
    } catch (err) {
      console.error("jar upload failed", err);
      setSnack({ open: true, message: "Jar upload failed", severity: "error" });
    } finally {
      setSaving(false);
      e.target.value = "";
    }
  };

  const handleSaveEnv = async () => {
    let parsed;
    try {
      parsed = envText ? JSON.parse(envText) : {};
    } catch (err) {
      setSnack({ open: true, message: "Environment JSON invalid", severity: "error" });
      return;
    }

    setSaving(true);
    try {
      await updateDeployment(deployment.id, { environment: parsed });
      setSnack({ open: true, message: "Environment saved", severity: "success" });
      setCfgOpen(false);
      await load();
    } catch (err) {
      console.error("save env failed", err);
      setSnack({ open: true, message: "Save failed", severity: "error" });
    } finally {
      setSaving(false);
    }
  };

  const handleSaveDocker = async () => {
    setSaving(true);
    try {
      await updateDeployment(deployment.id, { dockerfileContent: dockerText });
      setSnack({ open: true, message: "Dockerfile saved", severity: "success" });
      setDockerOpen(false);
      await load();
    } catch (err) {
      console.error("save docker failed", err);
      setSnack({ open: true, message: "Save failed", severity: "error" });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ p: 4, display: "flex", justifyContent: "center" }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  if (!deployment) {
    return (
      <Box sx={{ p: 4 }}>
        <Alert severity="info">Deployment not found</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, width: "90vw", minHeight: "70vh" }}>
      <Paper sx={{ p: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={9}>
            <Typography variant="h5">{deployment.serviceName || "—"}</Typography>
            <Typography variant="subtitle2" color="text.secondary">
              ID: {deployment.id}
            </Typography>
          </Grid>

          <Grid item xs={3} sx={{ textAlign: "right" }}>
            <Tooltip title={deployment.deployed ? "Already deployed" : "Publish this deployment"}>
              <span>
                <Button variant="contained" startIcon={<PlayArrowIcon />} onClick={handlePublish} disabled={deployment.deployed || saving} sx={{ mr: 1 }}>
                  Publish
                </Button>
              </span>
            </Tooltip>

            <Tooltip title={deployment.deployed ? "Unpublish" : "Nothing to unpublish"}>
              <span>
                <Button variant="outlined" color="error" startIcon={<StopIcon />} onClick={handleUnpublish} disabled={!deployment.deployed || saving} sx={{ mr: 1 }}>
                  Unpublish
                </Button>
              </span>
            </Tooltip>
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 1 }} />
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle1">JAR File</Typography>
            <Typography variant="body2">{deployment.jarFileName || "-"}</Typography>
            <Box sx={{ mt: 1 }}>
              <label htmlFor="jar-upload">
                <input id="jar-upload" type="file" accept=".jar" style={{ display: "none" }} onChange={handleJarSelect}/>
                <Button variant="outlined" startIcon={<CloudUploadIcon />} component="span" sx={{ mr: 1 }} disabled={saving}>
                  Update JAR
                </Button>
              </label>

              <Button variant="text" startIcon={<EditIcon />}
                    onClick={() => {
                        navigate(`/deployment/${deployment.id}/update-jar`);
                    }}
              >
                Update JAR (page)
              </Button>
            </Box>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle1">Environment</Typography>
            <Box sx={{ mt: 1, p: 2, bgcolor: "#f7f7f7", borderRadius: 1, fontFamily: "monospace", whiteSpace: "pre-wrap" }}>
              {prettyEnv(deployment.environment)}
            </Box>

            <Box sx={{ mt: 1 }}>
              <Button variant="outlined" onClick={() => setCfgOpen(true)} sx={{ mr: 1 }}>
                Edit Config
              </Button>

              <Button variant="outlined" onClick={() => setDockerOpen(true)}>
                Edit Docker Config
              </Button>
            </Box>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle1">Other</Typography>
            <Typography variant="body2">AppId: {deployment.appId || "-"}</Typography>
            <Typography variant="body2">AppSecret: {deployment.appSecret ? "••••••" : "-"}</Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* Edit Config Dialog */}
      <Dialog open={cfgOpen} onClose={() => setCfgOpen(false)} fullWidth maxWidth="md">
        <DialogTitle>
          Edit Environment Variables
          <IconButton aria-label="close" onClick={() => setCfgOpen(false)} sx={{ position: "absolute", right: 8, top: 8 }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        <DialogContent dividers>
          <Typography variant="caption" color="textSecondary" sx={{ mb: 1, display: "block" }}>
            Edit environment as JSON object. e.g.
            <pre style={{ margin: 6, padding: 8, background: "#f1f1f1" }}>
                {`{
                "APP_ID":"abc",
                "APP_SECRET":"xyz",
                "SPRING_PROFILES_ACTIVE":"prod"
                }`}
            </pre>
          </Typography>

          <TextField value={envText} onChange={(e) => setEnvText(e.target.value)} multiline rows={10} fullWidth variant="outlined" placeholder='{"KEY":"value"}'/>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setCfgOpen(false)} disabled={saving}>Cancel</Button>
          <Button onClick={handleSaveEnv} variant="contained" disabled={saving}>
            Save
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dockerfile Dialog */}
      <Dialog open={dockerOpen} onClose={() => setDockerOpen(false)} fullWidth maxWidth="md">
        <DialogTitle>
          Edit Dockerfile Content
          <IconButton aria-label="close" onClick={() => setDockerOpen(false)} sx={{ position: "absolute", right: 8, top: 8 }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        <DialogContent dividers>
          <TextField value={dockerText} onChange={(e) => setDockerText(e.target.value)} multiline rows={14} fullWidth variant="outlined" placeholder="FROM openjdk:17-jdk..."/>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setDockerOpen(false)} disabled={saving}>Cancel</Button>
          <Button onClick={handleSaveDocker} variant="contained" disabled={saving}>
            Save Dockerfile
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar open={snack.open} autoHideDuration={5000} onClose={() => setSnack((s) => ({ ...s, open: false }))} message={snack.message} anchorOrigin={{ vertical: "bottom", horizontal: "center" }}/>
    </Box>
  );
}