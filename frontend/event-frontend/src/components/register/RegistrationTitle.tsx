import {
  Anchor,
  Button,
  Checkbox,
  Container,
  Group,
  Paper,
  PasswordInput,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import classes from './RegistrationTitle.module.css';

export function RegistrationTitle() {
  return (
    <Container size={420} my={40}>
      <Title ta="center" className={classes.title}>
        Create an Account
      </Title>

      

      <Paper withBorder shadow="sm" p={22} mt={30} radius="md">
        <TextInput label="Email" placeholder="you@mantine.dev" required radius="md" />
        <TextInput label="Username" placeholder="username" required radius="md" mt="md" />
        <PasswordInput label="Password" placeholder="Your password" required mt="md" radius="md" />
        <PasswordInput label="Confirm Password" placeholder="Your password" required mt="md" radius="md" />
        <Button fullWidth mt="xl" radius="md">
          Register
        </Button>
      </Paper>
    </Container>
  );
}