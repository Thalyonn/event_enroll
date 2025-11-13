import '@mantine/core/styles.css';

import { MantineProvider } from '@mantine/core';
import { Router } from './Router';
import { theme } from './theme';
import { AuthProvider } from './context/AuthContext';
import { Notifications } from '@mantine/notifications';

export default function App() {
  return (
    <AuthProvider>
      <MantineProvider theme={theme}>
        <Notifications 
          styles={{
          notification: {
            width: 'auto',       
            maxWidth: 'fit-content',
            minWidth: 'unset',
          },
        }}/>
        <Router />
      </MantineProvider>
    </AuthProvider>
    
  );
}
