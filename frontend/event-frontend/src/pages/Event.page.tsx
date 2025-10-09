import { Container, Image } from "@mantine/core";

export function EventPage() {
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
