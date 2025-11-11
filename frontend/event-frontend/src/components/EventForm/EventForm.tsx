import { Button, Container, Group, Textarea, TextInput, Title } from '@mantine/core';
import { useForm } from '@mantine/form';
import { DateTimePicker } from '@mantine/dates';
import { DropzoneButton } from '../DropzoneButton/DropzoneButton';
import { useEffect, useState, forwardRef, useImperativeHandle } from 'react';
import '@mantine/dates/styles.css';
import MDEditor from "@uiw/react-md-editor"
import rehypeSanitize from "rehype-sanitize";
import { useNavigateBack } from '../SmartNavigate/NavigateBack';

interface Event {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  eventTime: string;
  capacity: number;
  currentEnrollments: number;
  descriptionMarkdown: string;
}

interface EventFormProps {
  mode: "create" | "edit";
  eventData?: Event | null;
  onSubmit: (formData: FormData) => void;
}

export interface EventFormHandle {
  resetForm: () => void;
  editDisableSuccess: () => void;
}


export const EventForm = forwardRef<EventFormHandle, EventFormProps>(({mode, eventData, onSubmit} , ref) => {
  console.log("eventus")
  console.log(eventData)
  
  const [file, setFile] = useState<File | null>(null);
  const [markdown, setMarkdown] = useState("**Describe the event in more detail here!**");
  const [submitColor , setSubmitColor] = useState<string>();
  
  const goBack = useNavigateBack("/")
  

  
  
  const form = useForm({
    initialValues: {
      title: eventData?.title || '',
      description: eventData?.description || '',
      eventTime: eventData?.eventTime || '',
      capacity: eventData?.capacity || '',
      imageUrl: eventData?.imageUrl || '',
    },
    validate: {
      title: (value) => !value || value.trim().length < 2 ? 'Title needs to be more than 2 characters' : null,
      eventTime: (value) => !value ? 'Select an event time' : null,
      capacity: (value) => {
        if(!value) {
          return 'Enter a positive capacity'
        }
        const capacity = Number(value)
        if(isNaN(capacity) || capacity < 0) {
          return 'Enter a positive capacity';
        }
        return null;
      }
    },
    validateInputOnChange: true,
  });
  const hasChanges =
    form.isDirty() ||
    markdown !== (eventData?.descriptionMarkdown || "**Describe the event in more detail here!**") ||
    file !== null;

  useEffect(() => {
    form.setValues({
      title: eventData?.title || '',
      description: eventData?.description || '',
      eventTime: eventData?.eventTime || '',
      capacity: eventData?.capacity || '',
      imageUrl: eventData?.imageUrl || '',

    })
    setMarkdown(eventData?.descriptionMarkdown || "**Describe the event in more detail here!**")
  },[eventData])
  const handleSubmit = async (values: typeof form.values) => {
    const formData = new FormData();
    formData.append('title', values.title);
    formData.append('description', values.description || '');
    formData.append('descriptionMarkdown', markdown || '')
    formData.append('eventTime', new Date(values.eventTime).toISOString());
    formData.append('capacity', values.capacity.toString());

    if (file) {
      formData.append('image', file);
    }

    onSubmit(formData);

  };

  
  useImperativeHandle(ref, () => ({
    resetForm,
    editDisableSuccess,
  }));
  
  const resetForm = () => {
    form.reset();
    setMarkdown("**Describe the event in more detail here!**");
    setFile(null);  
  }
  const editDisableSuccess = () => {
    form.resetDirty(form.values); // mark everything as clean again
    setSubmitColor('gray');
  }

  return (
    <form onSubmit={form.onSubmit(handleSubmit)}>
      <Title
        order={2}
        size="h1"
        style={{ fontFamily: 'Outfit, var(--mantine-font-family)' }}
        fw={900}
        ta="center"
      >
        {mode === "create" ? "Create Event" : "Edit Event"}
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
        onWheel={(e) => e.currentTarget.blur()} //disables scroll from changing numbers.
        //  It was causing unintended behavior with laptop touchpad scroll gestures
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
      <MDEditor value={markdown} onChange={setMarkdown} 
      previewOptions={{rehypePlugins: [[rehypeSanitize]],
  }}/>
      

      <Container mt="sm">
        <DropzoneButton onFileDrop={(f) => setFile(f)} />
      </Container>

      

      <Group justify="center" mt="xl">
        <Button 
          type="submit" 
          size="md" 
          color={form.isValid() &&hasChanges ? 'lime' : 'gray'} 
          disabled={!hasChanges || !form.isValid() }
        >
          {mode === "create" ? "Create Event" : "Save Changes"}
        </Button>
        <Button size="md" color='red' onClick={goBack}>
          Go Back
        </Button>
      </Group>
    </form>
  );
}
  
);