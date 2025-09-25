import { Button, Group, SimpleGrid, Textarea, TextInput, Title } from '@mantine/core';
import { useForm } from '@mantine/form';
import { DateTimePicker } from '@mantine/dates';
import { DropzoneButton } from '../DropzoneButton/DropzoneButton';
import '@mantine/dates/styles.css';

//needs title, description, imageUrl, eventTime, capacity
export function CreateEvent() {
  const form = useForm({
    initialValues: {
      title: '',
      description: '',
      eventTime: '',
      capacity: '',
      imageUrl: '',
    },
    validate: {
      name: (value) => value.trim().length < 2,
      email: (value) => !/^\S+@\S+$/.test(value),
      subject: (value) => value.trim().length === 0,
    },
  });

  return (
    <form onSubmit={form.onSubmit(() => {})}>
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
        placeholder="10"
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

      <DropzoneButton/>

      <Group justify="center" mt="xl">
        <Button type="submit" size="md">
          Create Event
        </Button>
      </Group>
    </form>
  );
}