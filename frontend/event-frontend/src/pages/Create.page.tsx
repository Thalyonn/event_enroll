import { Container, Text } from '@mantine/core';
import { EventForm, EventFormHandle } from '@/components/EventForm/EventForm';
import { useRef, useState } from 'react';

export function CreateEventPage() {
  const [message, setMessage] = useState<string | null>(null)
  const [color, setColor] = useState<string | null>(null)
  const [isSubmitting, setIsSubmittting] = useState<boolean>(false)
  const handleSubmit = async (formData: FormData) => {
    setIsSubmittting(true);
    const res = await fetch('http://localhost:8080/api/events', {
      method: 'POST',
      body: formData,
      credentials: 'include', 
    });

    if (!res.ok) {
      setColor("red")
      console.error('Error creating event');
      setMessage("Error in event creation");
    } else {
      const createdEvent = await res.json();
      console.log('Event created:', createdEvent);
      setColor('teal');
      setMessage("Event succesfully created");
      formRef.current?.resetForm();
    }
    setIsSubmittting(false);
  }
  const formRef = useRef<EventFormHandle>(null);
  return (
    <>
      <Container my="md">
        {message && <Text ta="center" color={color || "red"}>{message}</Text>}
        <EventForm mode="create" onSubmit={handleSubmit} ref={formRef} isSubmitting={isSubmitting}/>
      </Container>

      
    </>
  );
}