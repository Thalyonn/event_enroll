import { EventGrid } from '@/components/EventGrid/EventGrid';
import { ColorSchemeToggle } from '../components/ColorSchemeToggle/ColorSchemeToggle';
import { Welcome } from '../components/Welcome/Welcome';
import { Button, Container } from '@mantine/core';
import { useLocation } from 'react-router-dom';
import { showNotification } from '@mantine/notifications'; 
import { useEffect } from 'react';


export function HomePage() {
  const location = useLocation();

  useEffect(() => {
    if(location.state?.showHideSuccess) {
      console.log("HIDING EVENT NOTIF");
      showNotification( {
        title: 'Event hidden',
        message: 'Event hidden successfully',
        color: 'green',
      });
      window.history.replaceState({}, document.title);
    }

  }, [location.state]);

  return (
    <>
      <Welcome />
      <ColorSchemeToggle />
      <Container size="lg" my="xl"> 
        <EventGrid />
      </Container>
      
    </>
  );
}
