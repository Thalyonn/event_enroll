import { Container, Image, Title, Flex, Text, Button, Box, Stack } from "@mantine/core";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

interface Event {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  eventTime: string;
  capacity: number;
  currentEnrollments: number;
}

export function EventPage() {
  const { id } = useParams<{ id: string }>();
  const [event, setEvent] = useState<Event | null>(null);
  const url = `http://localhost:8080/api/events/${id}`;
  
  useEffect(() => {
  fetch(url)
    .then((res) => res.json())
    .then((data) => setEvent(data))
    .catch((err) => console.error('Failed to fetch events', err));
  },
  []);

  console.log(event);

  return (
    <>
    
    <Stack gap="lg" 
    style={{ flexGrow: 1, minWidth: '300px' }} 
    align="center">
      <Title ta="left">{event?.title}</Title>
      <Flex
      mih={50}
      gap="sm"
      justify="flex-start"
      align="flex-start"
      direction="row"
      wrap="wrap"
      >
        <Box 
            w={300} // Set a fixed width for the image wrapper
            style={{ flexShrink: 0 }} // Prevents image from shrinking in Flex
        >
          <Image
              src="https://picsum.photos/id/237/200/300"
              h={400} 
              fit="contain" 
              alt="Event Image"
          />
        </Box>
        
        <Stack 
            gap="lg" 
            style={{ flexGrow: 1, minWidth: '300px' }} 
        >
          <Text>{event?.description}</Text>
          <Text>Starts at: {event?.eventTime}</Text>
          <Text>
            Currently Enrolled: {event?.currentEnrollments} / {event?.capacity}
          </Text>
          <Button>Enroll</Button>
        </Stack>
        
      </Flex>
        
    </Stack>
    
      
    </>
  );
}
