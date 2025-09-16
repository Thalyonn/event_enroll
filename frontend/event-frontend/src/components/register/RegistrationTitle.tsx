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
import { useForm } from '@mantine/form';
import { useNavigate } from "react-router-dom";

export function RegistrationTitle() {
  const form = useForm({
    initialValues: {
      email: "",
      username: "",
      password: "",
      confirmPassword: "",
    },

    validate: {
      username: (value) => (value.length < 2 ? 'Name must have at least 2 letters' : null),
      email: (value) => (/^\S+@\S+$/.test(value) ? null : 'Invalid email'),
      password: (value) => value.length < 6 ? "Password must be at least 6 characters" : null,
      confirmPassword: (value, values) => value !== values.password ? "Passwords do not match" : null,
    }
  });

  const navigate = useNavigate();

  const handleSubmit = async (values: typeof form.values) => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: values.email,
          username: values.username,
          password: values.password,
        }),
      });

      if (response.ok) {
        const data = await response.json();
        console.log("Registered:", data);
        //alert("Registration successful!");
        form.reset();
        navigate("/");
      } else {
        const err = await response.json();
        console.log(err);
        //alert(err.error || "Registration failed");
      }
    } catch (error) {
      console.error("Error registering:", error);
      //alert("Something went wrong.");
    }
  };
  


  return (
    <Container size={420} my={40}>
      <Title ta="center" className={classes.title}>
        Create an Account
      </Title>

      
      
      <Paper withBorder shadow="sm" p={22} mt={30} radius="md">
        <form onSubmit={form.onSubmit(handleSubmit)}>
          <TextInput label="Email" placeholder="you@mantine.dev" required radius="md" {...form.getInputProps("email")}/>
          <TextInput label="Username" placeholder="username" required radius="md" mt="md" {...form.getInputProps("username")}/>
          <PasswordInput label="Password" placeholder="Your password" required mt="md" radius="md" {...form.getInputProps("password")}/>
          <PasswordInput label="Confirm Password" placeholder="Your password" required mt="md" radius="md" {...form.getInputProps("confirmPassword")}/>
          <Button type="submit" fullWidth mt="xl" radius="md">
            Register
          </Button>
        </form>
        
      </Paper>
    </Container>
  );
}