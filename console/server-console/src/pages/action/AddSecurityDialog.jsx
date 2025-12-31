// import { useState, useEffect } from 'react';
// import { Dialog, DialogActions, DialogContent, DialogTitle, Button, Autocomplete, TextField, Select, MenuItem, FormControl, InputLabel } from '@mui/material';
// import api from '../../services/api';

// const AddSecurityDialog = ({ open, onClose, onAddRoles, accessLevel }) => {
//   const [idpList, setIdpList] = useState([]);
//   const [rolesByIdp, setRolesByIdp] = useState({});
//   const [selectedIdp, setSelectedIdp] = useState('');
//   const [roleOptions, setRoleOptions] = useState([]);
//   const [selectedRoles, setSelectedRoles] = useState([]);
//   const [loading, setLoading] = useState(false);

//   useEffect(() => {
//     if (open) {
//       fetchIdpRoles();
//       setSelectedIdp('');
//       setSelectedRoles([]);
//     }
//   }, [open]);

//   const fetchIdpRoles = async () => {
//     setLoading(true);
//     try {
//       const res = await api.get(`/role/lookup?accessLevel=${accessLevel}&pageNumber=0&pageSize=10`);
      
//       if (res.data.success) {
//         const data = res.data.responseData || {};
//         setRolesByIdp(data);
//         setIdpList(Object.keys(data));
//       }
//     } catch (err) {
//       console.error('Error fetching IDP roles:', err);
//     } finally {
//       setLoading(false);
//     }
//   };

//   const handleIdpChange = (e) => {
//     const idp = e.target.value;
//     setSelectedIdp(idp);
//     setSelectedRoles([]);

//     const idpRolesArray = rolesByIdp[idp] || [];
//     const options = [];

//     idpRolesArray.forEach(roleObject => {
//       Object.entries(roleObject).forEach(([id, name]) => {
//         options.push({
//           roleId: id,
//           roleName: name,
//         });
//       });
//     });

//     setRoleOptions(options);
//   };

//   const handleRolesChange = (event, values) => {
//     setSelectedRoles(values);
//   };

//   const handleSubmit = () => {
//     if (!selectedIdp || selectedRoles.length === 0) {
//       alert('Please select IDP and at least one Role');
//       return;
//     }
//     // Call parent with selected idp and roles
//     onAddRoles({
//       idp: selectedIdp,
//       roles: selectedRoles, // array of {roleId, roleName}
//     });
//     onClose();
//   };

//   return (
//     <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
//       <DialogTitle>Add Security</DialogTitle>
//       <DialogContent>
//         <FormControl fullWidth margin="normal" disabled={loading}>
//           <InputLabel>IDP</InputLabel>
//           <Select
//             value={selectedIdp}
//             onChange={handleIdpChange}
//             label="IDP"
//           >
//             {idpList.map(idp => (
//               <MenuItem key={idp} value={idp}>{idp}</MenuItem>
//             ))}
//           </Select>
//         </FormControl>
//         {selectedIdp && (
//           <Autocomplete
//             multiple
//             disableCloseOnSelect
//             options={roleOptions}
//             getOptionLabel={opt => `${opt.roleName} (${opt.roleId})`}
//             value={selectedRoles}
//             onChange={handleRolesChange}
//             renderInput={params => (
//               <TextField {...params} label="Roles" placeholder="Select roles" margin="normal" />
//             )}
//             sx={{ mt: 2 }}
//           />
//         )}
//       </DialogContent>
//       <DialogActions>
//         <Button onClick={onClose}>Cancel</Button>
//         <Button onClick={handleSubmit} variant="contained" color="primary">
//           Add
//         </Button>
//       </DialogActions>
//     </Dialog>
//   );
// };

// export default AddSecurityDialog;