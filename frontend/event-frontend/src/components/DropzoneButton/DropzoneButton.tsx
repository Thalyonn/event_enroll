import { forwardRef, useImperativeHandle, useRef, useState } from 'react';
import { IconCloudUpload, IconDownload, IconX } from '@tabler/icons-react';
import { Button, Group, Text, useMantineTheme } from '@mantine/core';
import { Dropzone, FileWithPath, MIME_TYPES } from '@mantine/dropzone';
import classes from './DropzoneButton.module.css';
import '@mantine/core/styles.css';
import '@mantine/dropzone/styles.css';

export interface DropzoneButtonHandle {
  handleRemove: () => void;
}

interface DropzoneButtonProps {
  onFileDrop: (file: File | null) => void;
}


export const DropzoneButton= forwardRef<DropzoneButtonHandle, DropzoneButtonProps>(
  ({ onFileDrop }, ref) => {
  const theme = useMantineTheme();
  const openRef = useRef<() => void>(null);
  const [file, setFile] = useState<File | null>(null);

  const handleDrop = (files: FileWithPath[]) => {
    if (files.length>0) {
      setFile(files[0]);
      onFileDrop(files[0]);
    }

  }

  useImperativeHandle(ref, () => ({
      handleRemove,
  }));

  const handleRemove = () => {
    setFile(null);
    onFileDrop(null);
  }

  return (
    <div className={classes.wrapper}>
      <Dropzone
        openRef={openRef}
        onDrop={handleDrop}
        className={classes.dropzone}
        radius="md"
        accept={[MIME_TYPES.png, MIME_TYPES.jpeg]}
        maxSize={5 * 1024 ** 2} // 5MB max
      >
        <div style={{ pointerEvents: 'none' }}>
          <Group justify="center">
            <Dropzone.Accept>
              <IconDownload size={50} color={theme.colors.blue[6]} stroke={1.5} />
            </Dropzone.Accept>
            <Dropzone.Reject>
              <IconX size={50} color={theme.colors.red[6]} stroke={1.5} />
            </Dropzone.Reject>
            <Dropzone.Idle>
              <IconCloudUpload size={50} stroke={1.5} className={classes.icon} />
            </Dropzone.Idle>
          </Group>

          <Text ta="center" fw={700} fz="lg" mt="xl">
            <Dropzone.Accept>Drop images here</Dropzone.Accept>
            <Dropzone.Reject>Only PNG or JPEG files less than 5MB</Dropzone.Reject>
            <Dropzone.Idle>{file ? "Replace Image" : "Upload Image"}</Dropzone.Idle>
          </Text>

          <Text className={classes.description}>
            Drag&apos;n&apos;drop image here to upload. We can accept only <i>.png</i> and <i>.jpeg</i> files less than 5MB in size.
          </Text>
        </div>
      </Dropzone>

      

      {
        file && (
          <Group justify="space-between" mt="xs">
            <Text>Image to upload: <strong>{file.name}</strong></Text>
            <Button
            variant="subtle"
            color="red"
            size="xs"
            leftSection={<IconX size={14} />}
            onClick={handleRemove}
            >
              Remove
            </Button>
          </Group>
        )
      }
    </div>
  );
}

)