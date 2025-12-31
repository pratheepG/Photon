import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Avatar, Button, TextField, Box, Typography, Container,
  InputAdornment, IconButton, Paper
} from "@mui/material";
import { Visibility, VisibilityOff, LockOutlined } from "@mui/icons-material";
import api, { setSession } from "../../services/api";

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({ userName: "", password: "" });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleClickShowPassword = () => setShowPassword((prev) => !prev);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await api.post(
        "/authentication/login",
        formData,
        {
          headers: {
            AuthType: "OOB_STATIC_PWD",
            Provider: "SC_IDP",
            Operation: "LOGIN",
          },
        }
      );

      
      if (response.data?.responseData?.accessToken) {
          const { accessToken, refreshToken, expiryDuration } = response.data.responseData;
          setSession({ accessToken, refreshToken, expiryDuration });
          navigate("/identity-service");
      } else {
        alert("Login failed: No token received");
      }
    } catch (err) {
      console.error("Login failed ❌", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
        <Paper elevation={6} sx={{ p: 4, display: "flex", flexDirection: "column", alignItems: "center", borderRadius: 3, width: "100%" }}>
          <Avatar sx={{ m: 1, bgcolor: "primary.main" }}>
            <LockOutlined />
          </Avatar>
          <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
            Welcome Back 👋
          </Typography>

          <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1, width: "100%" }}>
            <TextField required fullWidth id="userName" label="User Name" name="userName" value={formData.userName} onChange={handleChange} margin="normal" />
            <TextField required fullWidth id="password" label="Password" name="password" value={formData.password} onChange={handleChange} margin="normal" type={showPassword ? "text" : "password"}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={handleClickShowPassword} edge="end">
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button type="submit" fullWidth variant="contained" disabled={loading} sx={{ mt: 3, mb: 2, py: 1.5 }}>
              {loading ? "Logging in..." : "Login"}
            </Button>

            <Typography variant="body2" align="center">
              Don’t have an account?{" "}
              <Link to="/signup" style={{ textDecoration: "none", color: "#1976d2" }}>
                Sign Up
              </Link>
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}