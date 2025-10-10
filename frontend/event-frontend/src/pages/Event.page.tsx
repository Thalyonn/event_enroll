import { Container, Image } from "@mantine/core";
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
    <Container>
        <Image
        //set to imageUrl
            src="https://picsum.photos/id/237/200/300"
            height={160}
            alt="Event Image"
        />
    </Container>
      
    </>
  );
}
