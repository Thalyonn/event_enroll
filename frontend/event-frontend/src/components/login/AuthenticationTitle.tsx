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
import classes from './AuthenticationTitle.module.css';
import { useForm } from "@mantine/form"
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

export function AuthenticationTitle() {
  const form = useForm({
    initialValues: {
     username: "",
     password:  "",
    },
    //Placeholder validations for now
    validate: {
    username: (value) => (value.length < 2 ? "Username must have at least 2 characters" : null),
    password: (value) => (value.length < 2 ? "Password must be at least 2 characters" : null),
    }
  });
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (values: typeof form.values) => {
    try {
      await login(values.username, values.password); 
      console.log("Login success!");
      navigate("/"); // redirect to homepage
    } catch (error) {
      console.error("Login failed:", error);
      form.setErrors({ password: "Invalid username or password" });
    }
  };
  //To add: remember me function
  return (
    <Container size={420} my={40}>
      <Title ta="center" className={classes.title}>
        Welcome back!
      </Title>

      <Text className={classes.subtitle}>
        Do not have an account yet? <Anchor component={Link} to="/register">Create account</Anchor>
      </Text>

      <Paper withBorder shadow="sm" p={22} mt={30} radius="md">
        <form onSubmit={form.onSubmit(handleSubmit)}>
          <TextInput label="Username" placeholder="Your Username" required radius="md" {...form.getInputProps("username")}/>
          <PasswordInput label="Password" placeholder="Your password" required mt="md" radius="md" {...form.getInputProps("password")}/>
          <Group justify="space-between" mt="lg">
            <Checkbox label="Remember me" />
            <Anchor component="button" size="sm">
              Forgot password?
            </Anchor>
          </Group>
          <Button type="submit" fullWidth mt="xl" radius="md">
            Sign in
          </Button>
        </form>
      </Paper>
    </Container>
  );
}