import React, { useState, useEffect } from "react";
import {
  Box, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, IconButton, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Checkbox, FormControlLabel, MenuItem
} from "@mui/material";
import { Add, Delete, Edit } from "@mui/icons-material";
import api from "../services/api";
import { useNavigate } from 'react-router-dom';

const FormListPage = () => {
  const navigate = useNavigate();
  const [forms, setForms] = useState([]);
  const [fields, setFields] = useState([]);
  const [open, setOpen] = useState(false);
  const [formName, setFormName] = useState("");
  const [formItems, setFormItems] = useState([]);
  const [editMode, setEditMode] = useState(false);
  const [editFormId, setEditFormId] = useState(null);

  useEffect(() => {
    fetchForms();
    fetchFields();
  }, []);

  const fetchForms = async () => {
    const res = await api.get("/onboarding/form");
    setForms(res.data.responseData || []);
  };

  const fetchFields = async () => {
    const res = await api.get("/onboarding/field");
    setFields(res.data.responseData || []);
  };

  const handleAddField = () => {
    setFormItems(prev => [...prev, {
      type: "FIELD",
      referenceId: "",
      dtoFieldName: "",
      isRequired: true,
      isCollection: false
    }]);
  };

  const handleAddGroup = () => {
    setFormItems(prev => [...prev, {
      type: "GROUP",
      dtoFieldName: "",
      isRequired: true,
      isCollection: false,
      data: []
    }]);
  };

  const handleFieldChange = (index, key, value, parentIndex = null) => {
    const update = [...formItems];
    if (parentIndex !== null) {
      update[parentIndex].data[index][key] = value;
    } else {
      update[index][key] = value;
    }
    setFormItems(update);
  };

  const handleGroupFieldAdd = (parentIndex) => {
    const update = [...formItems];
    update[parentIndex].data.push({
      type: "FIELD",
      referenceId: "",
      dtoFieldName: "",
      isRequired: true,
      isCollection: false
    });
    setFormItems(update);
  };

  const handleSave = async () => {
    const payload = {
      name: formName,
      fields: formItems
    };

    if (editMode) {
      await api.put(`/onboarding/form/${editFormId}`, payload);
    } else {
      await api.post("/onboarding/form", payload);
    }

    fetchForms();
    handleClose();
  };

  const handleClose = () => {
    setOpen(false);
    setFormName("");
    setFormItems([]);
    setEditMode(false);
    setEditFormId(null);
  };

  const handleEdit = (form) => {
    setFormName(form.name);
    setFormItems(form.fields);
    setEditFormId(form.id);
    setEditMode(true);
    setOpen(true);
  };

  const handleDelete = async (id) => {
    await api.delete(`/onboarding/form/${id}`);
    fetchForms();
  };

  return (
    <Box sx={{ width: '80vw', minHeight: '100vh', padding: '20px', backgroundColor: '#f5f5f5', position: 'relative' }}>
      <Button onClick={() => setOpen(true)} startIcon={<Add />}>
        Create Form
      </Button>

      <TableContainer component={Paper} sx={{ mt: 3 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Form Name</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {forms.map((form) => (
              <TableRow key={form.id}>
                <TableCell>{form.name}</TableCell>
                <TableCell>
                  {/* <IconButton onClick={() => handleEdit(form)}><Edit /></IconButton> */}
                  <IconButton onClick={() => navigate(`/form/${form.id}`)}><Edit /></IconButton>
                  <IconButton onClick={() => handleDelete(form.id)}><Delete /></IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
        <DialogTitle>{editMode ? "Edit Form" : "Create Form"}</DialogTitle>
        <DialogContent>
          <TextField fullWidth margin="normal" label="Form Name" value={formName} onChange={(e) => setFormName(e.target.value)} />

          {formItems.map((item, idx) => (
            <Box key={idx} sx={{ p: 2, border: "1px solid #ccc", mb: 2 }}>
              {item.type === "FIELD" ? (
                <>
                  <TextField select fullWidth label="Select Field" value={item.referenceId}
                    onChange={(e) => handleFieldChange(idx, "referenceId", e.target.value)}
                    margin="dense">
                    {fields.map(f => (
                      <MenuItem key={f.id} value={f.id}>{f.name}</MenuItem>
                    ))}
                  </TextField>
                  <TextField fullWidth margin="dense" label="DTO Field Name" value={item.dtoFieldName}
                    onChange={(e) => handleFieldChange(idx, "dtoFieldName", e.target.value)} />
                  <FormControlLabel control={
                    <Checkbox checked={item.isRequired}
                      onChange={(e) => handleFieldChange(idx, "isRequired", e.target.checked)} />
                  } label="Required" />
                </>
              ) : (
                <>
                  <TextField fullWidth margin="dense" label="Group DTO Name" value={item.dtoFieldName}
                    onChange={(e) => handleFieldChange(idx, "dtoFieldName", e.target.value)} />
                  <FormControlLabel control={
                    <Checkbox checked={item.isRequired}
                      onChange={(e) => handleFieldChange(idx, "isRequired", e.target.checked)} />
                  } label="Required" />
                  <FormControlLabel control={
                    <Checkbox checked={item.isCollection}
                      onChange={(e) => handleFieldChange(idx, "isCollection", e.target.checked)} />
                  } label="Is Collection" />

                  <Button onClick={() => handleGroupFieldAdd(idx)} sx={{ mt: 1 }}>Add Field to Group</Button>

                  {(item.data || []).map((f, i) => (
                    <Box key={i} sx={{ mt: 1, pl: 2 }}>
                      <TextField select fullWidth label="Select Field" value={f.referenceId}
                        onChange={(e) => handleFieldChange(i, "referenceId", e.target.value, idx)}
                        margin="dense">
                        {fields.map(fd => (
                          <MenuItem key={fd.id} value={fd.id}>{fd.name}</MenuItem>
                        ))}
                      </TextField>
                      <TextField fullWidth label="DTO Field Name" margin="dense"
                        value={f.dtoFieldName} onChange={(e) => handleFieldChange(i, "dtoFieldName", e.target.value, idx)} />
                      <FormControlLabel control={
                        <Checkbox checked={f.isRequired}
                          onChange={(e) => handleFieldChange(i, "isRequired", e.target.checked, idx)} />
                      } label="Required" />
                    </Box>
                  ))}
                </>
              )}
            </Box>
          ))}

          <Box display="flex" gap={2} mt={2}>
            <Button variant="outlined" onClick={handleAddField}>Add Field</Button>
            <Button variant="outlined" onClick={handleAddGroup}>Add Group</Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button variant="contained" onClick={handleSave}>Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default FormListPage;