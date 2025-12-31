import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box, Typography, Paper, IconButton, Divider, Button, Tooltip, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField
} from "@mui/material";
import { Delete, Edit, ArrowBack } from "@mui/icons-material";
import api from "../services/api";
import FieldFormDialog from "./FieldFormDialog";
import ConfirmDialog from "./ConfirmDialog";
import { Accordion, AccordionSummary, AccordionDetails, List, ListItem, ListItemText } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import FormStructureDialog from "./FormStructureDialog";

const FormDetailsPage = () => {
  const { formId } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(null);
  const [selectedItem, setSelectedItem] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [editOpen, setEditOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [mappingEditOpen, setMappingEditOpen] = useState(false);
  const [mappingItem, setMappingItem] = useState(null);
  const [fieldDialogOpen, setFieldDialogOpen] = useState(false);
  const [fieldInitial, setFieldInitial] = useState(null);
  const [structureDialogOpen, setStructureDialogOpen] = useState(false);


  useEffect(() => {
    fetchForm();
  }, [formId]);

  const fetchForm = async () => {
    try {
      const res = await api.get(`/onboarding/form/${formId}`);
      setForm(res.data.responseData);
    } catch (e) {
      console.error("Fetch error:", e);
    }
  };

  const transformBackendData = (form) => {
    return {
        id: form.id,
        name: form.name,
        fields: (form.fields || []).map(item => {
        if (item.type === "FIELD") {
            return {
            type: "FIELD",
            referenceId: item.field.id,
            dtoFieldName: item.dtoFieldName,
            required: item.required,
            isCollection: item.field.collection
            };
        } else if (item.type === "GROUP") {
            return {
            type: "GROUP",
            dtoFieldName: item.dtoFieldName,
            required: item.required,
            isCollection: item.group.isCollection,
            data: (item.group.fields || []).map(f => ({
                type: "FIELD",
                referenceId: f.field.id,
                dtoFieldName: f.field.name,
                required: f.required,
                isCollection: f.field.collection
            }))
            };
        }
        return null;
        }).filter(Boolean)
    };
  };

  const handleEditMapping = (item) => {
    setMappingItem(item);
    setMappingEditOpen(true);
  };

  const handleMappingSave = async () => {
    setMappingEditOpen(false);
    setMappingItem(null);
    fetchForm();
  };


  const handleEdit = (item) => {
    setSelectedItem(item);
    setEditOpen(true);
  };

  const handleDelete = (item) => {
    setDeleteTarget(item);
    setConfirmOpen(true);
  };

  const confirmDelete = async () => {
    try {
      const refId = deleteTarget?.field?.id || deleteTarget?.group?.id;
      const isField = !!deleteTarget?.field;
      await api.delete(`/onboarding/${isField ? "field" : "group"}/${refId}`);
      fetchForm();
    } catch (e) {
      console.error("Delete failed:", e);
    }
    setConfirmOpen(false);
  };

  const handleEditDialogClose = () => {
    setSelectedItem(null);
    setEditOpen(false);
    fetchForm();
  };

  if (!form) return <Typography>Loading...</Typography>;

  return (
    <Box sx={{width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f5f5f5'}}>
        <Box display="flex" alignItems="center" justifyContent="space-between" mb={3}>
            {/* Left section: back button + title */}
            <Box display="flex" alignItems="center">
                <IconButton onClick={() => navigate(-1)}><ArrowBack /></IconButton>
                <Typography variant="h5" ml={1}>{form.name}</Typography>
            </Box>

            {/* Right section: button */}
            <Button variant="outlined" onClick={() => setStructureDialogOpen(true)}>
                Add Field / Group
            </Button>
        </Box>

      {form.fields.map((el, idx) => (
        <Paper key={idx} sx={{ mb: 2, p: 2, borderLeft: "4px solid #1976d2" }}>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="subtitle1">
              {el.type === "FIELD" ? `Field: ${el.field.name}` : `Group: ${el.group.name}`}
            </Typography>
            <Box>
              <Tooltip title="Edit">
                <IconButton onClick={() => handleEditMapping(el)}><Edit /></IconButton>
              </Tooltip>
              <Tooltip title="Delete">
                <IconButton onClick={() => handleDelete(el)}><Delete /></IconButton>
              </Tooltip>
            </Box>
          </Box>
          <Divider sx={{ my: 1 }} />
          {el.type === "FIELD" ? (
            <>
                <Typography variant="body2">Type: {el.field.type}</Typography>
                <Typography variant="body2">Required: {el.required ? "Yes" : "No"}</Typography>

                <Accordion sx={{ mt: 3 }}>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="subtitle2">Field Configuration</Typography>
                    </AccordionSummary>
                    <IconButton onClick={() => handleEdit(el)}><Edit /></IconButton>
                    <AccordionDetails>
                        <List dense>
                            {Object.entries(el.field.config || {}).map(([key, value]) => (
                                <ListItem key={key} disablePadding>
                                    <ListItemText
                                        primary={key}
                                        secondary={
                                        key === "lookupData" ? <pre style={{ margin: 0 }}>{JSON.stringify(value, null, 2)}</pre>
                                            : typeof value === "object" ? JSON.stringify(value): String(value)
                                        }
                                    />
                                </ListItem>
                            ))}
                        </List>
                    </AccordionDetails>
                </Accordion>
            </>
            ) : (
            <>
              <Typography variant="body2">Collection: {el.group.isCollection ? "Yes" : "No"}</Typography>
              {(el.group.fields || []).map((gf, gi) => (
                <Paper key={gi} variant="outlined" sx={{ mt: 1, p: 1 }}>
                  <Typography variant="subtitle2">Field: {gf.field.name}</Typography>
                  <Typography variant="body2">Required: {gf.required ? "Yes" : "No"}</Typography>
                  <pre>{JSON.stringify(gf.field.config, null, 2)}</pre>
                </Paper>
              ))}
            </>
          )}
        </Paper>
      ))}

      {/* Confirmation Dialog */}
      <ConfirmDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={confirmDelete}
        title="Delete Confirmation"
        content="Are you sure you want to delete this item?"
      />

      <Dialog open={mappingEditOpen} onClose={() => setMappingEditOpen(false)}>
        <DialogTitle>Edit Mapping</DialogTitle>
        <DialogContent>
            <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <Typography variant="body2">This only edits the DTO mapping, not field config.</Typography>
            <TextField
                label="DTO Field Name"
                value={mappingItem?.dtoFieldName || ''}
                onChange={e =>
                setMappingItem(prev => ({ ...prev, dtoFieldName: e.target.value }))
                }
                fullWidth
            />
            <Box display="flex" alignItems="center">
                <Typography>Required:</Typography>
                <input
                type="checkbox"
                checked={mappingItem?.required || false}
                onChange={e =>
                    setMappingItem(prev => ({ ...prev, required: e.target.checked }))
                }
                style={{ marginLeft: 8 }}
                />
            </Box>
            </Box>
        </DialogContent>
        <DialogActions>
            <Button onClick={() => setMappingEditOpen(false)}>Cancel</Button>
            <Button variant="contained" onClick={handleMappingSave}>Save</Button>
        </DialogActions>
      </Dialog>

      {/* Field Edit Dialog */}
      {selectedItem?.type === "FIELD" && (
        <FieldFormDialog
          open={editOpen}
          onClose={handleEditDialogClose}
          onSaved={handleEditDialogClose}
          initial={{
            id: selectedItem.field.id,
            name: selectedItem.field.name,
            type: selectedItem.field.type,
            fieldConfig: selectedItem.field.config
          }}
        />
      )}

      <FieldFormDialog open={fieldDialogOpen} onClose={() => setFieldDialogOpen(false)} onSaved={() => { setFieldDialogOpen(false); fetchForm(); }} initial={fieldInitial} />
      <FormStructureDialog open={structureDialogOpen} onClose={() => setStructureDialogOpen(false)} initial={transformBackendData(form)} onSaved={fetchForm}/>

    </Box>
  );
};

export default FormDetailsPage;