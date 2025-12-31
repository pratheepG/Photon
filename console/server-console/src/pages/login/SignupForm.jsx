import { useState } from "react";
import { Link } from "react-router-dom";
import { Avatar, Button, TextField, Box, Typography, Container, InputAdornment, IconButton, Paper, MenuItem, Grid} from "@mui/material";
import { Visibility, VisibilityOff, PersonAddOutlined } from "@mui/icons-material";
import api from '../../services/api';

export default function SignupForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    userName: "",
    password: "",
    sex: "",
    dob: "",
    // Added tenant object
    tenant: {
      name: "",
      description: "",
    },
    address: {
      type: "HOME",
      streetName: "",
      city: "",
      pin: "",
      district: "",
      state: "",
    },
  });

  const [loading, setLoading] = useState(false);

  const handleClickShowPassword = () => setShowPassword((prev) => !prev);

  const handleChange = (e) => {
    const { name, value } = e.target;

    // Logic to handle nested address fields
    if (["streetName", "city", "pin", "district", "state"].includes(name)) {
      setFormData((prev) => ({
        ...prev,
        address: { ...prev.address, [name]: value },
      }));
    // New logic to handle nested tenant fields
    } else if (["tenantName", "tenantDescription"].includes(name)) {
      const tenantKey = name === "tenantName" ? "name" : "description";
      setFormData((prev) => ({
        ...prev,
        tenant: { ...prev.tenant, [tenantKey]: value },
      }));
    // Logic for top-level fields
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // The payload structure requires 'address' to be an array
      const payload = { 
        ...formData, 
        address: [formData.address] 
      };

      const response = await api.post(
        "/user/register-first-user",
        payload
      );

      alert("Signup successful!");
      console.log("Response:", response.data);
    } catch (error) {
      console.error("Signup failed:", error);
      alert("Signup failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="md">
      <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
        <Paper elevation={6} sx={{ p: 4, display: "flex", flexDirection: "column", alignItems: "center", borderRadius: 3, width: "100%"}}>
          <Avatar sx={{ m: 1, bgcolor: "secondary.main" }}>
            <PersonAddOutlined />
          </Avatar>
          <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
            Create Account
          </Typography>

          <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1, width: "100%" }}>
            <Grid container spacing={2}>
              {/* Name Fields */}
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="firstName" label="First Name" name="firstName" value={formData.firstName} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="lastName" label="Last Name" name="lastName" value={formData.lastName} onChange={handleChange} />
              </Grid>

              {/* User Name & Password */}
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="userName" label="User Name (Email)" name="userName" value={formData.userName} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="password" label="Password" name="password" value={formData.password} onChange={handleChange} type={showPassword ? "text" : "password"}
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
              </Grid>

              {/* Sex & DOB */}
              <Grid item xs={12} sm={6}>
                <TextField select required fullWidth id="sex" label="Sex" name="sex" value={formData.sex} onChange={handleChange}>
                  <MenuItem value="MALE">Male</MenuItem>
                  <MenuItem value="FEMALE">Female</MenuItem>
                  <MenuItem value="OTHERS">Other</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="dob" label="Date of Birth" name="dob" type="date" InputLabelProps={{ shrink: true }} value={formData.dob} onChange={handleChange}/>
              </Grid>

              {/* --- Organization (Tenant) Details --- */}
              <Grid item xs={12}>
                <Typography variant="subtitle1" sx={{ mt: 2, mb: -1 }}>
                  Organization Details
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="tenantName" label="Organization Name" name="tenantName" value={formData.tenant.name} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth id="tenantDescription" label="Organization Description" name="tenantDescription" value={formData.tenant.description} onChange={handleChange} />
              </Grid>


              {/* --- Address Details --- */}
              <Grid item xs={12}>
                <Typography variant="subtitle1" sx={{ mt: 2, mb: -1 }}>
                  Address Details
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <TextField required fullWidth id="streetName" label="Street Name" name="streetName" value={formData.address.streetName} onChange={handleChange} />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="city" label="City" name="city" value={formData.address.city} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="pin" label="Pin Code" name="pin" value={formData.address.pin} onChange={handleChange} />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="district" label="District" name="district" value={formData.address.district} onChange={handleChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth id="state" label="State" name="state" value={formData.address.state} onChange={handleChange} />
              </Grid>
            </Grid>

            {/* Submit */}
            <Button type="submit" fullWidth variant="contained" color="secondary" disabled={loading} sx={{ mt: 3, mb: 2, py: 1.5 }}>
              {loading ? "Signing Up..." : "Sign Up"}
            </Button>

            <Typography variant="body2" align="center">
              Already have an account?{" "}
              <Link to="/login" style={{ textDecoration: "none", color: "#1976d2" }}>
                Login
              </Link>
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}