import { EventGrid } from '@/components/EventGrid/EventGrid';
import { ColorSchemeToggle } from '../components/ColorSchemeToggle/ColorSchemeToggle';
import { Welcome } from '../components/Welcome/Welcome';
import { Container } from '@mantine/core';


export function HomePage() {
  

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
