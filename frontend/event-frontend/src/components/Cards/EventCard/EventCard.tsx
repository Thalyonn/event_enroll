import { Card, Image, Text, Badge, Button, Group } from '@mantine/core';

interface EventCardProps {
  title: string;
  description: string;
  imageUrl: string;
  eventTime: string;
  capacity?: number;
  currentEnrollments?: number;
}

export function EventCard({title, description, imageUrl, eventTime, capacity, currentEnrollments} : EventCardProps) {
  const isFull = capacity !== undefined && currentEnrollments !== undefined && currentEnrollments >= capacity;
  return (
    <Card shadow="sm" padding="lg" radius="md" withBorder>
      <Card.Section>
        <Image
        //set to imageUrl
          src="https://picsum.photos/id/237/200/300"
          height={160}
          alt="Event Image"
        />
      </Card.Section>

      <Group justify="space-between" mt="md" mb="xs">
        <Text fw={500}>{title}</Text>
        {capacity !== undefined && (
          <Badge color={isFull ? 'red' : 'green'}>
            {currentEnrollments ?? 0}/{capacity}
          </Badge>
        )}
      </Group>
      <Text size="md">{eventTime}</Text>

      <Text size="sm" c="dimmed">
        {description}
      </Text>

      <Button color="blue" fullWidth mt="md" radius="md">
        View More
      </Button>
    </Card>
  );
}