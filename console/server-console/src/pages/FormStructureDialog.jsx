import { useEffect, useState } from "react";
import { Box, Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Checkbox, FormControlLabel } from "@mui/material";
import api from "../services/api";

const FormStructureDialog = ({ open, onClose, initial = null, onSaved }) => {
  const [formName, setFormName] = useState("");
  const [formItems, setFormItems] = useState([]);
  const [fields, setFields] = useState([]);
  const [originalItems, setOriginalItems] = useState([]);

  useEffect(() => {
    if (open) {
      setFormName(initial?.name || "");
      const mappedFields = (initial?.fields || []).map((item) => {
        if (item.type === "GROUP") {
          return {
            ...item,
            data: item.field || [],
          };
        }
        return item;
      });
      setFormItems(mappedFields);
      setOriginalItems(JSON.parse(JSON.stringify(mappedFields)));
      fetchFields();
    }
  }, [open, initial]);

  const getModifiedItems = () => {
    const changes = [];

    const isFieldModified = (a, b) =>
      a.referenceId !== b.referenceId ||
      a.dtoFieldName !== b.dtoFieldName ||
      a.required !== b.required;

    const isGroupModified = (a, b) =>
      a.dtoFieldName !== b.dtoFieldName ||
      a.required !== b.required ||
      a.isCollection !== b.isCollection;

    const originalMap = new Map();
    originalItems.forEach((item) => {
      const key =
        item.type === "FIELD"
          ? `FIELD-${item.referenceId}`
          : `GROUP-${item.group?.id || item.dtoFieldName}`;
      originalMap.set(key, item);
    });

    formItems.forEach((item) => {
      const key =
        item.type === "FIELD"
          ? `FIELD-${item.referenceId}`
          : `GROUP-${item.group?.id || item.dtoFieldName}`;

      const original = originalMap.get(key);

      if (!original) {
        changes.push(item);
      } else if (item.type === "FIELD" && isFieldModified(item, original)) {
        changes.push(item);
      } else if (item.type === "GROUP" && isGroupModified(item, original)) {
        const modifiedFields = [];
        (item.data || []).forEach((inner) => {
          const match = (original.data || []).find(
            (o) => o.referenceId === inner.referenceId
          );
          if (!match || isFieldModified(inner, match)) {
            modifiedFields.push(inner);
          }
        });

        if (modifiedFields.length) {
          changes.push({ ...item, data: modifiedFields });
        }
      }
    });

    return changes;
  };

  const fetchFields = async () => {
    try {
      const res = await api.get("/onboarding/field");
      setFields(res.data.responseData || []);
    } catch (e) {
      console.error("Failed to fetch fields", e);
    }
  };

  const handleAddField = () => {
    setFormItems((prev) => [
      ...prev,
      {
        type: "FIELD",
        referenceId: "",
        dtoFieldName: "",
        required: true,
        isCollection: false,
      },
    ]);
  };

  const handleAddGroup = () => {
    setFormItems((prev) => [
      ...prev,
      {
        type: "GROUP",
        dtoFieldName: "",
        required: true,
        isCollection: false,
        data: [],
      },
    ]);
  };

  const handleGroupFieldAdd = (parentIndex) => {
    setFormItems((prev) => {
      const updated = [...prev];
      updated[parentIndex].data.push({
        type: "FIELD",
        referenceId: "",
        dtoFieldName: "",
        required: true,
        isCollection: false,
      });
      return updated;
    });
  };

  const handleFieldChange = (index, key, value, parentIndex = null) => {
    setFormItems((prev) => {
      const updated = [...prev];
      if (parentIndex !== null) {
        updated[parentIndex].data[index][key] = value;
      } else {
        updated[index][key] = value;
      }
      return updated;
    });
  };

  const handleSave = async () => {
    const updatedFields = getModifiedItems();

    if (updatedFields.length === 0) {
    console.log("No changes to save.");
    onClose();
    return;
    }

    const payload = {
    name: formName,
    fields: updatedFields
    };

    console.log("Sending PATCH payload:", payload);


    try {
      if (initial?.id) {
        await api.patch(`/onboarding/form/${initial.id}`, payload);
      } else {
        await api.post("/onboarding/form", payload);
      }
      onSaved?.();
      onClose();
    } catch (e) {
      console.error("Save error", e);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{initial ? "Edit Form" : "Create Form"}</DialogTitle>
      <DialogContent>
        <TextField
          fullWidth
          margin="normal"
          label="Form Name"
          value={formName}
          onChange={(e) => setFormName(e.target.value)}
        />

        {formItems.map((item, idx) => (
          <Box key={idx} sx={{ p: 2, border: "1px solid #ccc", mb: 2 }}>
            {item.type === "FIELD" ? (
              <>
                <TextField
                  select
                  fullWidth
                  label="Select Field"
                  value={item.referenceId}
                  onChange={(e) =>
                    handleFieldChange(idx, "referenceId", e.target.value)
                  }
                  margin="dense"
                >
                  {fields.map((f) => (
                    <MenuItem key={f.id} value={f.id}>
                      {f.name}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  fullWidth
                  margin="dense"
                  label="DTO Field Name"
                  value={item.dtoFieldName}
                  onChange={(e) =>
                    handleFieldChange(idx, "dtoFieldName", e.target.value)
                  }
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={item.required}
                      onChange={(e) =>
                        handleFieldChange(idx, "required", e.target.checked)
                      }
                    />
                  }
                  label="Required"
                />
              </>
            ) : (
              <>
                <TextField
                  fullWidth
                  margin="dense"
                  label="Group DTO Name"
                  value={item.dtoFieldName}
                  onChange={(e) =>
                    handleFieldChange(idx, "dtoFieldName", e.target.value)
                  }
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={item.required}
                      onChange={(e) =>
                        handleFieldChange(idx, "required", e.target.checked)
                      }
                    />
                  }
                  label="Required"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={item.isCollection}
                      onChange={(e) =>
                        handleFieldChange(idx, "isCollection", e.target.checked)
                      }
                    />
                  }
                  label="Is Collection"
                />

                <Button onClick={() => handleGroupFieldAdd(idx)} sx={{ mt: 1 }}>
                  Add Field to Group
                </Button>

                {(item.data || []).map((f, i) => (
                  <Box key={i} sx={{ mt: 1, pl: 2 }}>
                    <TextField
                      select
                      fullWidth
                      label="Select Field"
                      value={f.referenceId}
                      onChange={(e) =>
                        handleFieldChange(i, "referenceId", e.target.value, idx)
                      }
                      margin="dense"
                    >
                      {fields.map((fd) => (
                        <MenuItem key={fd.id} value={fd.id}>
                          {fd.name}
                        </MenuItem>
                      ))}
                    </TextField>
                    <TextField
                      fullWidth
                      label="DTO Field Name"
                      margin="dense"
                      value={f.dtoFieldName}
                      onChange={(e) =>
                        handleFieldChange(
                          i,
                          "dtoFieldName",
                          e.target.value,
                          idx
                        )
                      }
                    />
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={f.required}
                          onChange={(e) =>
                            handleFieldChange(
                              i,
                              "required",
                              e.target.checked,
                              idx
                            )
                          }
                        />
                      }
                      label="Required"
                    />
                  </Box>
                ))}
              </>
            )}
          </Box>
        ))}

        <Box display="flex" gap={2} mt={2}>
          <Button variant="outlined" onClick={handleAddField}>
            Add Field
          </Button>
          <Button variant="outlined" onClick={handleAddGroup}>
            Add Group
          </Button>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={handleSave}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default FormStructureDialog;