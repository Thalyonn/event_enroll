import { useRef } from 'react';
import { IconCloudUpload, IconDownload, IconX } from '@tabler/icons-react';
import { Button, Group, Text, useMantineTheme } from '@mantine/core';
import { Dropzone, MIME_TYPES } from '@mantine/dropzone';
import classes from './DropzoneButton.module.css';
import '@mantine/core/styles.css';
import '@mantine/dropzone/styles.css';

export function DropzoneButton({ onFileDrop }: { onFileDrop: (file: File) => void }) {
  const theme = useMantineTheme();
  const openRef = useRef<() => void>(null);

  return (
    <div className={classes.wrapper}>
      <Dropzone
        openRef={openRef}
        onDrop={(files) => {
          if (files.length > 0) {
            onFileDrop(files[0]);
          }
        }}
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
            <Dropzone.Idle>Upload profile picture</Dropzone.Idle>
          </Text>

          <Text className={classes.description}>
            Drag&apos;n&apos;drop image here to upload. We can accept only <i>.png</i> and <i>.jpeg</i> files less than 5MB in size.
          </Text>
        </div>
      </Dropzone>

      <Button className={classes.control} size="sm" radius="xl" onClick={() => openRef.current?.()}>
        Select file
      </Button>
    </div>
  );
}
