import { Image, Title, Flex, Text, Button, Box, Stack } from "@mantine/core";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { NothingFoundBackground } from "@/components/errors/404/NothingFoundBackground";
import { useAuth } from "@/context/AuthContext";
import MDEditor from "@uiw/react-md-editor";
import rehypeSanitize from "rehype-sanitize";
import { MarkdownComponent } from "@/components/MarkdownComponent/MarkdownComponent";
import { ViewEnrolledModal } from "@/components/Modals/ViewEnrolled/ViewEnrolledModal";

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

export function EventPage() {
  const { id } = useParams<{ id: string }>();
  const eventIdNumber = Number(id);
  const [event, setEvent] = useState<Event | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [checkingEnrollment, setCheckingEnrollment] = useState(true);
  const url = `http://localhost:8080/api/events/${id}`;
  const { isAuthenticated, isAdmin } = useAuth();
  const navigate = useNavigate();

  const getEvent = async () => {
    console.log("getting event again");
    fetch(url)
    .then((res) => {
      if(res.status === 404) {
        throw new Error("Event not found");
      }
      if(!res.ok) {
        throw new Error("Failed to fetch event");
      }  
      return res.json();
    })
    .then((data) => setEvent(data))
    .catch((err) => {console.error('Failed to fetch events', err)
      setEvent(null);
      setError(err.message);
    });
  }

  const checkEnrolled = async () => {
    setCheckingEnrollment(true);
    fetch(`http://localhost:8080/api/enrollments/check/${id}`, {
      credentials: "include",
    })
      .then((res) => res.json())
      .then((data) => setIsEnrolled(data.enrolled))
      .catch(() => setIsEnrolled(false))
      .finally(() => setCheckingEnrollment(false));
  }

  const onModalClose = () => {
    getEvent();
    checkEnrolled();
  }
  
  useEffect(() => {
  getEvent();
  },
  []);

  useEffect(() => {
    if (!id) {return;}
    checkEnrolled();
  }, [id]);

  console.log(event);
  console.log("Checking enrolled");
  console.log(isEnrolled);

  const handleEnroll = async () => {
    if (!id) {return;}
    if(!isAuthenticated) {
      navigate("/login");
    }
    setLoading(true);
    setMessage(null);
    

    try {
      const response = await fetch(`http://localhost:8080/api/enrollments/${id}`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || "Failed to enroll.");
      }

      setMessage("Successfully enrolled!");
      setIsEnrolled(true);
      setEvent((prev) =>
        prev
          ? { ...prev, currentEnrollments: prev.currentEnrollments + 1 }
          : prev
      ); //if previois event not null then add the ++currentenrollments otherwise return prev
    } catch (err: any) {
      setMessage(err.message);
    } finally {
      setLoading(false);
    }
  };

  const isFull =
    event && event.capacity !== undefined && event.currentEnrollments >= event.capacity;

  //to do: take account of other possible error messages. Only 404 for now.
  if(error){
    return (
      <>
       <NothingFoundBackground />
      </>
    )
  }

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
            mx="auto"
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
            mx={{ base: 'md', sm: 'xl', md: '0' }}
        >
          <Text>{event?.description}</Text>
          <Text>Starts at: {event?.eventTime}</Text>
          <Text fz="sm" c="gray.7">
            Currently Enrolled: {event?.currentEnrollments} / {event?.capacity}
          </Text>
          <Button
            onClick={handleEnroll}
            loading={loading}
            disabled={isFull || isEnrolled || checkingEnrollment}
            color={isFull ? "gray" : isEnrolled ? "teal" : "blue"}
            >{isFull ? "Event is Full" : isEnrolled ? "Enrolled" : "Enroll"}</Button>
          {isAdmin && id && <>
          <ViewEnrolledModal eventId={eventIdNumber} onClose={onModalClose}/>
          <Button color="yellow" onClick={() => navigate(`/edit/${id}`)}>Edit</Button>
          <Button color="red">Hide</Button>
          
          </>
          }
          {message && <Text color="teal">{message}</Text>}
        </Stack>
        
      </Flex>
      
        
    </Stack>
    <Box 
      mx="auto"
      my="lg"
      px={{ base: 'md', md: 'lg' }}
      style={{ maxWidth: 800 }}>
      <MarkdownComponent markdown={event?.descriptionMarkdown}/>
    </Box>
    
      
    </>
  );
}
