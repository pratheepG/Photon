import React, { useEffect, useState } from 'react';
import {
  Box, Paper, Typography, Button, Grid, TextField, FormControl, InputLabel, Select, MenuItem,
  OutlinedInput, Checkbox, ListItemText, CircularProgress, Alert, Toolbar
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';

const defaultAddress = { type: 'HOME', streetName: '', city: '', pin: '', district: '', state: '' };
const defaultElectronic = { type: 'PHONE', isPrimary: true, value: '', countryCode: '91' };

const accessLevels = [
  { id: 'NONE', description: 'It provides none of the resource access' },
  { id: 'VIEWER', description: 'It provides read-only access for the resources' },
  { id: 'EDITOR', description: 'It provides read and edit access for the resources' },
  { id: 'OWNER', description: 'It provides read and edit access for the owner’s resources' },
  { id: 'ADMIN', description: 'It provides global access for all resources' },
  { id: 'TENANT_ADMIN', description: 'It provide tenant-level access for all resources' },
];

export default function CreateUserPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    userName: '',
    sex: 'MALE',
    dob: '',
    tenantId: '',
    accessLevel: '',
    roles: [],
    electronicAddress: [{ ...defaultElectronic }],
    address: [{ ...defaultAddress }],
  });

  const [rolesList, setRolesList] = useState([]);
  const [tenants, setTenants] = useState([]);
  const [loadingMeta, setLoadingMeta] = useState(true);
  const [creating, setCreating] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');


 useEffect(() => {
    const loadTenants = async () => {
        setLoadingMeta(true);
        try {
        const tenantsResp = await api.get('/tenants?pageNumber=0&pageSize=100');
        if (tenantsResp.data?.success) {
            const data = tenantsResp.data.responseData || [];
            // normalize tenant objects: they have tenantId and name
            const normalized = data.map(t => ({
            id: t.tenantId || t.id || '',
            name: t.name || t.tenantName || t.description || t.tenantId,
            raw: t
            }));
            setTenants(normalized);
            // default first tenant if none selected
            if (normalized.length && !form.tenantId) {
            setForm(prev => ({ ...prev, tenantId: normalized[0].id }));
            }
        }
        } catch (err) {
        console.warn('Failed to load tenants', err);
        } finally {
        setLoadingMeta(false);
        }
    };
    loadTenants();
  }, []);


  useEffect(() => {
    if (!form.accessLevel) return;
    fetchRolesByAccessLevel(form.accessLevel);
  }, [form.accessLevel]);

  const fetchRolesByAccessLevel = async (accessLevel) => {
    try {
        const resp = await api.get(`/role/lookup?accessLevel=${accessLevel}&pageNumber=0&pageSize=10`);

        if (!resp.data?.success) {
        setRolesList([]);
        return;
        }

        const payload = resp.data.responseData || {};

        const roles = [];

        Object.keys(payload).forEach(idpKey => {
        const arr = payload[idpKey] || [];
        arr.forEach(item => {
            const entries = Object.entries(item);
            entries.forEach(([k, v]) => {
            const numericId = Number(k);
            roles.push({
                id: isNaN(numericId) ? k : numericId,
                roleId: v,
                name: v,
                idp: idpKey
            });
            });
        });
        });

        setRolesList(roles);
    } catch (err) {
        console.error('Failed to fetch roles by access level', err);
        setRolesList([]);
    }
  };


  const handleChange = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const handleAddressChange = (index, field, value) => {
    setForm((prev) => {
      const addresses = [...prev.address];
      addresses[index] = { ...addresses[index], [field]: value };
      return { ...prev, address: addresses };
    });
  };

  const handleElectronicChange = (index, field, value) => {
    setForm((prev) => {
      const eAddr = [...prev.electronicAddress];
      eAddr[index] = { ...eAddr[index], [field]: value };
      return { ...prev, electronicAddress: eAddr };
    });
  };

  const addAddress = () => setForm((prev) => ({ ...prev, address: [...prev.address, { ...defaultAddress }] }));
  const removeAddress = (i) => setForm((prev) => ({ ...prev, address: prev.address.filter((_, idx) => idx !== i) }));
  const addElectronic = () => setForm((prev) => ({ ...prev, electronicAddress: [...prev.electronicAddress, { ...defaultElectronic }] }));
  const removeElectronic = (i) => setForm((prev) => ({ ...prev, electronicAddress: prev.electronicAddress.filter((_, idx) => idx !== i) }));

  const validate = () => {
    if (!form.firstName.trim()) return 'First name required';
    if (!form.userName.trim()) return 'User name required';
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.userName)) return 'User name must be a valid email';
    if (!form.accessLevel) return 'Please select an Access Level';
    if (!form.roles.length) return 'Please select at least one Role';
    for (const ea of form.electronicAddress) {
      if (ea.value && /[^0-9]/.test(ea.value)) return 'Phone must be digits only';
    }
    return null;
  };

  const handleCreate = async () => {
    const v = validate();
    if (v) {
      setErrorMsg(v);
      return;
    }
    setErrorMsg('');
    setCreating(true);

    const payload = {
      firstName: form.firstName,
      lastName: form.lastName,
      userName: form.userName,
      sex: form.sex,
      dob: form.dob,
      tenantId: form.tenantId,
      roles: form.roles,
      electronicAddress: form.electronicAddress.map(e => ({
        type: e.type,
        isPrimary: !!e.isPrimary,
        value: e.value,
        countryCode: e.countryCode || '',
      })),
      address: form.address.map(a => ({
        type: a.type,
        streetName: a.streetName,
        city: a.city,
        pin: a.pin,
        district: a.district,
        state: a.state,
      })),
    };

    try {
      const resp = await api.post('/user/create-user', payload);
      if (resp.data?.success) {
        navigate('/users');
      } else {
        setErrorMsg(resp.data?.message || 'Failed to create user');
      }
    } catch (err) {
      console.error('Create user failed', err);
      setErrorMsg('Create user failed. See console for details.');
    } finally {
      setCreating(false);
    }
  };

  if (loadingMeta) {
    return (
      <Box sx={{ width: '90vw', padding: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ width: '90vw', minHeight: '80vh', padding: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Create User</Typography>
        <Button variant="outlined" onClick={() => navigate('/users')}>Back to list</Button>
      </Box>

      <Paper sx={{ p: 2 }}>
        {errorMsg && <Alert severity="error" sx={{ mb: 2 }}>{errorMsg}</Alert>}

        <Grid container spacing={2}>
          {/* Basic Info */}
          <Grid item xs={12} sm={6}>
            <TextField label="First Name" value={form.firstName} onChange={(e) => handleChange('firstName', e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Last Name" value={form.lastName} onChange={(e) => handleChange('lastName', e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="User Name (Email)" value={form.userName} onChange={(e) => handleChange('userName', e.target.value)} fullWidth />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Sex</InputLabel>
              <Select value={form.sex} label="Sex" onChange={(e) => handleChange('sex', e.target.value)}>
                <MenuItem value="MALE">Male</MenuItem>
                <MenuItem value="FEMALE">Female</MenuItem>
                <MenuItem value="OTHER">Other</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} sm={6}>
            <TextField label="Date of Birth" type="date" InputLabelProps={{ shrink: true }} value={form.dob} onChange={(e) => handleChange('dob', e.target.value)} fullWidth />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Tenant</InputLabel>
              <Select value={form.tenantId} label="Tenant" onChange={(e) => handleChange('tenantId', e.target.value)}>
                {tenants.length ? tenants.map(t => <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>): <MenuItem value="">(no tenants)</MenuItem>}
              </Select>
            </FormControl>
          </Grid>

          {/* Access Level */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Access Level</InputLabel>
              <Select value={form.accessLevel} label="Access Level" onChange={(e) => handleChange('accessLevel', e.target.value)}>
                {accessLevels.map((a) => (
                  <MenuItem key={a.id} value={a.id}>
                    {a.id} — {a.description}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Roles */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Roles</InputLabel>
              <Select multiple value={form.roles} onChange={(e) => handleChange('roles', e.target.value)} input={<OutlinedInput label="Roles" />}
               renderValue={(selected) => (rolesList.filter(r => selected.includes(r.id)).map(r => r.name || r.roleId).join(', '))}>
                {rolesList.length ? rolesList.map((r) => (
                <MenuItem key={r.id} value={r.id}>
                    <Checkbox checked={form.roles.indexOf(r.id) > -1} />
                    <ListItemText primary={r.name || r.roleId} />
                </MenuItem>
                )) : <MenuItem disabled>(no roles found)</MenuItem>}
              </Select>
            </FormControl>
          </Grid>

          {/* Electronic Addresses */}
          <Grid item xs={12}><Typography variant="subtitle1">Electronic Addresses</Typography></Grid>
          {form.electronicAddress.map((ea, idx) => (
            <React.Fragment key={idx}>
              <Grid item xs={12} sm={3}>
                <FormControl fullWidth>
                  <InputLabel>Type</InputLabel>
                  <Select value={ea.type} label="Type" onChange={(e) => handleElectronicChange(idx, 'type', e.target.value)}>
                    <MenuItem value="PHONE">PHONE</MenuItem>
                    <MenuItem value="EMAIL">EMAIL</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="Country Code" value={ea.countryCode} onChange={(e) => handleElectronicChange(idx, 'countryCode', e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField label="Value" value={ea.value} onChange={(e) => handleElectronicChange(idx, 'value', e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} sm={2} sx={{ display: 'flex', alignItems: 'center' }}>
                <Button onClick={() => (idx === 0 ? addElectronic() : removeElectronic(idx))} variant="outlined">
                  {idx === 0 ? 'Add' : 'Remove'}
                </Button>
              </Grid>
            </React.Fragment>
          ))}

          {/* Address */}
          <Grid item xs={12}><Typography variant="subtitle1">Addresses</Typography></Grid>
          {form.address.map((a, idx) => (
            <React.Fragment key={idx}>
              <Grid item xs={12} sm={3}>
                <FormControl fullWidth>
                  <InputLabel>Type</InputLabel>
                  <Select value={a.type} label="Type" onChange={(e) => handleAddressChange(idx, 'type', e.target.value)}>
                    <MenuItem value="HOME">HOME</MenuItem>
                    <MenuItem value="WORK">WORK</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={5}>
                <TextField label="Street" value={a.streetName} onChange={(e) => handleAddressChange(idx, 'streetName', e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField label="City" value={a.city} onChange={(e) => handleAddressChange(idx, 'city', e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="Pin" value={a.pin} onChange={(e) => handleAddressChange(idx, 'pin', e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="District" value={a.district} onChange={(e) => handleAddressChange(idx, 'district', e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="State" value={a.state} onChange={(e) => handleAddressChange(idx, 'state', e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} sm={3} sx={{ display: 'flex', alignItems: 'center' }}>
                <Button onClick={() => (idx === 0 ? addAddress() : removeAddress(idx))} variant="outlined">
                  {idx === 0 ? 'Add' : 'Remove'}
                </Button>
              </Grid>
            </React.Fragment>
          ))}
        </Grid>

        <Toolbar sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
          <Button onClick={() => navigate('/users')} sx={{ mr: 2 }}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate} disabled={creating}>
            {creating ? 'Creating...' : 'Create User'}
          </Button>
        </Toolbar>
      </Paper>
    </Box>
  );
}