import { CreateEvent } from '../components/CreateEvent/CreateEvent';
import { Container, Text } from '@mantine/core';
import { EventForm } from '@/components/EventForm/EventForm';
import { useState } from 'react';

export function CreateEventPage() {
  const [message, setMessage] = useState<string | null>(null)
  const handleSubmit = async (formData: FormData) => {

    const res = await fetch('http://localhost:8080/api/events', {
      method: 'POST',
      body: formData,
      credentials: 'include', 
    });

    if (!res.ok) {
      console.error('Error creating event');
    } else {
      const createdEvent = await res.json();
      console.log('Event created:', createdEvent);
      setMessage("Event succesfully created")
      
    }
    }
  return (
    <>
      <Container>
        {message && <Text ta="center" color='teal'>{message}</Text>}
        <EventForm mode="create" onSubmit={handleSubmit}/>
      </Container>

      
    </>
  );
}