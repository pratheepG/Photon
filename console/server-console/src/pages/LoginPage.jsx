import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { v4 as uuidv4 } from 'uuid'; // Import UUID library
import api from '../services/api';

const LoginPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    userName: '',
    password: '',
    deviceInfo: {
      deviceId: '', // Initially empty, will be dynamically set
      deviceType: 'web browser', // You can determine device type dynamically as well
    },
  });

  // Function to detect the device ID dynamically (here using UUID)
  const generateDeviceId = () => {
    // Check if deviceId is already saved in localStorage (persisting it)
    let deviceId = localStorage.getItem('deviceId');
    if (!deviceId) {
      // If not available, generate a new one and store it
      deviceId = uuidv4();
      localStorage.setItem('deviceId', deviceId);
    }
    return deviceId;
  };

  useEffect(() => {
    // Set the generated device ID in formData when component loads
    setFormData((prevData) => ({
      ...prevData,
      deviceInfo: {
        ...prevData.deviceInfo,
        deviceId: generateDeviceId(),
      },
    }));
  }, []); // Runs once when component mounts

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async () => {
    try {
      const response = await api.post('/authentication/login', formData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });

      // Handle successful login (store token, navigate, etc.)
      if (response.data.success) {
        alert('Login successful!');
        navigate('/dashboard'); // Redirect to the dashboard after login
      }
    } catch (error) {
      console.error('Login failed:', error);
      alert('Login failed. Please check your credentials.');
    }
  };

  return (
    <Box sx={{ mt: 3, width: '50%', margin: '0 auto' }}>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Login
      </Typography>

      <TextField
        fullWidth
        margin="normal"
        label="Username"
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

      <Button variant="contained" color="primary" sx={{ mt: 2 }} onClick={handleSubmit}>
        Login
      </Button>
    </Box>
  );
};

export default LoginPage;