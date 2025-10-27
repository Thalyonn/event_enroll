import { useDisclosure } from '@mantine/hooks';
import { Modal, Button } from '@mantine/core';
import { TableSort } from './TableSort';

interface ViewEnrolledProps {
    eventId: number
    onClose?: () => void;
}

export function ViewEnrolledModal({eventId, onClose} : ViewEnrolledProps) {
  const [opened, { open, close }] = useDisclosure(false);
  const handleClose = () => {
    close();
    onClose?.();
  }
  return (
    <>
      <Modal opened={opened} onClose={handleClose} title="View Enrolled" centered size="xl">
        <TableSort eventId={eventId}/>
      </Modal>

      <Button variant="default" onClick={open}>
        View Enrolled
      </Button>
    </>
  );
}