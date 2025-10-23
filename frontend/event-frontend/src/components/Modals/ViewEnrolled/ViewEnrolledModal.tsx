import { useDisclosure } from '@mantine/hooks';
import { Modal, Button } from '@mantine/core';
import { TableSort } from './TableSort';

interface ViewEnrolledProps {
    eventId: number
}

export function ViewEnrolledModal({eventId} : ViewEnrolledProps) {
  const [opened, { open, close }] = useDisclosure(false);

  return (
    <>
      <Modal opened={opened} onClose={close} title="View Enrolled" centered size="xl">
        <TableSort eventId={eventId}/>
      </Modal>

      <Button variant="default" onClick={open}>
        View Enrolled
      </Button>
    </>
  );
}