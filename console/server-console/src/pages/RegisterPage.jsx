import React, { useState } from 'react';
import { Box, TextField, Button, Typography, FormControlLabel, Checkbox } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const RegisterPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    userName: '',
    password: '',
    phoneNumber: '',
    isEnabled: true,
    sex: 'M', // Default value
    address: {
      addressLabel: 'HOME',
      streetName: '',
      city: '',
      pin: '',
      district: '',
      state: 'Tamil Nadu',
    },
  });

  const handleInputChange = (event) => {
    const { name, value } = event.target;

    // For address fields
    if (name.startsWith('address.')) {
      const addressField = name.split('.')[1];
      setFormData({
        ...formData,
        address: {
          ...formData.address,
          [addressField]: value,
        },
      });
    } else {
      setFormData({
        ...formData,
        [name]: value,
      });
    }
  };

  const handleSubmit = async () => {
    try {
      const payload = {
        ...formData,
        address: [formData.address], // Wrap address in an array as required by the payload structure
      };

      const response = await api.post('/authentication/register', payload, {
        headers: {
          'Content-Type': 'application/json',
        },
      });

      // Handle successful registration (navigate to login, show success message, etc.)
      if (response.data.success) {
        alert('Registration successful! Please login.');
        navigate('/login'); // Redirect to login page after registration
      }
    } catch (error) {
      console.error('Registration failed:', error);
      alert('Registration failed. Please check your input.');
    }
  };

  return (
    <Box sx={{ mt: 3, width: '50%', margin: '0 auto' }}>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Register
      </Typography>

      <TextField
        fullWidth
        margin="normal"
        label="First Name"
        name="firstName"
        value={formData.firstName}
        onChange={handleInputChange}
        required
      />

      <TextField
        fullWidth
        margin="normal"
        label="Last Name"
        name="lastName"
        value={formData.lastName}
        onChange={handleInputChange}
        required
      />

      <TextField
        fullWidth
        margin="normal"
        label="Email (Username)"
        name="userName"
        value={formData.userName}
        onChange={handleInputChange}
        required
      />

      <TextField
        fullWidth
        margin="normal"
        label="Password"
        name="password"
        type="password"
        value={formData.password}
        onChange={handleInputChange}
        required
      />

      <TextField
        fullWidth
        margin="normal"
        label="Phone Number"
        name="phoneNumber"
        value={formData.phoneNumber}
        onChange={handleInputChange}
        required
      />

      <Typography variant="subtitle1" sx={{ mt: 2 }}>
        Address Information
      </Typography>

      <TextField
        fullWidth
        margin="normal"
        label="Street Name"
        name="address.streetName"
        value={formData.address.streetName}
        onChange={handleInputChange}
        required
      />

      <TextField
        fullWidth
        margin="normal"
        label="City"
        name="address.city"
        value={formData.address.city}
        onChange={handleInputChange}
        required
      />

      <TextField
        fullWidth
        margin="normal"
        label="PIN"
        name="address.pin"
        value={formData.address.pin}
        onChange={handleInputChange}
        required
      />

      <TextField
        fullWidth
        margin="normal"
        label="District"
        name="address.district"
        value={formData.address.district}
        onChange={handleInputChange}
        required
      />

      <FormControlLabel
        control={<Checkbox checked={formData.isEnabled} onChange={() => setFormData({ ...formData, isEnabled: !formData.isEnabled })} />}
        label="Is Enabled"
      />

      <Button variant="contained" color="primary" sx={{ mt: 2 }} onClick={handleSubmit}>
        Register
      </Button>
    </Box>
  );
};

export default RegisterPage;