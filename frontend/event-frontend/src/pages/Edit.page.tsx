import { EventForm, EventFormHandle } from "@/components/EventForm/EventForm";
import { useEffect, useRef, useState } from "react";
import { Container, Text } from "@mantine/core";
import { useParams } from "react-router-dom";

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

export function EditEventPage() {
    const { id } = useParams<{ id: string }>();
    const [message, setMessage] = useState<string | null>(null);
    const [color, setColor] = useState<string | null>(null);
    const [eventData, setEventData] = useState<Event | null>(null);
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
    useEffect(() => {
      setIsSubmitting(true);
        console.log("Getting event for edit")
        fetch(`http://localhost:8080/api/events/${id}`, {credentials: 'include'})
        .then((res) => {
        if(res.status === 404) {
            throw new Error("Event not found");
            setColor("red");
            setMessage("Event not found");
        }
        if(!res.ok) {
            throw new Error("Failed to fetch event");
            setColor("red");
            setMessage("Failed to fetch event");
        }  
        return res.json();
        })
        .then((data) => setEventData(data))
        .catch((err) => {console.error('Failed to fetch events', err)
        setEventData(null);
        });
        setIsSubmitting(false);
    },[id]);
    const handleUpdateEvent = async (formData: FormData) => {
      setIsSubmitting(true);
        const res=await fetch(`http://localhost:8080/api/events/${id}`,
            {
                method: "PUT",
                credentials: 'include',
                body: formData,                
             }
             
        )
        if (!res.ok) {
          console.error('Error on event update');
          setColor("red");
          setMessage("Event update failed");
        }
        else {
          console.log('Event updated succesfully:', await res.json());
          formRef.current?.editDisableSuccess();
          setColor("teal");
          setMessage("Event edited successfully");
        }
        
        setIsSubmitting(false);
    }
  const formRef = useRef<EventFormHandle>(null);
  return (

    
    <>
      <Container my="md">
        {message && <Text ta="center" color={color || "red"}>{message}</Text>}
        <EventForm ref={formRef} mode="edit" eventData={eventData} onSubmit={handleUpdateEvent} isSubmitting={isSubmitting}/>
      </Container>

      
    </>
  );
}