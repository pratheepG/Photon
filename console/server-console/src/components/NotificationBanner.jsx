// // =================================================================
// // 1. NOTIFICATION CONTEXT & HOOK (Conceptually: useNotification.js)
// // This is the entry point for triggering notifications globally.
// // =================================================================

// const NotificationContext = React.createContext(null);

// /**
//  * Custom hook to access the showNotification function.
//  * Use this in any component that needs to display a success or error banner.
//  * Example: const { showNotification } = useNotification();
//  */
// const useNotification = () => {
//     const context = useContext(NotificationContext);
//     if (context === null) {
//         throw new Error('useNotification must be used within a NotificationProvider');
//     }
//     return context;
// };


// // =================================================================
// // 2. NOTIFICATION BANNER UI (Conceptually: NotificationBanner.jsx)
// // This component renders the visual banner.
// // =================================================================

// /**
//  * UI Component for the global status banner.
//  */
// const NotificationBanner = ({ notification, handleClose }) => {
//     const { open, message, type } = notification;

//     // Use MUI Snackbar and Alert for a professional, non-blocking notification
//     return (
//         <Snackbar 
//             open={open} 
//             autoHideDuration={4000} 
//             onClose={handleClose} 
//             anchorOrigin={{ vertical: "top", horizontal: "center" }}
//         >
//             <Alert 
//                 onClose={handleClose} 
//                 severity={type} // 'success' or 'error'
//                 variant="filled" 
//                 sx={{ width: "100%", minWidth: '300px' }}
//             >
//                 {message}
//             </Alert>
//         </Snackbar>
//     );
// };


// // =================================================================
// // 3. NOTIFICATION PROVIDER (Conceptually: NotificationProvider.jsx)
// // This component manages the state and wraps the application.
// // =================================================================

// const NotificationProvider = ({ children }) => {
//     const [notification, setNotification] = useState({
//         open: false,
//         message: '',
//         type: 'success', // success, error
//     });
    
//     // Function to close the banner manually or via timeout
//     const handleClose = () => {
//         setNotification(prev => ({ ...prev, open: false }));
//     };

//     /**
//      * Shows a global notification banner.
//      * @param {string} message - The message to display.
//      * @param {('success'|'error')} type - The type of notification.
//      */
//     const showNotification = (message, type = 'success') => {
//         if (!message) return;
        
//         // Use handleClose to reset state before showing a new one (optional, but good practice)
//         handleClose();

//         setNotification({
//             open: true,
//             message,
//             type,
//         });
//     };

//     return (
//         <NotificationContext.Provider value={{ showNotification }}>
//             {children}
//             <NotificationBanner notification={notification} handleClose={handleClose} />
//         </NotificationContext.Provider>
//     );
// };