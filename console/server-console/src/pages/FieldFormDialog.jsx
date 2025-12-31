import React, { useState, useEffect } from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Button, Box, Select, InputLabel, FormControl, IconButton, Typography
} from '@mui/material';
import { AddCircle, RemoveCircle } from '@mui/icons-material';
import api from '../services/api';
import { Tooltip } from '@mui/material';
import ContentPasteIcon from '@mui/icons-material/ContentPaste';


const FieldFormDialog = ({ open, onClose, onSaved, initial, copiedField }) => {
  const [name, setName] = useState(initial?.name || '');
  const [type, setType] = useState(initial?.type || '');
  const [config, setConfig] = useState(initial?.fieldConfig || {});
  const [meta, setMeta] = useState({ formFieldTypes: [], fileSizeUnits: [], fileTypes: {} });
  const [lookupDataInput, setLookupDataInput] = useState('');

  useEffect(() => {
    api.get('/identity-meta').then(r => setMeta(r.data.responseData));
  }, []);

  useEffect(() => {
    if (config.lookupData) {
      setLookupDataInput(JSON.stringify(config.lookupData, null, 2));
    }
  }, [config.lookupData]);

  useEffect(() => {
    if (initial) {
        setName(initial.name || '');
        setType(initial.type || '');
        setConfig(initial.fieldConfig || {});
    } else {
        setName('');
        setType('');
        setConfig({});
    }
  }, [initial]);



  const handleConfigChange = (key, value) => setConfig(prev => ({ ...prev, [key]: value }));

  const handlePaste = () => {
    if (copiedField) {
      setName(copiedField.name + " Copy");
      setType(copiedField.type);
      setConfig({ ...copiedField.config });
    }
  };


  const addFileEntry = () => {
    setConfig(prev => ({
      ...prev,
      files: [...(prev.files || []), { type: '', supportedFormats: [], minFileSize: '', maxFileSize: '', scale: '' }]
    }));
  };

  const updateFileEntry = (idx, key, val) => {
    setConfig(prev => {
      const arr = [...(prev.files || [])];
      arr[idx] = { ...arr[idx], [key]: val };
      return { ...prev, files: arr };
    });
  };

  const removeFileEntry = idx => {
    setConfig(prev => {
      const arr = [...(prev.files || [])];
      arr.splice(idx, 1);
      return { ...prev, files: arr };
    });
  };

  const handleSave = async () => {
    if (type === 'FILE') {
      config.isCollection = config.maxNumberOfFiles > 1;
    } else if (type === 'DATE') {
      config.isCollection = false;
    }

    if (lookupDataInput && ['CHECK_BOX', 'DROP_DOWN', 'RADIO_BUTTON'].includes(type)) {
      config.lookupData = JSON.parse(lookupDataInput);
    }

    try {
      const finalConfig = { ...config,
                            lookupData: (typeof config.lookupData === 'object')
                              ? JSON.stringify(config.lookupData)
                              : config.lookupData
                          };
      await api.post('/onboarding/field', { name, type, fieldConfig: finalConfig });
      onSaved();
    } catch (e) {
      alert('Validation error: ' + (e.response?.data?.message || e.message));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          {initial ? 'Edit Field' : 'Create Field'}
          {copiedField && (
            <Tooltip title="Paste copied field">
              <IconButton onClick={handlePaste}>
                <ContentPasteIcon />
              </IconButton>
            </Tooltip>
          )}
      </DialogTitle>
      <DialogContent>
        <Box mb={2}>
          <TextField
            label="Field Name"
            value={name}
            onChange={e => setName(e.target.value)}
            fullWidth margin="dense"
          />
        </Box>
        <Box mb={2}>
          <FormControl fullWidth margin="dense">
            <InputLabel>Field Type</InputLabel>
            <Select value={type} onChange={e => setType(e.target.value)}>
              {meta.formFieldTypes.map(ft => (
                <MenuItem key={ft} value={ft}>{ft.replace('_', ' ')}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {type === 'TEXT_BOX' && (
          <>
            <Box display="flex" gap={2} mb={2}>
              <TextField
                label="Min Length" type="number"
                value={config.minLength || ''} onChange={e => handleConfigChange('minLength', +e.target.value)}
              />
              <TextField
                label="Max Length" type="number"
                value={config.maxLength || ''} onChange={e => handleConfigChange('maxLength', +e.target.value)}
              />
            </Box>
            <TextField
              label="Regex"
              value={config.regex || ''}
              onChange={e => handleConfigChange('regex', e.target.value)}
              fullWidth margin="dense"
            />
            <FormControl fullWidth margin="dense">
              <InputLabel>Input Type</InputLabel>
              <Select
                value={config.tbxInputType || ''}
                onChange={e => handleConfigChange('tbxInputType', e.target.value)}
              >
                {['TEXT','PHONE','E_MAIL','NUMBER','URL'].map(opt => (
                  <MenuItem key={opt} value={opt}>{opt}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </>
        )}

        {(type === 'CHECK_BOX' || type === 'DROP_DOWN' || type === 'RADIO_BUTTON' || type === 'SWITCH') && (
          <>
            {['CHECK_BOX','DROP_DOWN','RADIO_BUTTON'].includes(type) && (
              <TextField
                label="Lookup Data (JSON object)"
                value={lookupDataInput}
                onChange={e => setLookupDataInput(e.target.value)}
                onBlur={() => {
                  try {
                    const parsed = JSON.parse(lookupDataInput);
                    handleConfigChange('lookupData', parsed);
                  } catch (err) {
                    alert("Invalid JSON. Please fix the syntax.");
                  }
                }}
                fullWidth
                margin="dense"
                multiline
                minRows={3}
                placeholder='{"KEY": "Label", "KEY2": "Label2"}'
              />
            )}
            {type === 'CHECK_BOX' || type === 'DROP_DOWN' ? (
              <Box display="flex" gap={2}>
                <TextField
                  label="Min Selection" type="number"
                  value={config.minSelection || 0}
                  onChange={e => handleConfigChange('minSelection', +e.target.value)}
                />
                <TextField
                  label="Max Selection" type="number"
                  value={config.maxSelection || 0}
                  onChange={e => handleConfigChange('maxSelection', +e.target.value)}
                />
              </Box>
            ) : null}
            {type === 'SWITCH' && (
              <Box display="flex" gap={2}>
                <TextField
                  label="On Value" value={config.onValue || ''}
                  onChange={e => handleConfigChange('onValue', e.target.value)}
                />
                <TextField
                  label="Off Value" value={config.offValue || ''}
                  onChange={e => handleConfigChange('offValue', e.target.value)}
                />
              </Box>
            )}
          </>
        )}

        {type === 'DATE' && (
          <Box mb={2}>
            <FormControl fullWidth margin="dense">
              <InputLabel>Date Format</InputLabel>
              <Select
                value={config.dateFormat || ''}
                onChange={e => handleConfigChange('dateFormat', e.target.value)}
              >
                {(meta.dateFormates || []).map(fmt => (
                  <MenuItem key={fmt} value={fmt}>{fmt}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        )}


        {type === 'FILE' && (
          <>
            <Box display="flex" gap={2} mb={2}>
              <TextField
                label="Min Files" type="number"
                value={config.minNumberOfFiles || ''}
                onChange={e => handleConfigChange('minNumberOfFiles', +e.target.value)}
              />
              <TextField
                label="Max Files" type="number"
                value={config.maxNumberOfFiles || ''}
                onChange={e => handleConfigChange('maxNumberOfFiles', +e.target.value)}
              />
            </Box>

            <Typography variant="subtitle1">Allowed File Entries</Typography>
            {(config.files || []).map((f, i) => (
              <Box key={i} display="flex" gap={2} alignItems="center" my={1}>
                <FormControl sx={{ minWidth: 140 }}>
                  <InputLabel>Type</InputLabel>
                  <Select
                    value={f.type} onChange={e => updateFileEntry(i, 'type', e.target.value)}
                  >
                    {Object.keys(meta.fileTypes || {}).map(cat => (
                      <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <FormControl sx={{ minWidth: 200 }}>
                  <InputLabel>Formats</InputLabel>
                  <Select
                    multiple
                    value={f.supportedFormats}
                    onChange={e => updateFileEntry(i, 'supportedFormats', e.target.value)}
                  >
                    {(meta.fileTypes[f.type] || []).map(fmt => (
                      <MenuItem key={fmt} value={fmt}>{fmt}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <TextField
                  label="Min Size" type="number"
                  value={f.minFileSize || ''} onChange={e => updateFileEntry(i, 'minFileSize', +e.target.value)}
                  sx={{ width: 100 }}
                />
                <TextField
                  label="Max Size" type="number"
                  value={f.maxFileSize || ''} onChange={e => updateFileEntry(i, 'maxFileSize', +e.target.value)}
                  sx={{ width: 100 }}
                />
                <FormControl sx={{ minWidth: 100 }}>
                  <InputLabel>Unit</InputLabel>
                  <Select
                    value={f.scale} onChange={e => updateFileEntry(i, 'scale', e.target.value)}
                  >
                    {meta.fileSizeUnits.map(u => <MenuItem key={u} value={u}>{u}</MenuItem>)}
                  </Select>
                </FormControl>
                <IconButton onClick={() => removeFileEntry(i)}><RemoveCircle /></IconButton>
              </Box>
            ))}
            <Button startIcon={<AddCircle />} onClick={addFileEntry}>Add File Entry</Button>
          </>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSave} variant="contained">Save Field</Button>
      </DialogActions>
    </Dialog>
  );
};

export default FieldFormDialog;