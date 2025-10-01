import { Button, Group, Textarea, TextInput, Title } from '@mantine/core';
import { useForm } from '@mantine/form';
import { DateTimePicker } from '@mantine/dates';
import { DropzoneButton } from '../DropzoneButton/DropzoneButton';
import { useState } from 'react';
import '@mantine/dates/styles.css';

//needs title, description, imageUrl, eventTime, capacity
export function CreateEvent() {
  const [file, setFile] = useState<File | null>(null);
  const form = useForm({
    initialValues: {
      title: '',
      description: '',
      eventTime: '',
      capacity: '',
      imageUrl: '',
    },
    validate: {
      title: (value) => value.trim().length < 2,
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    const formData = new FormData();
    formData.append('title', values.title);
    formData.append('description', values.description);
    formData.append('eventTime', new Date(values.eventTime).toISOString());
    formData.append('capacity', values.capacity.toString());

    if (file) {
      formData.append('image', file);
    }

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
    }

  };

  

  return (
    <form onSubmit={form.onSubmit(handleSubmit)}>
      <Title
        order={2}
        size="h1"
        style={{ fontFamily: 'Outfit, var(--mantine-font-family)' }}
        fw={900}
        ta="center"
      >
        Create Event
      </Title>

      

      <TextInput
          label="Title"
          placeholder="Event title"
          mt="md"
          name="title"
          variant="filled"
          {...form.getInputProps('title')}
        />

      <TextInput
        label="Capacity"
        placeholder="Event Capacity"
        mt="md"
        name="capacity"
        variant="filled"
        type="number"
        {...form.getInputProps('capacity')}
      />
      <DateTimePicker 
        label="Pick date and time" 
        placeholder="Pick date and time" 
        mt="md"
        name="eventTime"
        {...form.getInputProps('eventTime')}
      />
      <Textarea
        mt="md"
        mb="md"
        label="Description"
        placeholder="Event description"
        maxRows={10}
        minRows={5}
        autosize
        name="description"
        variant="filled"
        {...form.getInputProps('description')}
      />

      <DropzoneButton onFileDrop={(f) => setFile(f)} />

      <Group justify="center" mt="xl">
        <Button type="submit" size="md">
          Create Event
        </Button>
      </Group>
    </form>
  );
}