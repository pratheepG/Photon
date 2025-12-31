import { useEffect, useState } from "react";
import { Alert, Snackbar } from "@mui/material";
import { subscribeToErrors } from "../services/api";

export default function ErrorBanner() {
  const [error, setError] = useState(null);

  useEffect(() => {
    const unsubscribe = subscribeToErrors((msg) => {
      setError(msg);
    });
    return unsubscribe;
  }, []);

  const handleClose = () => setError(null);

  return (
    <Snackbar open={!!error} autoHideDuration={5000} onClose={handleClose} anchorOrigin={{ vertical: "top", horizontal: "center" }}>
      <Alert onClose={handleClose} severity="error" variant="filled" sx={{ width: "100%" }}>
        {error}
      </Alert>
    </Snackbar>
  );
}