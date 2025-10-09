import { SimpleGrid } from "@mantine/core";
import { EventCard } from "../Cards/EventCard/EventCard";
import { useEffect, useState } from "react";

interface Event {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  eventTime: string;
  capacity: number;
  currentEnrollments: number;
}



export function EventGrid() {
    const [events, setEvents] = useState<Event[]>([]);

    useEffect(() => {
    fetch('http://localhost:8080/api/events')
      .then((res) => res.json())
      .then((data) => setEvents(data))
      .catch((err) => console.error('Failed to fetch events', err));
    },
    []);

    return (
        <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }}>
            {events.map((event: Event) => (
                <EventCard 
                    key={event.id}
                    id={event.id}
                    title={event.title}
                    description={event.description}
                    imageUrl={event.imageUrl}
                    eventTime={event.eventTime}
                    capacity={event.capacity}
                    currentEnrollments={event.currentEnrollments}

                />
            ))}
        </SimpleGrid>
    );
}